package egast.zioeasymock

import org.easymock.{EasyMock, IMocksControl}
import zio._

object EasyMocksControl {
  type EasyMocksControl = Has[Service]

  class Service(control: Ref[IMocksControl]) {
    def createMock[A](cls: Class[A]): Task[A] = control.get.flatMap(c => Task(c.createMock[A, A](cls)))

    def replay: Task[Unit] = control.get.flatMap(c => Task(c.replay()))

    def verify: Task[Unit] = control.get.flatMap(c => Task(c.verify()))
  }

  def createMock[A](cls: Class[A]): RIO[EasyMocksControl, A] =
    ZIO.accessM(_.get[Service].createMock[A](cls))

  def replay: RIO[EasyMocksControl, Unit] =
    ZIO.accessM(_.get[Service].replay)

  def verify: RIO[EasyMocksControl, Unit] =
    ZIO.accessM(_.get[Service].verify)

  lazy val standard: ZLayer[Any, Throwable, Has[Service]] = ZLayer.fromEffect(
    make(EasyMock.createControl())
  )

  lazy val strict: ZLayer[Any, Throwable, Has[Service]] = ZLayer.fromEffect(
    make(EasyMock.createStrictControl())
  )

  private def make(mocksControl: => IMocksControl) =
    for {
      c <- Task.effect(mocksControl)
      control <- Ref.make(c)
    } yield new Service(control)
}