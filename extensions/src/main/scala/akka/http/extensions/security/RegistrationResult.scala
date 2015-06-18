package akka.http.extensions.security

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Rejection, RejectionHandler}

case class UserRegistered(user:LoginInfo) extends RegistrationResult
case class UserAlreadyExists(user:LoginInfo) extends RegistrationResult with Rejection
case class BadEmail(user:LoginInfo,message:String) extends RegistrationResult with Rejection
case class BadPassword(user:LoginInfo,message:String) extends RegistrationResult with Rejection
sealed trait RegistrationResult

trait WithRegistrationRejections{
  implicit def registerRejectionHandlers =
    RejectionHandler.newBuilder()
      .handle { case UserAlreadyExists(user) ⇒
      complete(Forbidden, s"You cannot register, as user ${user.username} already exists") }
      .handle { case BadEmail(user,message) ⇒
      complete(Forbidden, s"The email of ${user.username} is wrong, because: $message") }
      .handle { case BadPassword(user,message) ⇒
      complete(Forbidden, s"The password of ${user.username} is wrong, because: $message") }
      .result()
}