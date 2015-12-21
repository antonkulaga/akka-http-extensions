package akka.http.extensions

import akka.http.extensions.security._
import akka.http.scaladsl.model.headers.{Cookie, HttpCookie, `Set-Cookie`}
import akka.http.scaladsl.model.{DateTime, StatusCodes}


class SecuritySpec extends ExtensionsTestBase with RegistrationControllers{


  "application with registration" should {

    "encode passwords with bcrypt" in {
      val loginController = new TestLoginController
      import loginController._
      val (a, s, x) = (
        register("anton", "pass1", "antonku@gmail.com"),
        register("sasha", "pass2", "sasha@gmail.com"),
        register("xenia", "pass3", "xenia@gmail.com")
        )
      checkPassword("anton", "pass2") shouldEqual false
      checkPassword("anton", "pass1") shouldEqual true
      checkPassword("sasha", "pass2") shouldEqual true
      checkPassword("sasha", "pass3") shouldEqual false
      checkPassword("karmen", "pass3") shouldEqual false
      clean()
    }

    "be able to encode/decode with AES" in {
      val (one, two, three) = ("one", "two", "three")
      val key = "hello encryption!!!"
      val wrongKey = "I am wrong"

      val (oneEn, twoEn, threeEn) = (AES.encrypt(one, key), AES.encrypt(two, key), AES.encrypt(three, key))
      AES.decrypt(oneEn, key) shouldEqual one
      AES.decrypt(twoEn, key) shouldEqual two
      AES.decrypt(threeEn, key) shouldEqual three
      assert(AES.decrypt(threeEn, key) != one)
      assert(AES.decrypt(threeEn, wrongKey) != three)
    }


    "login by name with cookies" in {
      val loginController = new TestLoginController
      loginController.register(anton)
      val sessionController = new TestSessionController
      val logins = new Logins(loginController, sessionController)
      Put("/users/login?username=anton&password=test2test") ~> logins.routes ~> check {
        val resp = responseAs[String]
        responseAs[String] shouldEqual "The user anton was logged in"
        val tokOpt = sessionController.tokenByUsername("anton")
        tokOpt.isDefined shouldEqual true
        val tok = tokOpt.get
        val hop: Option[`Set-Cookie`] = header[`Set-Cookie`]
        hop.isDefined shouldEqual (true)
        val h = hop.get.cookie
        h.name shouldEqual "X-Token"
        h.pair().value shouldEqual tok
      }
    }



    "register" in {
      val loginController = new TestLoginController
      //loginController.register(anton)
      val sessionController = new TestSessionController
      val logins = new Logins(loginController, sessionController)
      import akka.http.scaladsl.server.Route
      Put("/users/register?username=anton&password=test2test&email=antonkulaga@gmail.com") ~> Route.seal(
        logins.routes) ~> check {
        val resp = responseAs[String]
        responseAs[String] shouldEqual "The user anton has been registered"
        val tokOpt = sessionController.tokenByUsername("anton")
        tokOpt.isDefined shouldEqual true
        val tok = tokOpt.get
        val hop: Option[`Set-Cookie`] = header[`Set-Cookie`]
        hop.isDefined shouldEqual (true)
        val h = hop.get.cookie
        h.name shouldEqual "X-Token"
        h.pair().value shouldEqual tok
      }
    }



    "register, login and logout" in {
      val loginController = new TestLoginController
      val sessionController = new TestSessionController
      val logins = new Logins(loginController, sessionController)
      import akka.http.scaladsl.server._

      val routes = logins.routes
      Put("/users/login?username=anton&password=test2test") ~> Route.seal(routes) ~> check {
        val resp = responseAs[String]
        status === StatusCodes.Forbidden
      }

      Get("/users/status") ~> Route.seal(routes) ~> check {
        status === StatusCodes.Forbidden
      }

      Put("/users/register?username=anton&password=test2test&email=antonkulaga@gmail.com") ~> Route.seal(routes) ~> check {
        val resp = responseAs[String]
        responseAs[String] shouldEqual "The user anton has been registered"
      }

      Put("/users/login?username=anton&password=test2test") ~> routes ~> check {
        val tokOpt = sessionController.tokenByUsername("anton")
        tokOpt.isDefined shouldEqual true
        val tok = tokOpt.get
        val resp = responseAs[String]
        responseAs[String] shouldEqual "The user anton was logged in"
        val hop: Option[`Set-Cookie`] = header[`Set-Cookie`]
        hop.isDefined shouldEqual (true)
        val h = hop.get.cookie
        h.name shouldEqual "X-Token"
        h.pair().value shouldEqual tok
      }

      val tok = sessionController.tokenByUsername("anton").get
      Get("/users/status") ~> Cookie("X-Token" -> tok) ~> routes ~> check {
        //intellij mistakenly highlights it with red
        responseAs[String] shouldEqual "anton"
      }

      Put("/users/logout") ~> Cookie("X-Token" -> tok) ~> routes ~> check {
        //intellij mistakenly highlights it with red
        responseAs[String] shouldEqual "The user was logged out"
        header[`Set-Cookie`] shouldEqual Some(`Set-Cookie`(HttpCookie("X-Token", value = "deleted", path = Some("/"), expires = Some(DateTime.MinValue))))
      }
    }

  }
}
