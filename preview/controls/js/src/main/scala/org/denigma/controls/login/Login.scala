package org.denigma.controls.login
import org.denigma.binding.extensions._
import org.scalajs.dom
import org.scalajs.dom.ext.{Ajax, AjaxException}
import rx.ops._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.{Failure, Success}

/**
  * Deals with login features
  */
trait Login extends BasicLogin{

  val loginWithEmail = this.login.map(l=>l.contains('@'))

   /**
    * When the user decided to switch to login
    */
   val loginToggleClick = loginClick.takeIf(inRegistration)

   /**
    * When the user comes from registration to login
    */
   val toggleLogin = this.loginToggleClick.handler{
     this.inRegistration() = false
   }

   def auth() = Ajax.get(
     s"/users/login?${if(loginWithEmail.now) "email" else "username"}=${this.login.now}&password=${this.password.now}",
     withCredentials = true
   )

   val authClick = loginClick.takeIfAll(canLogin,inLogin)
   val authHandler = authClick.handler{
     this.auth().onComplete{

       case Success(req) =>
         dom.console.log(dom.document.cookie)
         Session.login(login.now)
       //TODO: get full username
       //Session.setUser(user)


       case Failure(ex:AjaxException) =>
         //this.report(s"Authentication failed: ${ex.xhr.responseText}")
         this.report(ex.xhr)


       case _ => this.reportError("unknown failure")
     }
   }


 }
