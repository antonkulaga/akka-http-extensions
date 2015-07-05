package org.denigma.preview.views

import org.denigma.binding.views.BindableView
import org.denigma.binding.views.collections.CollectionView
import org.scalajs.dom.raw.HTMLElement
import rx.Rx
import rx.core.Var
import rx.ops._

import scala.collection.immutable.Map

object TestData{

  val menuItems = List("Find Plasmids","Deposit Plasmids","How to Order","Plasmid Reference")

  val prefix = "test/"
}

/**
 * Menu view, this view is devoted to displaying menus
 * @param elem html element
 * @param params view params (if any)
 */
class MenuView(val elem:HTMLElement, val params:Map[String,Any] = Map.empty) extends CollectionView
{

  override def activateMacro(): Unit = { extractors.foreach(_.extractEverything(this))}

  override protected def attachBinders(): Unit =withBinders(BindableView.defaultBinders(this))

  override type Item = String

  override def newItem(item: Item) = this.constructItemView(item){ case (el,mp)=> //TODO: rename constructItem to smt like ConstructItemView
    new MenuItem(el,item,mp)
  }

  override type ItemView = MenuItem

  override val items: Rx[List[Item]] = Var(TestData.menuItems)

}

class MenuItem(val elem:HTMLElement,value:String, val params:Map[String,Any] = Map.empty) extends BindableView{

  val label: Var[String] = Var(value)
  val uri: Rx[String] = label.map(l=>TestData.prefix+l.replace(" ","_"))


  override def activateMacro(): Unit = { extractors.foreach(_.extractEverything(this))}

  override protected def attachBinders(): Unit = withBinders(BindableView.defaultBinders(this))

}