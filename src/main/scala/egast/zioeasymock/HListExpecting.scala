package egast.zioeasymock

import egast.zioeasymock.HList._
import org.easymock.{EasyMock, IExpectationSetters}
import zio.{Task, ZIO}

sealed trait EasyMockMock[A] {
  val cls: Class[_]

}

sealed trait ExpectationSetter[A]

case class AndReturn[A, B](value: B) extends ExpectationSetter[B]

case class Expectation[A, B](expectation: A => B, expectationSetter: ExpectationSetter[B])

case class StandardMock[A](cls: Class[_]) extends EasyMockMock[A]

case class MockWithExpectations[A, E <: HList](mock: EasyMockMock[A], expectations: E) {

  def expect[B](expectation: A => B)(expectationSetter: ExpectationSetter[B]): MockWithExpectations[A, Expectation[A, B] :: E] =
    this.copy(expectations = HCons(Expectation(expectation, expectationSetter), expectations))
}

trait ExpectationBuilder[A, B] {
  def build(a: A, b: B): Task[B]
}

object ExpectationBuilder {

  def apply[A <: HList, B](implicit builder: ExpectationBuilder[A, B]): ExpectationBuilder[A, B] = builder
  def derive[A <:HList, B, M](expectations: MockWithExpectations[M, A])(implicit builder: ExpectationBuilder[A, B]): ExpectationBuilder[A, B] = builder

  implicit def hNilCase[A]: ExpectationBuilder[HNil.type, A] = new ExpectationBuilder[HNil.type, A] {
    override def build(a: HNil.type, b: A): Task[A] = Task.fail(new RuntimeException("No expectations set"))
  }

  implicit def lastCase[A, B]: ExpectationBuilder[Expectation[A, B] :: HNil.type , A] = new ExpectationBuilder[Expectation[A, B] :: HNil.type , A] {
    override def build(a: Expectation[A, B] :: HNil.type , b: A): Task[A] =
      Task(EasyMock.expect(a.head.expectation(b)))
        .flatMap(EasyMockInterpreter.processSetter(_, a.head.expectationSetter))
        .as(b)
  }

  implicit def deriveExpectationBuilder[A, B, E <: HList](implicit tail: ExpectationBuilder[E, A]): ExpectationBuilder[Expectation[A, B] :: E, A] =
    new ExpectationBuilder[Expectation[A, B] :: E, A] {
      override def build(a: Expectation[A, B] :: E, b: A): Task[A] =
        Task(EasyMock.expect(a.head.expectation(b)))
          .flatMap(EasyMockInterpreter.processSetter(_, a.head.expectationSetter))
          .flatMap(_ => tail.build(a.tail, b))
    }
}

object EasyMockInterpreter {
  def assert[A, E <: HList](mockWithExpectations: MockWithExpectations[A, E]) = for {
    mock <- toMock(mockWithExpectations.mock)

  } yield ???

  private def toMock[A](mock: EasyMockMock[A]): Task[A] = mock match {
    case StandardMock(cls) => Task(EasyMock.mock[A](cls))
  }

  private def buildExpectations[A, B, E <: HList](mock: A, expectations: Expectation[A, B] :: E) =
    ???

  //    ZIO.foreach(expectations)(expectation => Task(EasyMock.expect(expectations.head.expectation(mock))))

  def processSetter[A](a: IExpectationSetters[A], setter: ExpectationSetter[A]): Task[IExpectationSetters[A]] =
    Task.effect(
      setter match {
        case AndReturn(value) => a.andReturn(value)
      }
    )


  private def replayZio[A <: AnyRef](mock: A): Task[A] =
    ZIO.effect(EasyMock.replay(mock)).as(mock)

  private def verifyZio[A <: AnyRef](mock: A): Task[A] =
    ZIO.effect(EasyMock.verify(mock)).as(mock)
}

object EasyMockMockTestApp extends App {
  case class TestClass(s: String, n: Int)


  val mock = StandardMock[TestClass](TestClass.getClass)
  val m = MockWithExpectations(mock, HNil)
    .expect(_.s)(AndReturn("String"))
    .expect(_.s)(AndReturn("Bla"))
    .expect(_.n)(AndReturn(100))
    val derived = ExpectationBuilder.deriveExpectationBuilder[TestClass, Int,
     Expectation[TestClass, String] :: HNil.type]
  val derived2 = ExpectationBuilder.derive(m)

  val runtime = zio.Runtime.default
  val easyMock = EasyMock.mock[TestClass](classOf[TestClass])

  val result = derived2.build(m.expectations, easyMock)
val r =  runtime.unsafeRun(result)
  println(r)
}

