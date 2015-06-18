package akka.http.extensions.security

case class LoginInfo(username:String,password:String,email:String) //note: password or hash of a password