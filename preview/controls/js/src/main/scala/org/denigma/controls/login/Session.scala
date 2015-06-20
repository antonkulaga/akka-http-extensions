package org.denigma.controls.login
import org.denigma.binding.extensions._
import rx.ops._
import rx.{Rx, Var}

object Session {

   val currentUser:Var[Option[String]]= Var(None)
   val userChange: Rx[(Option[String], Option[String])] = currentUser.unique().zip()

   def login(str:String) = str match {
     case "" | null | "guest" => Session.currentUser.set(None)
     case uname => Session.currentUser.set(Some(uname))
   }

   def logout() = currentUser.set(None)

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
