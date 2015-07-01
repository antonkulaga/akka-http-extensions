package akka.http.extensions.security

import akka.http.scaladsl.model.headers.HttpCookie
import akka.http.scaladsl.server._
import akka.http.scaladsl.util.FastFuture._

import scala.concurrent.Future
import scala.util._

/**
 * Magnet that creates token from user
 *
 * TODO: merge String and LoginInfo implementations
 */
object TokenMagnet extends UserTokens with StringTokens

trait UserTokens {
  type FutureUserToken = LoginInfo=>Future[String]
  type TryUserToken = LoginInfo=>Try[String]
  type UserToken = LoginInfo=>String

  implicit def userToken(params:(LoginInfo,UserToken)): TokenMagnet  =
    TokenMagnet(Directive[Tuple1[String]] { inner ⇒ ctx ⇒
      val (user,tokenFun) = params
        inner(Tuple1(tokenFun(user)))(ctx)
      }.flatMap(token => Directives.setCookie(HttpCookie("token",  token)))
    )


  implicit def futureUserToken(params:(LoginInfo,FutureUserToken)): TokenMagnet  =
    TokenMagnet(Directive[Tuple1[String]] { inner ⇒ ctx ⇒
        import ctx.executionContext
        val (user,tokenFun) = params
        val future: Future[String] = tokenFun(user)
        future.fast
          .flatMap(t ⇒ inner(Tuple1(t))(ctx))
          .recoverWith{case th=>
          ctx.reject(ReadErrorRejection(s"cannot generate session token for ${user.username}",th))
        }
      }.flatMap(token => Directives.setCookie(HttpCookie("token",  token)))
    )



  implicit def tryUserToken(params:(LoginInfo,TryUserToken)): TokenMagnet  =
    TokenMagnet(Directive[Tuple1[String]] { inner ⇒ ctx ⇒
        val (user,tokenFun) = params
        tokenFun(user) match {
          case Success(t)=>  inner(Tuple1(t))(ctx)
          case Failure(th)=>  ctx.reject(ReadErrorRejection(s"cannot generate session token for ${user.username}",th))
        }
      }.flatMap(token => Directives.setCookie(HttpCookie("token",  token)))
    )


}

trait StringTokens
{

  type FutureToken = String=>Future[String]
  type TryToken = String=>Try[String]
  type UsernameToken = String=>String

  implicit def usernameToken(params:(String,UsernameToken)): TokenMagnet  =
    TokenMagnet(Directive.apply[Tuple1[String]] { inner ⇒ ctx ⇒
        inner(Tuple1(params._2(params._1)))(ctx)
      }.flatMap(token => Directives.setCookie(HttpCookie("token",  token)))
    )


  implicit def futureToken(params:(String,FutureToken)): TokenMagnet  =
    TokenMagnet(Directive[Tuple1[String]] { inner ⇒ ctx ⇒
        import ctx.executionContext
        val (username,tokenFun) = params
        val future: Future[String] = tokenFun(username)
        future.fast
          .flatMap(t ⇒ inner(Tuple1(t))(ctx))
          .recoverWith{case th=>
          ctx.reject(ReadErrorRejection(s"cannot generate session token for $username",th))
        }
      }.flatMap(token => Directives.setCookie(HttpCookie("token",  token)))
    )



  implicit def tryToken(params:(String,TryToken)): TokenMagnet  =
    TokenMagnet(Directive[Tuple1[String]] { inner ⇒ ctx ⇒
        val (username,tokenFun) = params
        tokenFun(username) match {
          case Success(t)=>  inner(Tuple1(t))(ctx)
          case Failure(th)=>  ctx.reject(ReadErrorRejection(s"cannot generate session token for $username",th))
        }
      }.flatMap(token => Directives.setCookie(HttpCookie("token",  token)))
    )

}

case class TokenMagnet(directive: Directive0)