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
  * Basic login varibales/events
  */
trait BasicLogin extends BindableView
 {
   /**
    * Extracts name from global
    */
   val registeredName = Session.username //I know, that it is bad to have shared mustable state=)

   val login = Var("","login")
   val password = Var("","password")
   val email = Var("","email")
   val message = Var("","message")


   val isSigned = Session.currentUser.map(_.isDefined)
   val inRegistration = Var(false)
   val inLogin = Rx(!inRegistration() && !isSigned())

   val validUsername:Rx[Boolean] = login.map(l=>l.length>4 && l.length<50 && !l.contains(" ") && l!="guest")
   val validPassword:Rx[Boolean] = password.map(p=>p.length>4 && p!=login.now)
   val canLogin = Rx{validUsername() && validPassword()}

   val loginClick: Var[MouseEvent] = Var(EventBinding.createMouseEvent(),"loginClick")
   val logoutClick: Var[MouseEvent] = Var(EventBinding.createMouseEvent(),"logoutClick")
   val signupClick: Var[MouseEvent] = Var(EventBinding.createMouseEvent(),"signupClick")

   def report(req:org.scalajs.dom.XMLHttpRequest): String = req.response.dyn.message match {
     case m if m.isNullOrUndef => this.report(req.responseText)
     case other =>this.report(other.toString)
   }

   /**
    * Reports some info
    * @param str
    * @return
    */
   def report(str:String): String = {
     this.message()=str
     str
   }

   def reportError(str:String) = dom.console.error(this.report(str))


 }
