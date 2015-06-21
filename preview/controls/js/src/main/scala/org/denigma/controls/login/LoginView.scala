package org.denigma.controls.login

import org.denigma.binding.binders.{NavigationBinding, GeneralBinder}
import org.denigma.binding.binders.extractors.EventBinding
import org.denigma.binding.extensions._
import org.denigma.binding.views.BindableView
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.raw.{Event, HTMLElement}
import org.scalajs.dom
import rx.Rx
import rx.core.Var
import scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.collection.immutable._
import scala.util.{Failure, Success}

/**
 * Login view
 */
class LoginView(val elem:HTMLElement, val params:Map[String,Any]) extends BindableView with Login with Registration with Signed
{

/*  val testClick = Var(EventBinding.createMouseEvent())
  testClick.handler{
    Ajax.get("/test?why=some",withCredentials = true).onComplete{
      case Success(res)=>
        dom.alert("COOOKIE IS "+dom.document.cookie)
      case Failure(th)=> dom.alert("failure")

    }
  }*/

  isSigned.handler {
    if(isSigned.now)  inRegistration() = false
  }

  val emailLogin = Rx{  login().contains("@")}

  /**
   * If anything changed
   */
  val anyChange = Rx{ (login(),password(),email(),repeat(),inLogin())}
  val clearMessage = anyChange.handler{
    message()=""
  }

  override def activateMacro(): Unit = { extractors.foreach(_.extractEverything(this))}

  override protected def attachBinders(): Unit = this.withBinders(BindableView.defaultBinders(this))
}











