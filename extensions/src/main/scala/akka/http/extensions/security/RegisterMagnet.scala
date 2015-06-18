package akka.http.extensions.security

import akka.http.scaladsl.server._
import akka.http.scaladsl.util.FastFuture
import akka.http.scaladsl.util.FastFuture._

import scala.concurrent.Future
import scala.util._

case class RegisterMagnet(directive: Directive1[LoginInfo])

/**
 * This companion object is used for implicit convertions of functions to RegisterMagnet
 * according to Magnet design pattern
 */
object RegisterMagnet extends FutureRegisterMagnet with TryRegisterMagnet

sealed trait FutureRegisterMagnet {

  type FutureRegister = (String,String,String)=>Future[RegistrationResult]

  protected def futureDirective(params:(String,String,String, FutureRegister)):Directive1[LoginInfo] =
    Directive[Tuple1[LoginInfo]]{ inner ⇒ ctx ⇒
      import ctx.executionContext
      val (username,password,email,register) = params
      register(username,password,email).fast flatMap {
        case u:UserRegistered =>inner(Tuple1(u.user))(ctx)
        case n:UserAlreadyExists => ctx.reject(n)
        case n:BadPassword => ctx.reject(n)
        case n:BadEmail => ctx.reject(n)
      } recoverWith {
        case th => ctx.reject(ReadErrorRejection(s"cannot register $username", th))
      }
    }

  /**
   * same as registration but also extracts result from
   * @param register
   * @return
   */
  implicit def futureRegistrationDefault(register: FutureRegister):RegisterMagnet =
    RegisterMagnet(Directives.parameters("username","password","email").tflatMap{ case (username,password,email)=>
      futureDirective((username,password,email,register))
    })


  implicit def futureRegistration(params:(String,String,String, FutureRegister)):RegisterMagnet =
    RegisterMagnet(futureDirective(params))

}

sealed trait TryRegisterMagnet {

  type TryRegister = (String,String,String)=>Try[RegistrationResult]

  protected def tryDirective(params:(String,String,String, TryRegister)):Directive1[LoginInfo] =
    Directive[Tuple1[LoginInfo]]{ inner ⇒ ctx ⇒
      val (username,password,email,register) = params
      register(username,password,email) match {
        case Success(u:UserRegistered) =>inner(Tuple1(u.user))(ctx)
        case Success(n:UserAlreadyExists) => ctx.reject(n)
        case Success(n:BadPassword) => ctx.reject(n)
        case Success(n:BadEmail) => ctx.reject(n)
        case Failure(th) => ctx.reject(ReadErrorRejection(s"cannot register $username", th))
      }
    }

  /**
   * same as registration but also extracts result from
   * @param register
   * @return
   */
  implicit def tryRegistrationDefault(register: TryRegister):RegisterMagnet =
    RegisterMagnet(Directives.parameters("username","password","email").tflatMap{ case (username,password,email)=>
      tryDirective((username,password,email,register))
    })

  implicit def tryRegistration(params:(String,String,String, TryRegister)):RegisterMagnet = RegisterMagnet(tryDirective(params))

}
