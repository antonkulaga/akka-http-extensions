package org.denigma.preview.views

import org.denigma.binding.views.BindableView
import org.denigma.controls.login.{LoginView, AjaxSession}
import org.scalajs.dom
import org.scalajs.dom.raw.Element
import org.denigma.binding.binders._
import org.denigma.binding.extensions._
import org.semantic.SidebarConfig
import org.semantic._
import org.semantic.ui._

import org.querki.jquery._
import scala.scalajs.js.annotation.JSExport

@JSExport("FrontEnd")
object FrontEnd extends BindableView with scalajs.js.JSApp
{

  lazy val elem: Element = dom.document.body

  val sidebarParams = SidebarConfig.exclusive(false).dimPage(false).closable(false).useLegacy(false)

  val session = new AjaxSession()

  /**
   * Register views
   */
  override lazy val injector = defaultInjector
    .register("menu"){
      case (el, args) =>
        new MenuView(el,args).withBinders(menu=>List(new GeneralBinder(menu),new NavigationBinder(menu)))
    }
    .register("sidebar"){ case (el, args) => new SidebarView(el,args).withBinder(new GeneralBinder(_)) }
    .register("login"){ case (el, args) => new LoginView(el,session).withBinder(new GeneralBinder(_)) }


  this.withBinder(new GeneralBinder(_))

  @JSExport
  def main(): Unit = {
    this.bindElement(viewElement)
    this.login("guest") //TODO: change it when session mechanism will work well
  }

  @JSExport
  def login(username:String): Unit = session.setUsername(username)

  @JSExport
  def showLeftSidebar() = {
    $(".left.sidebar").sidebar(sidebarParams).show()
  }

  @JSExport
  def load(content: String, into: String): Unit = {
    dom.document.getElementById(into).innerHTML = content
  }

  @JSExport
  def moveInto(from: String, into: String): Unit = {
    for {
      ins <- sq.byId(from)
      intoElement <- sq.byId(into)
    } {
      this.loadElementInto(intoElement, ins.innerHTML)
      ins.parentNode.removeChild(ins)
    }
  }
}
