package akka.http.extensions.stubs

import akka.http.extensions.security._
import akka.http.extensions.utils.BiMap
import com.github.t3hnar.bcrypt._

import scala.concurrent.Future

/**
 * Inmemory login and registration controller, recommended mostly for testing
 */
class InMemoryLoginController extends FutureLoginController {

  var users:Map[String,LoginInfo] = Map.empty

  override def login(username: String, password: String): Future[LoginResult] = users.get(username) match {
    case None=> Future.successful(UserDoesNotExist(username))
    case Some(user)=>
      Future.successful(if(password.isBcrypted(user.password)) LoggedIn(user) else PasswordDoesNotMuch(username,password))
  }



  def clean() =  users = BiMap.empty //for testing

  override def register(username: String, password: String, email: String): Future[RegistrationResult] =
    users.get(username) match
    {
      case Some(user)=> Future.successful(UserAlreadyExists(LoginInfo(username,password,email)))
      case None if password.length<4=>Future.successful(BadPassword(LoginInfo(username,password,email),"password is too short"))
      case None if password==username => Future.successful(BadPassword(LoginInfo(username,password,email),"password and username cannot be same"))
      case None =>
        val hash = password.bcrypt
        users = users + (username -> LoginInfo(username,hash,email))
        Future.successful(UserRegistered(LoginInfo(username,hash,email)))
    }

}