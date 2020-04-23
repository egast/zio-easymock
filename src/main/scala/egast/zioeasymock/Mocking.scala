package egast.zioeasymock

import egast.zioeasymock.Mocking._
import org.easymock.EasyMock
import zio._
import zio.test._

import scala.reflect.ClassTag

case class Mocks1[A: Tagged](mock1: Task[A]) {

  def expecting(expectation: A => Task[Any]): ExpectingMocks1[A] =
    ExpectingMocks1(this, expectation)

  private[zioeasymock] val asTuple: Task[Tuple1[A]] = mock1.map(Tuple1(_))
}

case class Mocks2[A: Tagged, B: Tagged](mock1: Task[A], mock2: Task[B]) {
  def expecting(expectation: (A, B) => Task[Any]): ExpectingMocks2[A, B] =
    ExpectingMocks2(this, expectation)

  private[zioeasymock] val asTuple: Task[(A, B)] = mock1.zip(mock2)
}

case class Mocks3[A: Tagged, B: Tagged, C: Tagged](mock1: Task[A], mock2: Task[B], mock3: Task[C]) {
  def expecting(expectation: (A, B, C) => Task[Any]): ExpectingMocks3[A, B, C] =
    ExpectingMocks3(this, expectation)

  private[zioeasymock] val asTuple: Task[(A, B, C)] = for {
    m1 <- mock1
    m2 <- mock2
    m3 <- mock3
  } yield (m1, m2, m3)
}

case class ExpectingMocks1[A: Tagged](mocks: Mocks1[A], expectation: A => Task[Any]) {
  def whenExecuting[R, E](assertion: A => ZIO[R, E, TestResult]): ZIO[R, E, TestResult] =
    testWhenExecuting[Tuple1[A], R, E](a => expectation(a._1), a => assertion(a._1))(mocks.asTuple)

  def whenExecutingAsLayer[R, E](assertion: ULayer[Has[A]] => ZIO[R, E, TestResult]): ZIO[R, E, TestResult] =
    whenExecuting(a => assertion(ZLayer.succeed(a)))
}

case class ExpectingMocks2[A: Tagged, B: Tagged](mocks: Mocks2[A, B], expectation: (A, B) => Task[Any]) {
  def whenExecuting[R, E](assertion: (A, B) => ZIO[R, E, TestResult]): ZIO[R, E, TestResult] =
    testWhenExecuting(expectation.tupled, assertion.tupled)(mocks.asTuple)

  def whenExecutingAsLayer[R, E](assertion: ULayer[Has[A] with Has[B]] => ZIO[R, E, TestResult]): ZIO[R, E, TestResult] =
    whenExecuting { case (a, b) => assertion(ZLayer.succeedMany(Has.allOf[A, B](a, b))) }
}

case class ExpectingMocks3[A: Tagged, B: Tagged, C: Tagged](mocks: Mocks3[A, B, C], expectation: (A, B, C) => Task[Any]) {
  def whenExecuting[R, E](assertion: (A, B, C) => ZIO[R, E, TestResult]): ZIO[R, E, TestResult] =
    testWhenExecuting(expectation.tupled, assertion.tupled)(mocks.asTuple)

  def whenExecutingAsLayer[R, E](assertion: ULayer[Has[A] with Has[B]] => ZIO[R, E, TestResult]): ZIO[R, E, TestResult] =
    whenExecuting { case (a, b, c) => assertion(ZLayer.succeedMany(Has.allOf[A, B, C](a, b, c))) }
}

private[zioeasymock] object Mocking {

  def mockZio[A](implicit cls: ClassTag[A]): Task[A] =
    ZIO.effect(EasyMock.mock[A](cls.runtimeClass))

  def mockStrictZio[A](implicit cls: ClassTag[A]): Task[A] =
    ZIO.effect(EasyMock.strictMock[A](cls.runtimeClass))

  def replayZio[A](mock: A): Task[A] =
    ZIO.effect(EasyMock.replay(mock)).as(mock)

  def verifyZio[A](mock: A): Task[A] =
    ZIO.effect(EasyMock.verify(mock)).as(mock)

  def testWhenExecuting[M <: Product : ClassTag, R, E]
  (expectation: M => Task[Any], assertion: M => ZIO[R, E, TestResult])
  (mocks: Task[M]) =
    mocks.foldM(
      t => ZIO.effectTotal(assert(t)(assertMockResult)),
      allMocks => testMocks(expectation, assertion)(allMocks)
    )

  private def testMocks[M <: Product : ClassTag, R, E](expectation: M => Task[Any], assertion: M => ZIO[R, E, TestResult])(allMocks: M): ZIO[R, E, TestResult] =
    (for {
      _ <- Task(expectation(allMocks)).flatten
      allMocksList = allMocks.productIterator.toList
      replayResult <- ZIO.foreach(allMocksList)(testReplay).map(_.reduce(_ && _))
      testResult <- assertion(allMocks)
        .catchAllCause(c => ZIO.fail(c.defects.headOption.getOrElse(new AssertionError("unknown mocking error"))))

      verifyResult <- ZIO.foreach(allMocksList)(testVerify).map(_.reduce(_ && _))
    } yield replayResult && testResult && verifyResult)
      .fold(t => assert(t)(assertMockResult), v => v)

  private def testReplay[M](m: M): IO[Throwable, TestResult] =
    replayZio(m).as(assertCompletes)

  private def testVerify[M](m: M): IO[Throwable, TestResult] =
    verifyZio(m).as(assertCompletes)

  private def assertMockResult: Assertion[Throwable] =
    Assertion.assertion("mocking expectations")()(_ => false)

}