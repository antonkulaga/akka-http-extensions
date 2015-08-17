package org.denigma.preview.views

import org.denigma.binding.views.BindableView
import org.denigma.preview.views.stubs.WithDomain
import org.querki.jquery._
import org.scalajs.dom.raw.HTMLElement
import org.semantic.ui._
import rx._
/**
 * View for the sitebar
 */
class SidebarView (val elem:HTMLElement,val params:Map[String,Any] = Map.empty[String,Any]) extends BindableView with WithDomain{
  val logo = Var("/resources/logo.jpg")

  override def bindElement(el:HTMLElement) = {
    super.bindElement(el)
    $(".ui.accordion").accordion()
  }
}
