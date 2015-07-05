package org.denigma.controls.login
import org.denigma.binding.extensions._
import org.scalajs.dom
import org.scalajs.dom.XMLHttpRequest
import org.scalajs.dom.ext.Ajax
import rx.ops._
import rx.{Rx, Var}
import scalajs.concurrent.JSExecutionContext.Implicits.queue

import scala.concurrent.Future


class AjaxSession extends Session
{

  private def printCookies() = {
    dom.console.log("COOKIES ="+dom.document.cookie)
  }

  private def logCookies(fut:Future[XMLHttpRequest]) = fut map { case
    result=>
      printCookies()
      dom.console.log("HEADERS = "+result.getAllResponseHeaders())
    dom.console.log("RESPONSE = "+result.response)

    result
  }


  override def register(username:String,password:String,email:String): Future[XMLHttpRequest] =  logCookies{
    Ajax.put(
      s"/users/register?username=$username&password=$password&email=$email",
      withCredentials = true
    )
  }

  override def logout(): Future[XMLHttpRequest] = logCookies{
    Ajax.get(
      sq.h(s"users/logout"),
      withCredentials = true
    ) map{ case result=>
      currentUser.set(None)
      result
    }
  }


  override def usernameLogin(username:String,password:String): Future[XMLHttpRequest] = logCookies{
    Ajax.put(
      s"/users/login?username=$username&password=$password",
      withCredentials = true
    )
  }

  override def emailLogin(email:String,password:String): Future[XMLHttpRequest] = logCookies{
    Ajax.put(
      s"/users/login?email=$email&password=$password",
      withCredentials = true
    )
  }

}

trait Session {

   val currentUser:Var[Option[String]]= Var(None)
   val userChange: Rx[(Option[String], Option[String])] = currentUser.unique().zip()

   def register(username:String,password:String,email:String):Future[_]

   def emailLogin(email:String,password:String):Future[_]

   def usernameLogin(name:String,password:String):Future[_]

  def setUsername(value:String)= value match {
    case "" | null | "guest" => currentUser.set(None)
    case uname => currentUser.set(Some(uname))
  }

   def logout(): Future[_]

   val username: Rx[String] = currentUser.map{
     case Some(str) => localName(str)
     case None=> "guest"
   }


   def localName(str:String): String = if(str.endsWith("/"))
     localName(str.substring(0,str.length-2))
   else
     if(str.contains("/"))
       str.substring(str.lastIndexOf("/")+1)
     else str


 }
