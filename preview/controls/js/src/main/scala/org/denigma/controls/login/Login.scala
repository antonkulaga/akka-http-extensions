package org.denigma.controls.login
import org.denigma.binding.binders.extractors.EventBinding
import org.denigma.binding.extensions._
import org.denigma.binding.views.BindableView
import org.scalajs.dom
import org.scalajs.dom._
import org.scalajs.dom.ext.{Ajax, AjaxException}
import org.scalajs.dom.raw.HTMLElement
import rx.ops._
import rx.{Rx, Var}

import scala.collection.immutable._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.{Failure, Success}
import org.denigma.binding.extensions._

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
     sq.h(s"users/login?${if(loginWithEmail.now) "email" else "username"}=${this.login.now}&password=${this.password.now}")
     ,withCredentials = true
   )

   val authClick = loginClick.takeIfAll(canLogin,inLogin)
   val authHandler = authClick.handler{
     this.auth().onComplete{

       case Success(req) =>
         dom.console.log("RESPONSE IS "+req.responseText)
         this.tellCookies()
         //dom.alert("authed successfuly")
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
