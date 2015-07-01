package akka.http.extensions.stubs

import akka.http.extensions.security.{LoginInfo, LoginResult, RegistrationResult}
import com.github.t3hnar.bcrypt._
import scala.concurrent.Future

trait FutureLoginController{
  
  def loginByName(username:String,passw:String):Future[LoginResult]

  def loginByEmail(username:String,passw:String):Future[LoginResult]

  def register(username:String,passw:String,email:String):Future[RegistrationResult]

  def withHash(registerInfo:LoginInfo) = registerInfo.copy(password = registerInfo.password.bcrypt)

}

trait SessionController{

  def withToken(user:LoginInfo):Future[String]

  def userByToken(token: String): Option[LoginInfo]

  def tokenByUser(user: LoginInfo): Option[String]

  def tokenByUsername(username:String): Option[String]

}