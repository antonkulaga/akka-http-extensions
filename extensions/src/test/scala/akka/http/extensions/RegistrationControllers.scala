package akka.http.extensions

import akka.http.extensions.security._
import akka.http.extensions.stubs.{InMemoryLoginController, InMemorySessionController}
import com.github.t3hnar.bcrypt._

import scala.concurrent.Future



trait RegistrationControllers {

  class TestLoginController extends  InMemoryLoginController{

    def checkPassword(username:String,password:String): Boolean = usersByName.get(username) match{
      case Some(u)=> password.isBcrypted(u.password)
      case None => false
    }
    def register(user:LoginInfo): Future[RegistrationResult] = this.register(user.username,user.password,user.email)
  }

  class TestSessionController extends InMemorySessionController

  class Logins(loginController: TestLoginController,sessionController: TestSessionController) extends DummyRegistration(
      loginController.loginByName,
      loginController.loginByEmail,
      loginController.register,
      sessionController.userByToken,
      sessionController.makeToken
    )


}
