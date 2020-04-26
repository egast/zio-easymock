package egast.zioeasymock

import egast.zioeasymock.HList._

sealed trait HList extends Product with Serializable

case class HCons[+A, +B <: HList](head: A, tail: B) extends HList {

  def ::[H](h: H): H :: A :: B = HCons(h, this)

  def update[U](f: A => U): U :: B = this.copy(f(head))
}

sealed trait HNil extends HList {
  def ::[H](h: H): H :: HNil = HCons(h, this)
}

case object HNil extends HNil

object HList {
  type ::[+A, +B <: HList] = HCons[A, B]

}

object HListTestApp extends App {

}
