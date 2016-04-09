package akka.http.extensions.utils

import scala.collection.immutable.Map

object BiMap {

  def apply[A,B](elems:(A,B)*): BiMap[A, B] ={
    val mp = Map(elems:_*)
    val inv = Map(elems.map{case (key,value)=>value->key}:_*)
    DirectBiMap[A,B](mp,inv)
  }

  def empty[A,B]: BiMap[A, B] = DirectBiMap(Map.empty[A,B],Map.empty[B,A])

  def apply[A,B](forward:Map[A,B], backward: Map[B,A]): BiMap[A,B] = DirectBiMap(forward, backward)

  def unapply[A,B](mp: BiMap[A,B]): Option[(Map[A,B],Map[B,A])] = Some((mp.forward, mp.backward))

  case class DirectBiMap[A,B](forward: Map[A,B], backward: Map[B,A]) extends BiMap[A,B]{
    lazy val inverse:BiMap[B,A] = InverseBiMap(this)
  }
  case class InverseBiMap[A,B](inverse: BiMap[B,A]) extends BiMap[A,B]{
    override def forward: Map[A, B] = inverse.backward

    override def backward: Map[B, A] = inverse.forward
  }

}


/**
 * This allows a bi-directional map to be created from any two maps.
 * These maps must be the inverse of each other to work.
 */
trait BiMap[A,B] extends Map[A,B] {
self=>

  def forward:Map[A,B]
  def backward:Map[B,A]
  def inverse:  BiMap[B,A]

  override def +[B1 >: B](kv: (A, B1)): BiMap[A,B1] = {
    val binv =  backward.toMap[B1,A] + (kv._2, kv._1).asInstanceOf[(B1,A)] //does not compile without this
    BiMap[A,B1](self.forward + kv, binv)
  }

  override def get(key: A): Option[B] = forward.get(key)

  override def iterator: Iterator[(A, B)] = forward.iterator

  def containsValue(value: B): Boolean = inverse.contains(value)

  override def -(key: A): BiMap[A, B] = self.forward.get(key) match {
    case Some(value)=>
      val b = self.backward-value
      BiMap[A,B](self.forward -key, b)
    case None=> this
  }
}
