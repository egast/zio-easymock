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

  lazy val live: ZLayer[Any, Throwable, Has[Service]] = ZLayer.fromEffect(
    for {
      c <- Task(EasyMock.createControl())
      control <- Ref.make(c)
    } yield new Service(control)
  )

  lazy val strict: ZLayer[Any, Throwable, Has[Service]] = ZLayer.fromEffect(
    for {
      c <- Task(EasyMock.createStrictControl())
      control <- Ref.make(c)
    } yield new Service(control)
  )
}
