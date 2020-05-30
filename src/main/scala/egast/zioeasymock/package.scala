package egast

import egast.zioeasymock.Mock._
import org.easymock.{ EasyMock, IExpectationSetters }
import zio.{ Tag, Task }

import scala.reflect.ClassTag

package object zioeasymock {

  def createMock[A <: AnyRef: ClassTag: Tag]: Mock1[A] =
    zioeasymock.Mock1(mockZio[A])

  def createMock[A <: AnyRef: ClassTag: Tag, B <: AnyRef: ClassTag: Tag]: Mock2[A, B] = Mock2(mockZio[A], mockZio[B])

  def createMock[A <: AnyRef: ClassTag: Tag, B <: AnyRef: ClassTag: Tag, C <: AnyRef: ClassTag: Tag]: Mock3[A, B, C] =
    Mock3(mockZio[A], mockZio[B], mockZio[C])

  def createMock[
    A <: AnyRef: ClassTag: Tag,
    B <: AnyRef: ClassTag: Tag,
    C <: AnyRef: ClassTag: Tag,
    D <: AnyRef: ClassTag: Tag
  ]: Mock4[A, B, C, D] =
    Mock4(mockZio[A], mockZio[B], mockZio[C], mockZio[D])

  def createStrictMock[A <: AnyRef: ClassTag: Tag]: Mock1[A] =
    zioeasymock.Mock1(mockStrictZio[A])

  def createStrictMock[A <: AnyRef: ClassTag: Tag, B <: AnyRef: ClassTag: Tag]: Mock2[A, B] =
    Mock2(mockStrictZio[A], mockStrictZio[B])

  def createStrictMock[A <: AnyRef: ClassTag: Tag, B <: AnyRef: ClassTag: Tag, C <: AnyRef: ClassTag: Tag]
    : Mock3[A, B, C] =
    Mock3(mockStrictZio[A], mockStrictZio[B], mockStrictZio[C])

  def createStrictMock[
    A <: AnyRef: ClassTag: Tag,
    B <: AnyRef: ClassTag: Tag,
    C <: AnyRef: ClassTag: Tag,
    D <: AnyRef: ClassTag: Tag
  ]: Mock4[A, B, C, D] =
    Mock4(mockStrictZio[A], mockStrictZio[B], mockStrictZio[C], mockStrictZio[D])

  def expecting[A <: AnyRef: ClassTag: Tag](
    expectation: A => Task[Any]
  ): ExpectingMock1[A] =
    createMock[A].expecting(expectation)

  def expecting[A <: AnyRef: ClassTag: Tag, B <: AnyRef: ClassTag: Tag](
    expectation: (A, B) => Task[Any]
  ): ExpectingMock2[A, B] = createMock[A, B].expecting(expectation)

  def expecting[A <: AnyRef: ClassTag: Tag, B <: AnyRef: ClassTag: Tag, C <: AnyRef: ClassTag: Tag](
    expectation: (A, B, C) => Task[Any]
  ): ExpectingMock3[A, B, C] = createMock[A, B, C].expecting(expectation)

  def expect[A, B](A: => A)(
    expectationSetters: IExpectationSetters[A] => IExpectationSetters[B]
  ): Task[IExpectationSetters[B]] =
    Task(EasyMock.expect(A)).flatMap(v => Task(expectationSetters(v)))

}
