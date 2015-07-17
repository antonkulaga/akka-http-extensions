package org.denigma.preview.views

import org.denigma.binding.extensions.sq
import org.denigma.binding.views.BindableView
import org.denigma.preview.views.stubs.WithDomain
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.raw.HTMLElement
import rx._
import org.querki.jquery._
import scala.scalajs.js
import org.denigma.binding.extensions._
import scalajs.concurrent.JSExecutionContext.Implicits.queue
import org.semantic.ui._
/**
 * View for the sitebar
 */
class SidebarView (val elem:HTMLElement,val params:Map[String,Any] = Map.empty[String,Any]) extends BindableView with WithDomain{

  override def activateMacro(): Unit = { extractors.foreach(_.extractEverything(this))}

  val logo = Var("/resources/logo.jpg")

  override def bindElement(el:HTMLElement) = {
    super.bindElement(el)
    $(".ui.accordion").accordion()
  }

  override protected def attachBinders(): Unit =  binders =  BindableView.defaultBinders(this)
}
