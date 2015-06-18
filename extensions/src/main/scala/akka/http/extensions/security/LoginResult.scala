package akka.http.extensions.security

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Rejection, RejectionHandler}


case class LoggedIn(user:LoginInfo) extends LoginResult
case class UserDoesNotExist(username:String) extends LoginResult with Rejection
//case class EmailDoesNotExist(email:String) extends LoginResult with Rejection
case class PasswordDoesNotMuch(username:String,password:String) extends LoginResult with Rejection
sealed trait LoginResult

trait WithLoginRejections{
  implicit def loginRejectionHandlers =
    RejectionHandler.newBuilder()
      .handle { case PasswordDoesNotMuch(username,password) ⇒
      complete(Forbidden, s"The password does not match") }
      .handle { case UserDoesNotExist(username) ⇒
      complete(Forbidden, s"You cannot login because the user $username does not exist") }
//      .handle { case EmailDoesNotExist(email) ⇒
//      complete(Forbidden, s"You cannot login because there is not user with  $email") }
      .result()
}