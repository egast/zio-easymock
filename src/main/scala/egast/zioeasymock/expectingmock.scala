package egast.zioeasymock

import egast.zioeasymock.EasyMocksControl.EasyMocksControl
import egast.zioeasymock.ExpectingMock._
import org.easymock.EasyMock
import zio._
import zio.test._

import scala.reflect.ClassTag

case class ExpectingMock1[A <: AnyRef : Tagged](mock: Mock1[A], expectation: A => Task[Any]) {
  def whenExecuting[R, E](assertion: A => ZIO[R, E, TestResult]): ZIO[EasyMocksControl with R, E, TestResult] =
    assertWhenExecuting[Tuple1[A], R, E](a => expectation(a._1), a => assertion(a._1))(mock.asTuple)

  def whenExecutingAsLayer[R, E](assertion: ULayer[Has[A]] => ZIO[R, E, TestResult]): ZIO[EasyMocksControl with R, E, TestResult] =
    whenExecuting(a => assertion(ZLayer.succeed(a)))
}

case class ExpectingMock2[A <: AnyRef : Tagged, B <: AnyRef : Tagged](mock: Mock2[A, B], expectation: (A, B) => Task[Any]) {
  def whenExecuting[R, E](assertion: (A, B) => ZIO[R, E, TestResult]): ZIO[EasyMocksControl with R, E, TestResult] =
    assertWhenExecuting(expectation.tupled, assertion.tupled)(mock.asTuple)

  def whenExecutingAsLayer[R, E](assertion: ULayer[Has[A] with Has[B]] => ZIO[R, E, TestResult]): ZIO[EasyMocksControl with R, E, TestResult] =
    whenExecuting { case (a, b) => assertion(ZLayer.succeedMany(Has.allOf[A, B](a, b))) }
}

case class ExpectingMock3[A <: AnyRef : Tagged, B <: AnyRef : Tagged, C <: AnyRef : Tagged](mock: Mock3[A, B, C], expectation: (A, B, C) => Task[Any]) {
  def whenExecuting[R, E](assertion: (A, B, C) => ZIO[R, E, TestResult]): ZIO[EasyMocksControl with R, E, TestResult] =
    assertWhenExecuting(expectation.tupled, assertion.tupled)(mock.asTuple)

  def whenExecutingAsLayer[R, E](assertion: ULayer[Has[A] with Has[B]] => ZIO[R, E, TestResult]): ZIO[EasyMocksControl with R, E, TestResult] =
    whenExecuting { case (a, b, c) => assertion(ZLayer.succeedMany(Has.allOf[A, B, C](a, b, c))) }
}

private[zioeasymock] object ExpectingMock {

  def assertWhenExecuting[M <: Product : ClassTag, R, E]
  (expectation: M => Task[Any], assertion: M => ZIO[R, E, TestResult])
  (mocks: RIO[EasyMocksControl,M]) =
    mocks.foldM(
      t => ZIO.effectTotal(assert(t)(failMockExpectation)),
      allMocks => assertMocked(expectation, assertion)(allMocks)
    )

  private def assertMocked[M <: Product : ClassTag, R, E](expectation: M => Task[Any], assertion: M => ZIO[R, E, TestResult])(allMocks: M): ZIO[EasyMocksControl with R, E, TestResult] =
    (for {
      _ <- Task(expectation(allMocks)).flatten
      allMocksList = allMocks.productIterator.toList.map(_.asInstanceOf[AnyRef])
      replayResult <- replayTestResult
      testResult <- assertion(allMocks)
        .catchAllCause(c => ZIO.fail(c.defects.headOption.getOrElse(new AssertionError("unknown mocking error"))))

      verifyResult <- verifyTestResult
    } yield replayResult && testResult && verifyResult)
      .fold(t => assert(t)(failMockExpectation), v => v)

  private def replayZio[A <: AnyRef](mock: A): Task[A] =
    ZIO.effect(EasyMock.replay(mock)).as(mock)

  private def verifyZio[A <: AnyRef](mock: A): Task[A] =
    ZIO.effect(EasyMock.verify(mock)).as(mock)

  private def replayTestResult: RIO[EasyMocksControl, TestResult] =
    EasyMocksControl.replay.as(assertCompletes)

  private def verifyTestResult: RIO[EasyMocksControl, TestResult] =
    EasyMocksControl.verify.as(assertCompletes)

  private def failMockExpectation: Assertion[Throwable] =
    Assertion.assertion("mocking expectations")()(_ => false)

}
