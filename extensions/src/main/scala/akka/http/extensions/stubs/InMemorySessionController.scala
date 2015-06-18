package akka.http.extensions.stubs

import java.util.UUID

import akka.http.extensions.utils.BiMap

import scala.concurrent.Future

/**
 * Inmemory sessions controller, recommended mostly for testing
 */
class InMemorySessionController extends SessionController {
  var tokens:BiMap[String,String] = BiMap.empty //token->username
  override def withToken(username: String): Future[String] = tokens.inverse.get(username) match
    {
      case Some(token)=> Future.successful(token)
      case None=>
        val token = UUID.randomUUID().toString
        tokens = tokens + (token->username)
        Future.successful(token)
    }

  override def getToken(username: String): Option[String] = tokens.inverse.get(username)

  def clean() = {
    tokens = BiMap.empty
  } //good for testing
}
