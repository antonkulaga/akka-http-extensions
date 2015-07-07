package akka.http.extensions

import akka.http.extensions.utils.BiMap
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
class UtilsSpec extends WordSpec with Matchers with ScalaFutures
{

  "bimap" should {

    val mp =  BiMap(1->"one",2->"two",3->"three")

    "be identical" in {
      mp(1) shouldEqual("one")
      mp.inverse("one") shouldEqual 1

    }


    "add new elements" in {
      val mp2: BiMap[Int, String] = mp + (4->"four")
      mp2(4) shouldEqual "four"
      mp2(1) shouldEqual "one"
      val inv = mp2.inverse

      inv("four") shouldEqual 4
      inv("one") shouldEqual 1
    }


    "delete new elements" in {
      val mp3 = mp - 2
      mp3.contains(2) shouldEqual false
      mp3.contains(3) shouldEqual true
      val inv = mp3.inverse
      inv.contains("two")  shouldEqual false
      inv.contains("two")  shouldEqual false
    }

  }
}
