package egast.zio.test.mock.easymock

import egast.zio.test.mock.easymock.MList._

sealed trait MList extends Product with Serializable

case class MCons[+A, +B <: MList](head: A, tail: B) extends MList {

  def ::[H](h: H): H :: A :: B = MCons(h, this)

  def update[U](f: A => U): U :: B = this.copy(f(head))
}

sealed trait MNil extends MList {
  def ::[H](h: H): H :: MNil = MCons(h, this)
}

case object MNil extends MNil

object MList {
  type ::[+A, +B <: MList] = MCons[A, B]

}

trait Mock[A] {
  def expect(expectation: A): Expectation[A]
}

trait Expectation[A]

trait MHead[H, L <: MList] {
  type Head
//  type Tail <: MList

  def head(l: L): Head

//  def tail(l: L): Tail
}


object MHead {


  implicit def deriveHead[H, T <: MList]: MHead[H, H :: T] = new MHead[H, H :: T] {
    override type Head = H

    override def head(l: H :: T): H = l.head
  }

//  implicit def recurse[H, T <: MList, U]
//  (implicit st: MHead[U, T]): MHead[U, H :: T] =
//  new MHead[U, H :: T] {
//    override type Head = st.Head
//
//    override def head(l: H :: T): Head = st.head(l.tail)
//  }
}

trait MTail[T <: MList, L <:MList]{
  type Tail <: MList
  def tail(l:L): Tail
}

object MTail{

  implicit def deriveTail[T, L <: MList]: MTail[T, T :: L] = new MTail[T, T :: L] {
    override type Tail = L

    override def tail(l: T :: L): Tail = l.tail
  }

//  implicit def recurse[H, T <: MList, U]
//  (implicit st: MTail[U, T]): MTail[U, H :: T] =
//    new MTail[U, H :: T] {
//      override type Tail = st.Tail
//
//      override def tail(l: H :: T): st.Tail = st.tail(l.tail)
//    }
}

case class MockHelper[MT <: MList, E <: MList](mocks: MT, expectations: E) {
  //  def expect(f: H => H): MockHelper[M, E] =

  def next[T <: MList, TH](implicit tHead: MHead[TH, MT], tTail: MTail[T, MT]): MockHelper[tTail.Tail, ::[tHead.Head, E]] =
    MockHelper(
      mocks = tTail.tail(mocks),
      expectations = MCons(tHead.head(mocks), expectations)
    )

  //    MockHelper(current = head.head(mocks))
}

object MockListTestApp extends App {


  type MList = String :: Int :: MNil

  val mlist = "erik" :: 10 :: MNil
  val head = mlist.head
  val tail = mlist.tail

  val mockHelper: MockHelper[String :: Int :: MNil, MNil.type] = MockHelper(mlist, MNil)
  val n1 = mockHelper.next
//  val n2 = n1.next
  //  val n3 = n2.next
  //  val n4 = mockHelper.next
}
