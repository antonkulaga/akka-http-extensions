package akka.http.extensions.security

import akka.http.scaladsl.server.Directive
import akka.http.scaladsl.util.FastFuture
import akka.http.scaladsl.util.FastFuture._

import scala.concurrent.Future
import scala.util._
object SessionMagnet {

  type FutureToken = String=>Future[String]
  type TryToken = String=>Try[String]

  implicit private[this] def directive2magnet(dir:Directive[Tuple1[String]]): SessionMagnet =
    new SessionMagnet {
      val directive = dir
    }
  


  implicit def convertFuture(params:(String,FutureToken)): SessionMagnet  =
      Directive[Tuple1[String]] { inner ⇒ ctx ⇒
        import ctx.executionContext
        val (username,tokenFun) = params
        val future: Future[String] = tokenFun(username)
        future.fast
          .flatMap(t ⇒ inner(Tuple1(t))(ctx))
          .recoverWith{case th=>
          ctx.reject(ReadErrorRejection(s"cannot generate session token for $username",th))
        }
      }



  implicit def convertTry(params:(String,TryToken)): SessionMagnet  =
    new SessionMagnet {
      val directive: Directive[Tuple1[String]] = Directive[Tuple1[String]] { inner ⇒ ctx ⇒
        val (username,tokenFun) = params
        tokenFun(username) match {
          case Success(t)=>  inner(Tuple1(t))(ctx)
          case Failure(th)=>  ctx.reject(ReadErrorRejection(s"cannot generate session token for $username",th))
        }
      }
    }


}

trait SessionMagnet {
  def directive: Directive[Tuple1[String]]
}