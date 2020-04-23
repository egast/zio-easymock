package egast

import egast.zioeasymock.Mock._
import org.easymock.{EasyMock, IExpectationSetters}
import zio.{Tagged, Task}

import scala.reflect.ClassTag

package object zioeasymock {

  def createMock[A: ClassTag : Tagged]: Mock1[A] = zioeasymock.Mock1(mockZio[A])

  def createMock[A: ClassTag : Tagged, B: ClassTag : Tagged]: Mock2[A, B] = Mock2(mockZio[A], mockZio[B])

  def createMock[A: ClassTag : Tagged, B: ClassTag : Tagged, C: ClassTag : Tagged]: Mock3[A, B, C] =
    Mock3(mockZio[A], mockZio[B], mockZio[C])

  def createStrictMock[A: ClassTag : Tagged]: Mock1[A] =
    zioeasymock.Mock1(mockStrictZio[A])

  def createStrictMock[A: ClassTag : Tagged, B: ClassTag : Tagged]: Mock2[A, B] =
    Mock2(mockStrictZio[A], mockStrictZio[B])

  def createStrictMock[A: ClassTag : Tagged, B: ClassTag : Tagged, C: ClassTag : Tagged]: Mock3[A, B, C] =
    Mock3(mockStrictZio[A], mockStrictZio[B], mockStrictZio[C])

  def expecting[A: ClassTag : Tagged](expectation: A => Task[Any]): ExpectingMock1[A] =
    createMock[A].expecting(expectation)

  def expecting[A: ClassTag : Tagged, B: ClassTag : Tagged]
  (expectation: (A, B) => Task[Any]
  ): ExpectingMock2[A, B] = createMock[A, B].expecting(expectation)

  def expecting[A: ClassTag : Tagged, B: ClassTag : Tagged, C: ClassTag : Tagged]
  (expectation: (A, B, C) => Task[Any]
  ): ExpectingMock3[A, B, C] = createMock[A, B, C].expecting(expectation)

  def expect[A](A: => A): Task[IExpectationSetters[A]] =
    Task(EasyMock.expect(A))

}