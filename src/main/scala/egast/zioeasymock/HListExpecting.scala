package egast.zioeasymock

import egast.zioeasymock.HList._
import org.easymock.{EasyMock, IExpectationSetters}
import zio.test.{Assertion, TestResult, assert, assertCompletes}
import zio.{IO, Task, ZIO}

import scala.reflect._

sealed trait EasyMockMock[A] {
  val cls: Class[_]

}

sealed trait ExpectationSetter[A]

case class AndReturn[A, B](value: B) extends ExpectationSetter[B]

case class Expectation[A, B](expectation: A => B, expectationSetter: IExpectationSetters[B] => Unit)

case class StandardMock[A](cls: Class[_]) extends EasyMockMock[A]

object StandardMock {
  def apply[A](implicit cls: ClassTag[A]): StandardMock[A] =
    StandardMock(cls.runtimeClass)
}

case class MockWithExpectations[A, E <: HList](mock: EasyMockMock[A], expectations: E) {

  def expect[B](expectation: A => B)(expectationSetter: IExpectationSetters[B] => Unit): MockWithExpectations[A, Expectation[A, B] :: E] =
    this.copy(expectations = HCons(Expectation(expectation, expectationSetter), expectations))
}

trait ExpectationBuilder[A, B] {
  def build(expectation: A, mock: B): Task[B]
}

object ExpectationBuilder {

  def apply[A <: HList, B](implicit builder: ExpectationBuilder[A, B]): ExpectationBuilder[A, B] = builder

  def derive[A <: HList, B, M](expectations: MockWithExpectations[M, A])(implicit builder: ExpectationBuilder[A, B]): ExpectationBuilder[A, B] = builder

  implicit def hNilCase[A]: ExpectationBuilder[HNil.type, A] = new ExpectationBuilder[HNil.type, A] {
    override def build(expectation: HNil.type, mock: A): Task[A] = Task.fail(new RuntimeException("No expectations set"))
  }

  implicit def lastCase[A, B]: ExpectationBuilder[Expectation[A, B] :: HNil.type, A] = new ExpectationBuilder[Expectation[A, B] :: HNil.type, A] {
    override def build(expectation: Expectation[A, B] :: HNil.type, mock: A): Task[A] =
      expect(expectation.head.expectation(mock))
        .flatMap(EasyMockInterpreter.processSetter(_, expectation.head.expectationSetter))
        .as(mock)
  }

  implicit def deriveExpectationBuilder[A, B, E <: HList](implicit tail: ExpectationBuilder[E, A]): ExpectationBuilder[Expectation[A, B] :: E, A] =
    new ExpectationBuilder[Expectation[A, B] :: E, A] {
      override def build(expectation: Expectation[A, B] :: E, mock: A): Task[A] =
        expect(expectation.head.expectation(mock))
          .flatMap(EasyMockInterpreter.processSetter(_, expectation.head.expectationSetter))
          .flatMap(_ => tail.build(expectation.tail, mock))
    }

}

object EasyMockInterpreter {
  def testWithMock[A <: AnyRef, EX <: HList, R, E]
  (mockWithExpectations: MockWithExpectations[A, EX])(test: A => ZIO[R, E, TestResult])(implicit expectations: ExpectationBuilder[EX, A]): ZIO[R, E, TestResult] = (for {
    mock <- toMock(mockWithExpectations.mock)
    expect <- Task(expectations.build(mockWithExpectations.expectations, mock)).flatten
    replayResult <- replayTestResult(mock)
    testResult1 <- Task(
      test(mock)
        .catchAllCause(c => ZIO.fail(c.defects.headOption.getOrElse(new AssertionError("unknown mocking error"))))
    )
    testResult <- testResult1
    verifyResult <- verifyTestResult(mock)
  } yield replayResult && testResult && verifyResult)
    .fold(t => assert(t)(failMockExpectation), v => v)

  private def toMock[A](mock: EasyMockMock[A]): Task[A] = mock match {
    case StandardMock(cls) => Task(EasyMock.mock[A](cls))
  }

  def processSetter[A](a: IExpectationSetters[A], setter: IExpectationSetters[A] => Unit): Task[Unit] =
    Task.effect(setter(a))


  private def replayZio[A <: AnyRef](mock: A): Task[A] =
    ZIO.effect(EasyMock.replay(mock)).as(mock)

  private def verifyZio[A <: AnyRef](mock: A): Task[A] =
    ZIO.effect(EasyMock.verify(mock)).as(mock)

  private def replayTestResult[M <: AnyRef](m: M): IO[Throwable, TestResult] =
    replayZio(m).as(assertCompletes)

  private def verifyTestResult[M <: AnyRef](m: M): IO[Throwable, TestResult] =
    verifyZio(m).as(assertCompletes)

  private def failMockExpectation: Assertion[Throwable] =
    Assertion.assertion("mocking expectations")()(_ => false)
}

object EasyMockMockTestApp extends App {

  case class TestClass(s: String, n: Int)


  val mock = StandardMock[TestClass]
  val m = MockWithExpectations(mock, HNil)
    .expect[String](_.s)(_.andReturn("Test"))
  //    .expect(_.s)(_.andReturn("Bla"))
  //    .expect(_.n)(_.andReturn(100))
  val derived = ExpectationBuilder.deriveExpectationBuilder[TestClass, Int,
    Expectation[TestClass, String] :: HNil.type]
  val derived2 = ExpectationBuilder.derive(m)

  val runtime = zio.Runtime.default
  val easyMock = EasyMock.mock[TestClass](classOf[TestClass])

  val result = derived2.build(m.expectations, easyMock)
  //  val r = runtime.unsafeRun(result)
  //  println(r)
  //  val replayedMock = runtime.unsafeRun(testWithMock(m))
  //  println(replayedMock)


}

