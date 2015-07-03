package akka.http.extensions

import java.util.UUID

import akka.http.extensions.security._
import akka.http.extensions.stubs.{InMemorySessionController, InMemoryLoginController}
import akka.http.scaladsl.model.{DateTime, StatusCodes}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.headers.{HttpCookie, Cookie, `Set-Cookie`}
import akka.http.scaladsl.server._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.denigma.preview.routes.Registration
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration._
import com.github.t3hnar.bcrypt._

trait Controllers {

  class TestLoginController extends  InMemoryLoginController{

    def checkPassword(username:String,password:String): Boolean = usersByName.get(username) match{
      case Some(u)=> password.isBcrypted(u.password)
      case None => false
    }
    def register(user:LoginInfo): Future[RegistrationResult] = this.register(user.username,user.password,user.email)
  }

  class TestSessionController extends InMemorySessionController

  class Permissions(sessionController: TestSessionController) extends AuthDirectives with Directives//to test permissions
  {
    case object OtherRealm extends Realm

    var drugs:Set[String] = Set.empty

     val permissions = new mutable.HashMap[Realm, mutable.Set[LoginInfo]] with mutable.MultiMap[Realm, LoginInfo]

    def checkRights(user:LoginInfo,realm:Realm):Boolean = permissions.get(realm).contains(user)
    def add2realm(user:LoginInfo,realm: Realm) ={
      permissions.addBinding(realm,user)
    }

    def removeFromRealm(user:LoginInfo,realm: Realm) ={
      permissions.removeBinding(realm,user)
    }

    def routes: Route =
      pathPrefix("add") {
        pathPrefix("drug") {
          put
          {
            parameter("name"){name=>
              authenticate(sessionController.userByToken _){ user=>
                authorize(user,UserRealm,checkRights _)
                {
                  drugs = drugs + name
                  complete("drug added!")
                }
              }
            }
          }

        }
      }
  }

  class Logins(loginController: TestLoginController,sessionController: TestSessionController) extends Registration(
      loginController.loginByName,
      loginController.loginByEmail,
      loginController.register,
      sessionController.userByToken,
      sessionController.makeToken
    )


}

class SecuritySpec  extends WordSpec
  with Matchers
  with Directives
  with ScalaFutures
  with ScalatestRouteTest
  with Controllers
{

  val anton = LoginInfo("anton","test2test","antonkulaga@gmail.com")
  val paul = LoginInfo("paul","test2paul","paul@gmail.com")
  val liz = LoginInfo("liz","test2liz","liz@gmail.com")


  val timeout = 500 millis
  implicit override val patienceConfig = new PatienceConfig(timeout)


  "authorization" should {
    "encode passwords with bcrypt" in {
      val loginController = new  TestLoginController
      import loginController._
      val (a,s,x) = (
        register("anton","pass1","antonku@gmail.com") ,
        register("sasha","pass2","sasha@gmail.com") ,
        register("xenia","pass3","xenia@gmail.com")
        )
      checkPassword("anton","pass2") shouldEqual false
      checkPassword("anton","pass1") shouldEqual true
      checkPassword("sasha","pass2") shouldEqual true
      checkPassword("sasha","pass3") shouldEqual false
      checkPassword("karmen","pass3") shouldEqual false
      clean()
    }

    "be able to encode/decode with AES" in {
      val (one,two,three) = ("one","two","three")
      val key = "hello encryption!!!"
      val wrongKey = "I am wrong"

      val (oneEn,twoEn,threeEn) = (AES.encrypt(one,key),AES.encrypt(two,key),AES.encrypt(three,key))
      AES.decrypt(oneEn,key) shouldEqual one
      AES.decrypt(twoEn,key) shouldEqual two
      AES.decrypt(threeEn,key) shouldEqual three
      assert(AES.decrypt(threeEn,key) != one)
      assert(AES.decrypt(threeEn,wrongKey) != three)
    }


    "login by name with cookies" in {
      val loginController = new TestLoginController
      loginController.register(anton)
      val sessionController = new TestSessionController
      val logins = new Logins(loginController,sessionController)
      Put("/users/login?username=anton&password=test2test") ~> logins.routes ~> check{
                val resp =  responseAs[String]
                responseAs[String] shouldEqual "The user anton was logged in"
                val tokOpt = sessionController.tokenByUsername("anton")
                tokOpt.isDefined shouldEqual true
                val tok = tokOpt.get
                val hop: Option[`Set-Cookie`] = header[`Set-Cookie`]
                hop.isDefined shouldEqual(true)
                val h = hop.get.cookie
                h.name shouldEqual  "token"
                h.pair().value shouldEqual tok
      }
    }



    "register" in {
      val loginController = new TestLoginController
      //loginController.register(anton)
      val sessionController = new TestSessionController
      val logins = new Logins(loginController,sessionController)
      import akka.http.scaladsl.server.Route
       Put("/users/register?username=anton&password=test2test&email=antonkulaga@gmail.com") ~> Route.seal(
        logins.routes) ~> check{
        val resp =  responseAs[String]
        responseAs[String] shouldEqual "The user anton has been registered"
        val tokOpt = sessionController.tokenByUsername("anton")
        tokOpt.isDefined shouldEqual true
        val tok = tokOpt.get
        val hop: Option[`Set-Cookie`] = header[`Set-Cookie`]
        hop.isDefined shouldEqual(true)
        val h = hop.get.cookie
        h.name shouldEqual  "token"
        h.pair().value shouldEqual tok
      }
    }



  "register, login and logout" in {
      val loginController = new TestLoginController
      val sessionController = new TestSessionController
      val logins = new Logins(loginController,sessionController)
      import akka.http.scaladsl.server._

      val routes = logins.routes
      Put("/users/login?username=anton&password=test2test") ~>  Route.seal(routes) ~> check{
        val resp =  responseAs[String]
        status === StatusCodes.Forbidden
      }

    Get("/users/status") ~>  Route.seal(routes) ~> check{
        status === StatusCodes.Forbidden
      }

    Put("/users/register?username=anton&password=test2test&email=antonkulaga@gmail.com") ~> Route.seal(routes) ~> check {
        val resp = responseAs[String]
        responseAs[String] shouldEqual "The user anton has been registered"
      }

    Put("/users/login?username=anton&password=test2test") ~> routes ~> check{
      val tokOpt = sessionController.tokenByUsername("anton")
      tokOpt.isDefined shouldEqual true
      val tok = tokOpt.get
      val resp =  responseAs[String]
      responseAs[String] shouldEqual "The user anton was logged in"
      val hop: Option[`Set-Cookie`] = header[`Set-Cookie`]
      hop.isDefined shouldEqual(true)
      val h = hop.get.cookie
      h.name shouldEqual  "token"
      h.pair().value shouldEqual tok
    }

    val tok = sessionController.tokenByUsername("anton").get
    Get("/users/status") ~> Cookie("token" -> tok) ~>  routes ~> check{ //intellij mistakenly highlights it with red
      responseAs[String] shouldEqual "anton"
    }

    Put("/users/logout") ~> Cookie("token" -> tok) ~>  routes ~> check{ //intellij mistakenly highlights it with red
      responseAs[String] shouldEqual "The user was logged out"
      header[`Set-Cookie`] shouldEqual Some(`Set-Cookie`(HttpCookie("token", value = "deleted", expires = Some(DateTime.MinValue))))
    }
  }

    "check permissions" in {
      val loginController = new TestLoginController
      val sessionController = new TestSessionController
      import akka.http.scaladsl.server._
      val logins= new Logins(loginController,sessionController)
      val routes = logins.routes
      Put("/users/register?username=anton&password=test2test&email=antonkulaga@gmail.com") ~> Route.seal(routes) ~> check {
        val resp = responseAs[String]
        responseAs[String] shouldEqual "The user anton has been registered"
      }

      Put("/users/login?email=antonkulaga@gmail.com&password=test2test") ~> logins.routes ~> check{
        val resp =  responseAs[String]
        responseAs[String] shouldEqual "The user anton was logged in"
      }

      val permission = new Permissions(sessionController)

    }

  }

}
