package akka.http.extensions.stubs

import akka.http.extensions.security.{LoginResult, RegistrationResult}

import scala.concurrent.Future

trait FutureLoginController{
  
  def login(username:String,passw:String):Future[LoginResult]

  def register(username:String,passw:String,email:String):Future[RegistrationResult]

}

trait SessionController{

  def withToken(username:String):Future[String]

  def getToken(username:String):Option[String]

}