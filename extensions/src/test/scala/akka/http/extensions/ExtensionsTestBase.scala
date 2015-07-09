package akka.http.extensions

import java.util.UUID

import akka.http.extensions.security._
import akka.http.extensions.stubs.{InMemorySessionController, InMemoryLoginController}
import akka.http.scaladsl.model.{DateTime, StatusCodes}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.headers.{HttpCookie, Cookie, `Set-Cookie`}
import akka.http.scaladsl.server._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration._
import com.github.t3hnar.bcrypt._

class ExtensionsTestBase extends WordSpec
  with Matchers
  with Directives
  with ScalaFutures
  with ScalatestRouteTest
{

  val anton = LoginInfo("anton","test2test","antonkulaga@gmail.com")
  val paul = LoginInfo("paul","test2paul","paul@gmail.com")
  val liz = LoginInfo("liz","test2liz","liz@gmail.com")
  
  val timeout = 500 millis

}
