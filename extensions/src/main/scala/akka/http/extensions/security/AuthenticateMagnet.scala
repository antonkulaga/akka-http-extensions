package akka.http.extensions.security

import akka.http.scaladsl.model.headers.HttpChallenge
import akka.http.scaladsl.server._
import akka.http.scaladsl.util.FastFuture._

import scala.concurrent.Future

case class OptionalMagnet[T](getName:(String=>Option[T])) extends AuthenticateMagnet{
  type Result = T
  def directive:Directive[Tuple1[T]]  = Directive[Tuple1[T]] {inner=> ctx=>
      getToken(ctx) match{
        case Some(token)=> getName(token) match {
            case Some(name)=> inner(Tuple1(name))(ctx)
            case None=>
              ctx.reject(AuthenticationFailedRejection(AuthenticationFailedRejection.CredentialsRejected,makeChallenge(ctx)))
          }

        case None=>
          ctx.reject(AuthenticationFailedRejection(AuthenticationFailedRejection.CredentialsMissing,makeChallenge(ctx)))
      }
    }
}

case class FutureMagnet[T](getName:(String=>Future[T])) extends AuthenticateMagnet{
  type Result = T
  def directive:Directive[Tuple1[T]]  = Directive[Tuple1[T]]{ inner=>ctx=>
    getToken(ctx) match {
        case Some(name)=>
         import ctx.executionContext
         getName(name).fast.flatMap{
          case user=> inner(Tuple1(user))(ctx)
         } recoverWith{
          case th=>
              ctx.reject(
                AuthenticationFailedRejection(AuthenticationFailedRejection.CredentialsRejected,makeChallenge(ctx))
              ) //TODO: add another rejection
         }

        case None=>
          ctx.reject(AuthenticationFailedRejection(AuthenticationFailedRejection.CredentialsMissing,makeChallenge(ctx)))
    }
  }
}


sealed trait AuthenticateMagnet{

  def makeChallenge(ctx:RequestContext,realm:String = "") =  HttpChallenge(ctx.request.getUri().scheme(),realm)

  protected def getToken(ctx:RequestContext)= ctx.request.cookies.collectFirst{
    case pair if pair.name=="X-Token"=> pair.value
  }


  type Result
  def directive: Directive1[Result]
}

object AuthenticateMagnet {

  implicit def usernameByToken(getName:String=>Option[String]): OptionalMagnet[String] = new OptionalMagnet[String](getName)
  implicit def loginInfoByToken(getName:String=>Option[LoginInfo]): OptionalMagnet[LoginInfo] = new OptionalMagnet[LoginInfo](getName)
  implicit def futureUsernameByToken(getName:String=>Future[String]): FutureMagnet[String] = new FutureMagnet[String](getName)
  implicit def futureLoginInfoByToken(getName:String=>Future[LoginInfo]): FutureMagnet[LoginInfo] = new FutureMagnet[LoginInfo](getName)

}

