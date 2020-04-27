package egast

import egast.zioeasymock.Mock._
import org.easymock.{EasyMock, IExpectationSetters}
import zio.{Tagged, Task}

import scala.reflect.ClassTag

package object zioeasymock {

  def createMock[A <: AnyRef : ClassTag : Tagged]: Mock1[A] = zioeasymock.Mock1(mockZio[A])

  def createMock[A <: AnyRef : ClassTag : Tagged, B <: AnyRef : ClassTag : Tagged]: Mock2[A, B] = Mock2(mockZio[A], mockZio[B])

  def createMock[A <: AnyRef : ClassTag : Tagged, B <: AnyRef : ClassTag : Tagged, C <: AnyRef : ClassTag : Tagged]: Mock3[A, B, C] =
    Mock3(mockZio[A], mockZio[B], mockZio[C])

  def createStrictMock[A <: AnyRef : ClassTag : Tagged]: Mock1[A] =
    zioeasymock.Mock1(mockStrictZio[A])

  def createStrictMock[A <: AnyRef : ClassTag : Tagged, B <: AnyRef : ClassTag : Tagged]: Mock2[A, B] =
    Mock2(mockStrictZio[A], mockStrictZio[B])

  def createStrictMock[A <: AnyRef : ClassTag : Tagged, B <: AnyRef : ClassTag : Tagged, C <: AnyRef : ClassTag : Tagged]: Mock3[A, B, C] =
    Mock3(mockStrictZio[A], mockStrictZio[B], mockStrictZio[C])

  def expecting[A <: AnyRef : ClassTag : Tagged](expectation: A => Task[Any]): ExpectingMock1[A] =
    createMock[A].expecting(expectation)

  def expecting[A <: AnyRef : ClassTag : Tagged, B <: AnyRef : ClassTag : Tagged]
  (expectation: (A, B) => Task[Any]
  ): ExpectingMock2[A, B] = createMock[A, B].expecting(expectation)

  def expecting[A <: AnyRef : ClassTag : Tagged, B <: AnyRef : ClassTag : Tagged, C <: AnyRef : ClassTag : Tagged]
  (expectation: (A, B, C) => Task[Any]
  ): ExpectingMock3[A, B, C] = createMock[A, B, C].expecting(expectation)

  def expect[A, B](A: => A)(expectationSetters: IExpectationSetters[A] => IExpectationSetters[B]): Task[IExpectationSetters[B]] =
    Task(EasyMock.expect(A)).flatMap(v => Task(expectationSetters(v)))

}