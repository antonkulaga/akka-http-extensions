package org.denigma.preview.views

import org.denigma.binding.extensions._
import org.denigma.binding.views.BindableView
import org.denigma.binding.views.utils.ViewInjector
import org.denigma.controls.{Session, LoginView}
import org.querki.jquery._
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLElement
import rx.core.Var

import scala.collection.immutable.Map
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scala.util.Try


@JSExport("FrontEnd")
object FrontEnd extends BindableView with scalajs.js.JSApp
{

  override def name = "main"


  val hello = Var("HELLO WORLD!")

  lazy val elem: HTMLElement = dom.document.body

  override val params: Map[String, Any] = Map.empty

  val sidebarParams = js.Dynamic.literal(
    exclusive = false,
    dimPage = false,
    closable = false,
    useLegacy = false
  )
  /**
   * Register views
   */
  ViewInjector
    .register("menu", (el, params) =>Try(new MenuView(el,params)))
    .register("sidebar", (el, params) =>Try(new SidebarView(el,params)))
    .register("login", (el, params) =>Try(new LoginView(el,params)))

  @JSExport
  def main(): Unit = {
    this.bindView(this.viewElement)
    this.login("guest") //TODO: change it when session mechanism will work well
  }

  @JSExport
  def login(username:String): Unit = Session.login(username)

  @JSExport
  def showLeftSidebar() = {
    $(".left.sidebar").dyn.sidebar(sidebarParams).sidebar("show")
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

  override def activateMacro(): Unit = {
    extractors.foreach(_.extractEverything(this))
  }

  def attachBinders() = {
    this.binders = BindableView.defaultBinders(this)
  }
}
