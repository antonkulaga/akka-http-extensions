package akka.http.extensions.security

import akka.http.scaladsl.server._
import akka.http.scaladsl.util.FastFuture._

import scala.concurrent.Future
import scala.util._
case class LoginMagnet(directive: Directive1[LoginInfo])

object LoginMagnet extends FutureLoginMagnet with TryLoginMagnet

sealed trait FutureLoginMagnet {
  type FutureLogin = (String,String)=>Future[LoginResult]

  protected def futureLoginDirective(params:(String,String, FutureLogin)): Directive1[LoginInfo] =
    Directive[Tuple1[LoginInfo]] { inner ⇒ ctx ⇒
      import ctx.executionContext
      val (username,password,futureLogin) = params
      futureLogin(username,password).fast
        .flatMap{
        case e:UserDoesNotExist =>  ctx.reject(e)
        case p:PasswordDoesNotMuch =>  ctx.reject(p)
        case e:EmailDoesNotExist =>  ctx.reject(e)
        case l:LoggedIn =>inner(Tuple1(l.user))(ctx)
      }
        .recoverWith{
        case th=>  ctx.reject(ReadErrorRejection(s"cannot login  $username",th))
      }
    }

  implicit def futureLoginDefault(tryLogin: FutureLogin):LoginMagnet =    LoginMagnet(
      Directives.parameter("username","password") //todo add email support
        .tflatMap{  case (username,password)=>  futureLoginDirective((username,password,tryLogin))  }
  )



  implicit def futureLoginDefault(logins: (FutureLogin,FutureLogin)):LoginMagnet =
  {
    val dir: Directive1[LoginInfo] =
      Directives.parameter("username","password") //login by username
        .tflatMap{  case (username,password)=>  futureLoginDirective((username,password,logins._1))  }  |
        Directives.parameter("email","password") //login by email
          .tflatMap{  case (email,password)=>  futureLoginDirective((email,password,logins._2))  }
    LoginMagnet(dir)
  }



  implicit def futureLogin(params:(String,String, FutureLogin)):LoginMagnet =
    LoginMagnet(futureLoginDirective(params) )
}

sealed trait TryLoginMagnet {
  type TryLogin = (String,String)=>Try[LoginResult]

  protected def tryLoginDirective(params:(String,String, TryLogin)): Directive1[LoginInfo] = {
    Directive[Tuple1[LoginInfo]]{ inner ⇒ ctx ⇒
      val (username,password,login) = params
      login(username,password) match {
        case Success(e:UserDoesNotExist) =>  ctx.reject(e)
        case Success(p:PasswordDoesNotMuch) =>  ctx.reject(p)
        case Success(e:EmailDoesNotExist) =>  ctx.reject(e)
        case Success(l:LoggedIn) =>inner(Tuple1(l.user))(ctx)
        case Failure(th) =>ctx.reject(ReadErrorRejection(s"cannot login $username",th))
      }
    }
  }

  implicit def tryLogin(params:(String,String, TryLogin)):LoginMagnet =
    LoginMagnet(tryLoginDirective(params))

  implicit def tryLoginDefault(tryLogin: TryLogin):LoginMagnet =
  {
    LoginMagnet(Directives.parameter("username","password").tflatMap{
      case (username,password)=>  tryLoginDirective((username,password,tryLogin))
    })
  }
}