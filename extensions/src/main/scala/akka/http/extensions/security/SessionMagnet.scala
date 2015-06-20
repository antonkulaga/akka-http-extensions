package akka.http.extensions.security

import akka.http.scaladsl.server.{Directive1, Directive}
import akka.http.scaladsl.util.FastFuture
import akka.http.scaladsl.util.FastFuture._

import scala.concurrent.Future
import scala.util._
object SessionMagnet {

  type FutureToken = String=>Future[String]
  type TryToken = String=>Try[String]

  implicit def futureUserToken(params:(LoginInfo,FutureToken)): SessionMagnet  = this.futureToken(params._1.username->params._2)

  implicit def futureToken(params:(String,FutureToken)): SessionMagnet  =
    SessionMagnet(Directive[Tuple1[String]] { inner ⇒ ctx ⇒
        import ctx.executionContext
        val (username,tokenFun) = params
        val future: Future[String] = tokenFun(username)
        future.fast
          .flatMap(t ⇒ inner(Tuple1(t))(ctx))
          .recoverWith{case th=>
          ctx.reject(ReadErrorRejection(s"cannot generate session token for $username",th))
        }
      })


  implicit def tryUserToken(params:(LoginInfo,TryToken)): SessionMagnet  = this.tryToken(params._1.username->params._2)

  implicit def tryToken(params:(String,TryToken)): SessionMagnet  =
    SessionMagnet(Directive[Tuple1[String]] { inner ⇒ ctx ⇒
        val (username,tokenFun) = params
        tokenFun(username) match {
          case Success(t)=>  inner(Tuple1(t))(ctx)
          case Failure(th)=>  ctx.reject(ReadErrorRejection(s"cannot generate session token for $username",th))
        }
      })

}

case class SessionMagnet(directive: Directive1[String])