package org.denigma.controls.login
import org.denigma.binding.extensions._
import org.scalajs.dom
import org.scalajs.dom.ext.{Ajax, AjaxException}
import rx.{Rx, Var}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.{Failure, Success}

/**
  * Part of the view that deals with registration
  */
trait Registration extends BasicLogin{

   /**
    * rx property binded to repeat password input
    */
   val repeat = Var("","repeat")
   val emailValid: Rx[Boolean] = Rx {email().length>4 && this.isValidEmail(email())}

   /**
    * Email regex to check if email is valid
    * @param email
    * @return
    */
   def isValidEmail(email: String): Boolean = """(\w+)@([\w\.]+)""".r.unapplySeq(email).isDefined


   /**
    * True if password and repeatpassword match
    */
   val samePassword = Rx{
     password()==repeat()
   }
   /**
    * Reactive variable telling if register request can be send
    */
   val canRegister = Rx{ samePassword() && canLogin() && emailValid()}

   val toggleRegisterClick = this.signupClick.takeIf(this.inLogin)
   val toggleRegisterHandler = this.toggleRegisterClick.handler{
     this.inRegistration() = true
   }

   protected def register() =  Ajax.put(
     s"/users/register?username=${this.login.now}&password=${this.password.now}&email=${this.email.now}",
     withCredentials = true
   )


   val registerClick = this.signupClick.takeIfAll(this.canRegister,this.inRegistration)
   val registerHandler = this.registerClick.handler{
     this.register().onComplete{

       case Success(req) =>
         dom.console.log("COOKIES: :\n"+dom.document.cookie)
         println(req.getAllResponseHeaders())
         println(req.getResponseHeader("SET-COOKIE"))
         Session.login(login.now)

       case Failure(ex:AjaxException) =>
         //this.report(s"Registration failed: ${ex.xhr.responseText}")
         this.report(ex.xhr)

       case _ => this.reportError("unknown failure")

     }
   }


 }
