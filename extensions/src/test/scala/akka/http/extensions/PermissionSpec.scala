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
import org.scalatest.{WordSpec}
import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration._
import com.github.t3hnar.bcrypt._


class PermissionSpec extends ExtensionsTestBase with PermissionControllers
{
  "application with authorization" should{
    "check permissions" in {
      val loginController = new TestLoginController
      val sessionController = new TestSessionController
      import akka.http.scaladsl.server._
      val logins = new Logins(loginController, sessionController)
      val routes = logins.routes
      Put("/users/register?username=anton&password=test2test&email=antonkulaga@gmail.com") ~> Route.seal(routes) ~> check {
        status shouldEqual OK
      }

      val permission = new Permissions(sessionController,loginController)

      Put("/add/drug?name=potion1") ~> permission.routes ~> check {
        rejection
      }

      Put("/users/login?email=antonkulaga@gmail.com&password=test2test") ~> logins.routes ~> check {
        status shouldEqual OK
      }

      val antonToken = sessionController.tokenByUsername(anton.username).get

      Put("/add/drug?name=potion1&kind=user") ~> Cookie("X-Token" -> antonToken) ~> permission.routes ~> check {
        status shouldEqual OK
        responseAs[String] shouldEqual s"drug potion1 added!"
        permission.drugs.contains("potion1") shouldEqual true
      }

      Put("/add/drug?name=potion2&kind=user") ~> permission.routes ~> check {
        rejection
      }

      val antonR = sessionController.userByToken(antonToken).get //get user with hashed password
      permission.add2realm(antonR,VIPRealm)

      permission.checkRights(antonR,SpecialRealm) shouldEqual false
      permission.checkRights(antonR,VIPRealm) shouldEqual true


      Put(s"/users/register?username=${liz.username}&password=${liz.password}&email=${liz.email}") ~> Route.seal(routes) ~> check {
        status shouldEqual OK
      }

      val lizToken =  sessionController.tokenByUsername(liz.username).get
      val lizR = sessionController.userByToken(lizToken).get

      permission.add2realm(lizR,SpecialRealm)
      permission.checkRights(lizR,SpecialRealm) shouldEqual true
      permission.checkRights(lizR,VIPRealm) shouldEqual false


    Put("/add/drug?name=potion2&kind=special") ~> Cookie("X-Token" -> antonToken) ~> permission.routes ~> check {
      permission.drugs.contains("potion2") shouldEqual false
      rejection
    }
    Put("/add/drug?name=potion2&kind=special") ~> Cookie("X-Token" -> lizToken) ~> permission.routes ~> check {
      status shouldEqual OK
      permission.drugs.contains("potion2") shouldEqual true
      responseAs[String] shouldEqual s"drug potion2 added!"
    }

    Put("/add/drug?name=potion3&kind=vip") ~> Cookie("X-Token" -> lizToken) ~> permission.routes ~> check {
    //  permission.drugs.contains("potion3") shouldEqual false
      rejection
    }
    Put("/add/drug?name=potion3&kind=vip") ~> Cookie("X-Token" -> antonToken) ~> permission.routes ~> check {
      status shouldEqual OK
      permission.drugs.contains("potion3") shouldEqual true
      responseAs[String] shouldEqual s"drug potion3 added!"
   }
  }
}

}
