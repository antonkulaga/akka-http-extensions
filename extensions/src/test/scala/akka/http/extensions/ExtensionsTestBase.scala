package akka.http.extensions

import akka.http.extensions.security._
import akka.http.scaladsl.server._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import scala.concurrent.duration._

class ExtensionsTestBase extends WordSpec
  with Directives
  with ScalaFutures
  with ScalatestRouteTest
  with Matchers
{

  val anton = LoginInfo("anton", "test2test", "antonkulaga@gmail.com")
  val paul = LoginInfo("paul", "test2paul", "paul@gmail.com")
  val liz = LoginInfo("liz", "test2liz", "liz@gmail.com")
  
  val timeout = 500 millis

}
