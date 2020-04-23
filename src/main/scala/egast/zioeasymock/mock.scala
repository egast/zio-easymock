package egast.zioeasymock

import org.easymock.EasyMock
import zio.{Tagged, Task, ZIO}

import scala.reflect.ClassTag

case class Mock1[A <: AnyRef : Tagged](mock1: Task[A]) {

  def expecting(expectation: A => Task[Any]): ExpectingMock1[A] =
    ExpectingMock1(this, expectation)

  private[zioeasymock] val asTuple: Task[Tuple1[A]] = mock1.map(Tuple1(_))
}


case class Mock2[A <: AnyRef : Tagged, B <: AnyRef : Tagged](mock1: Task[A], mock2: Task[B]) {
  def expecting(expectation: (A, B) => Task[Any]): ExpectingMock2[A, B] =
    ExpectingMock2(this, expectation)

  private[zioeasymock] val asTuple: Task[(A, B)] = mock1.zip(mock2)
}

case class Mock3[A <: AnyRef : Tagged, B <: AnyRef : Tagged, C <: AnyRef : Tagged](mock1: Task[A], mock2: Task[B], mock3: Task[C]) {
  def expecting(expectation: (A, B, C) => Task[Any]): ExpectingMock3[A, B, C] =
    ExpectingMock3(this, expectation)

  private[zioeasymock] val asTuple: Task[(A, B, C)] = for {
    m1 <- mock1
    m2 <- mock2
    m3 <- mock3
  } yield (m1, m2, m3)
}

object Mock {

  def mockZio[A <: AnyRef](implicit cls: ClassTag[A]): Task[A] =
    ZIO.effect(EasyMock.mock[A](cls.runtimeClass))

  def mockStrictZio[A <: AnyRef](implicit cls: ClassTag[A]): Task[A] =
    ZIO.effect(EasyMock.strictMock[A](cls.runtimeClass))
}