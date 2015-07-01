package akka.http.extensions.stubs

import akka.http.extensions.security._
import akka.http.extensions.utils.BiMap
import com.github.t3hnar.bcrypt._

import scala.concurrent.Future

/**
 * Inmemory login and registration controller, recommended mostly for testing
 */
class InMemoryLoginController extends FutureLoginController {

  protected var usersByName:Map[String,LoginInfo] = Map.empty
  protected var usersByEmail:Map[String,LoginInfo] = Map.empty

  def addUser(user:LoginInfo,computeHash:Boolean = true): LoginInfo =if(computeHash)
    addUser(this.withHash(user),false)
  else
  {
    usersByName =usersByName + (user.username -> user)
    usersByEmail =usersByEmail + (user.email -> user)
    user
  }

  def removeUser(user:LoginInfo) = {
    usersByName =usersByName -  user.username
    usersByEmail =usersByEmail - user.email
    this
  }

  def isValidEmail(email: String): Boolean = """(\w+)@([\w\.]+)""".r.unapplySeq(email).isDefined

  override def loginByName(username: String, password: String): Future[LoginResult] = usersByName.get(username) match {
    case None=> Future.successful(UserDoesNotExist(username))
    case Some(user)=>
      Future.successful(if(password.isBcrypted(user.password)) LoggedIn(user) else PasswordDoesNotMuch(username,password))
  }


  override def loginByEmail(email: String, password: String): Future[LoginResult] = usersByEmail.get(email) match {
    case None=> Future.successful(EmailDoesNotExist(email))
    case Some(user)=>
      Future.successful(if(password.isBcrypted(user.password)) LoggedIn(user) else PasswordDoesNotMuch(email,password))
  }



  def clean() =  {
    usersByName = Map.empty
    usersByEmail = Map.empty
  }


  override def register(username: String, password: String, email: String): Future[RegistrationResult] = {
    val registerInfo = LoginInfo(username, password, email)
    usersByName.get(username) match {
      case Some(existed) => Future.successful(UserAlreadyExists(existed))
      case None if password.length < 4 => Future.successful(BadPassword(registerInfo, "password is too short"))
      case None if password == username => Future.successful(BadPassword(registerInfo, "password and username cannot be same"))
      case None if !this.isValidEmail(email) => Future.successful(BadEmail(registerInfo,s"$email is not an email!"))
      case None =>
        Future.successful(UserRegistered(addUser(registerInfo)))
    }
  }

}