package egast.zio.test.mock

import org.easymock.IExpectationSetters
import zio.{Tagged, Task}

import scala.reflect.ClassTag

package object easymock {

  def createMock[A: ClassTag : Tagged]: Mocks1[A] = Mocks1(Mocking.mockZio[A])

  def createMock[A: ClassTag : Tagged, B: ClassTag : Tagged]: Mocks2[A, B] = Mocks2(Mocking.mockZio[A], Mocking.mockZio[B])

  def createMock[A: ClassTag : Tagged, B: ClassTag : Tagged, C: ClassTag : Tagged]: Mocks3[A, B, C] =
    Mocks3(Mocking.mockZio[A], Mocking.mockZio[B], Mocking.mockZio[C])

  def createStrictMock[A: ClassTag : Tagged]: Mocks1[A] =
    Mocks1(Mocking.mockStrictZio[A])

  def createStrictMock[A: ClassTag : Tagged, B: ClassTag : Tagged]: Mocks2[A, B] =
    Mocks2(Mocking.mockStrictZio[A], Mocking.mockStrictZio[B])

  def createStrictMock[A: ClassTag : Tagged, B: ClassTag : Tagged, C: ClassTag : Tagged]: Mocks3[A, B, C] =
    Mocks3(Mocking.mockStrictZio[A], Mocking.mockStrictZio[B], Mocking.mockStrictZio[C])

  def expecting[A: ClassTag : Tagged](expectation: A => Task[IExpectationSetters[_]]): ExpectingMocks1[A] =
    createMock[A].expecting(expectation)

  def expecting[A: ClassTag : Tagged, B: ClassTag : Tagged]
  (expectation: (A, B) => Task[IExpectationSetters[_]]
  ): ExpectingMocks2[A, B] = createMock[A, B].expecting(expectation)

  def expecting[A: ClassTag : Tagged, B: ClassTag : Tagged, C: ClassTag : Tagged]
  (expectation: (A, B, C) => Task[IExpectationSetters[_]]
  ): ExpectingMocks3[A, B, C] = createMock[A, B, C].expecting(expectation)

  import org.easymock.EasyMock.expect

  def expectM[A](A: A): Task[IExpectationSetters[A]] =
    Task(expect(A))

}
