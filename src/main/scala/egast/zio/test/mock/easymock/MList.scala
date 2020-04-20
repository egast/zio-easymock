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
  type Head = H
  type Tail <: MList

  def head(l: L): H

  def tail(l: L): Tail
}


object MHead {

  implicit def deriveHead[H, T <: MList]: MHead[H, H :: T] = new MHead[H, H :: T] {
    override type Tail = T

    override def head(l: H :: T): H = l.head

    override def tail(l: H :: T): T = l.tail
  }
}

case class MockHelper[H, MT <: MList, E <: MList](mocks: H :: MT, expectations: E) {
  //  def expect(f: H => H): MockHelper[M, E] =

  def next[TH](implicit tHead: MHead[TH, MT]): MockHelper[tHead.Head, tHead.Tail, H :: E] =
    MockHelper[tHead.Head, tHead.Tail, H :: E](
      mocks = MCons(tHead.head(mocks.tail),
        tHead.tail(mocks.tail)),
      expectations = MCons(mocks.head, expectations)
    )

  //    MockHelper(current = head.head(mocks))
}

object MockListTestApp extends App {


  type MList = String :: Int :: MNil

  val mlist = "erik" :: 10 :: MNil
  val head = mlist.head
  val tail = mlist.tail

  val mockHelper: MockHelper[String, Int :: MNil, MNil.type] = MockHelper(mlist, MNil)
  val n1 = mockHelper.next
//  val n2 = n1.next
//  val n3 = n2.next
//  val n4 = mockHelper.next
}
