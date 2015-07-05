package org.denigma.controls.login

import org.denigma.binding.extensions._
import org.denigma.binding.views.BindableView
import org.scalajs.dom.raw.HTMLElement
import rx.Rx

import scala.collection.immutable._

/**
 * Login view
 */
class LoginView(val elem:HTMLElement, val session:Session, val params:Map[String,Any])
  extends BindableView
  with Login
  with Registration
  with Signed
{

  isSigned.handler {
    if(isSigned.now)  inRegistration() = false
  }

  val emailLogin = Rx{ username().contains("@") }

  /**
   * If anything changed
   */
  val anyChange = Rx{ (username(),password(),email(),repeat(),inLogin()) }
  val clearMessage = anyChange.handler{
    message()=""
  }

  override def activateMacro(): Unit = { extractors.foreach(_.extractEverything(this))}

  override protected def attachBinders(): Unit = this.withBinders(BindableView.defaultBinders(this))
}











