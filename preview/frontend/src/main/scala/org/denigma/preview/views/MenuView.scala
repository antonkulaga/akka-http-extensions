package org.denigma.preview.views

import org.denigma.binding.binders.{NavigationBinder, GeneralBinder}
import org.denigma.binding.views._
import org.scalajs.dom.raw.Element
import rx.Rx
import rx.Var
import rx._
import rx.Ctx.Owner.Unsafe.Unsafe

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
class MenuView(val elem: Element, val params: Map[String,Any] = Map.empty) extends CollectionSeqView
{

  override type Item = String


  override def newItemView(item: Item) = this.constructItemView(item){ case (el,mp)=> //TODO: rename constructItem to smt like ConstructItemView
    new MenuItem(el,item,mp).withBinders(i=>List(new GeneralBinder(i),new NavigationBinder(i)))
  }

  override type ItemView = MenuItem

  override val items: Rx[List[Item]] = Var(TestData.menuItems)

}

class MenuItem(val elem: Element, value: String, val params: Map[String, Any] = Map.empty) extends BindableView{

  val label: Var[String] = Var(value)
  val uri: Rx[String] = label.map(l=>TestData.prefix+l.replace(" ","_"))

}