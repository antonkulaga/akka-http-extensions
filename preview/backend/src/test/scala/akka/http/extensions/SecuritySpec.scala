package akka.http.extensions

import akka.http.extensions.security.{AES, RegistrationResult, LoginInfo}
import akka.http.extensions.stubs.{InMemorySessionController, InMemoryLoginController}
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.denigma.preview.routes.Registration
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import com.github.t3hnar.bcrypt._
import scala.concurrent.Future
import scala.concurrent.duration._

class SecuritySpec  extends WordSpec with Matchers with Directives with ScalaFutures with  ScalatestRouteTest
{

  object loginController extends  InMemoryLoginController{

    def checkPassword(username:String,password:String): Boolean = users.get(username) match{
      case Some(u)=> password.isBcrypted(u.password)
      case None => false
    }
    def register(user:LoginInfo): Future[RegistrationResult] = this.register(user.username,user.email,user.password)
  }

  object sessionController extends InMemorySessionController

  object logins extends Registration(
    loginController.login,
    loginController.register,
    sessionController.withToken)
  import loginController._

  val timeout = 500 millis
  implicit override val patienceConfig = new PatienceConfig(timeout)

  "authorization" should {
    "encode passwords with bcrypt" in {
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
      loginController.clean()
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


    "generate cookies on login" in {
      loginController.register("anton","test","antonkulaga@gmail.com")
      Get("/users/login?username=anton&password=test") ~> logins.routes ~> check{
                val resp =  responseAs[String]
                println("RESP: "+resp)
                responseAs[String] shouldEqual "The user anton was logged in"
        /*       val tokOpt = sessionController.getToken("anton")
                tokOpt.isDefined shouldEqual true
                val tok = tokOpt.get
                val hop: Option[`Set-Cookie`] = header[`Set-Cookie`]
                hop.isDefined shouldEqual(true)
                val h = hop.get.cookie
                h.name shouldEqual  "token"
                h.content shouldEqual tok*/
        loginController.clean()
      }
    }



  }

}
