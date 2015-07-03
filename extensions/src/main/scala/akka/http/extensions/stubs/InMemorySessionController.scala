package akka.http.extensions.stubs

import java.util.UUID

import akka.http.extensions.security.LoginInfo
import akka.http.extensions.utils.BiMap

import scala.concurrent.Future

/**
 * Inmemory sessions controller, recommended mostly for testing
 */
class InMemorySessionController extends SessionController {
  var tokens:BiMap[String,LoginInfo] = BiMap.empty //token->username

  override def makeToken(user: LoginInfo): Future[String] = tokens.inverse.get(user) match
    {
      case Some(token)=> Future.successful(token)
      case None=>
        val token = UUID.randomUUID().toString
        tokens = tokens + (token->user)
        Future.successful(token)
    }

  def setToken(user:LoginInfo) = {
    val token = UUID.randomUUID().toString
    tokens = tokens + (token->user)
    token
  }



  def clean() = {
    tokens = BiMap.empty
  } //good for testing
  override def userByToken(token: String): Option[LoginInfo] = tokens.get(token)

  override def tokenByUser(user: LoginInfo): Option[String] = tokens.inverse.get(user)

  override def tokenByUsername(username:String): Option[String] = tokens.inverse.collectFirst{
    case (key,value) if key.username==username => value
  }

}
