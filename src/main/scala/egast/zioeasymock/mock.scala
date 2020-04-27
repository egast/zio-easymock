package egast.zioeasymock

import egast.zioeasymock.EasyMocksControl.EasyMocksControl
import org.easymock.EasyMock
import zio.{RIO, Tagged, Task, ZIO}

import scala.reflect.ClassTag

case class Mock1[A <: AnyRef : Tagged](mock1: Class[A]) {

  def expecting(expectation: A => Task[Any]): ExpectingMock1[A] =
    ExpectingMock1(this, expectation)

  private[zioeasymock] val asTuple: RIO[EasyMocksControl, Tuple1[A]] =
    EasyMocksControl.createMock(mock1).map(Tuple1(_))
}


case class Mock2[A <: AnyRef : Tagged, B <: AnyRef : Tagged](mock1: Class[A], mock2: Class[B]) {
  def expecting(expectation: (A, B) => Task[Any]): ExpectingMock2[A, B] =
    ExpectingMock2(this, expectation)

  private[zioeasymock] val asTuple: RIO[EasyMocksControl, (A, B)] =
    EasyMocksControl.createMock(mock1).zip(EasyMocksControl.createMock(mock2))
}

case class Mock3[A <: AnyRef : Tagged, B <: AnyRef : Tagged, C <: AnyRef : Tagged](mock1: Class[A], mock2: Class[B], mock3: Class[C]) {
  def expecting(expectation: (A, B, C) => Task[Any]): ExpectingMock3[A, B, C] =
    ExpectingMock3(this, expectation)

  private[zioeasymock] val asTuple: RIO[EasyMocksControl, (A, B, C)] = for {
    m1 <- EasyMocksControl.createMock(mock1)
    m2 <- EasyMocksControl.createMock(mock2)
    m3 <- EasyMocksControl.createMock(mock3)
  } yield (m1, m2, m3)
}

object Mock {

  def mockZio[A <: AnyRef](implicit cls: ClassTag[A]): Task[A] =
    ZIO.effect(EasyMock.mock[A](cls.runtimeClass))

  def mockStrictZio[A <: AnyRef](implicit cls: ClassTag[A]): Task[A] =
    ZIO.effect(EasyMock.strictMock[A](cls.runtimeClass))
}