package akka.http.extensions.security


import akka.http.extensions.security.LoginInfo
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.util.FastFuture._
import scala.concurrent.Future
import scala.util._

case object UserRealm extends Realm
trait Realm

case class PermissionRejection(user:LoginInfo,realm:Realm) extends Rejection

case class PermissionMagnet(directive:Directive0)

object PermissionMagnet{

  implicit def authorize(params:(LoginInfo,LoginInfo=>Boolean)):PermissionMagnet =  authorize(
      (params._1 , UserRealm, (info:LoginInfo,r:Realm)=> params._2(info))
    )

  implicit def tryAuthorize(params:(LoginInfo,LoginInfo=>Try[Boolean])):PermissionMagnet =  tryAuthorize(
    (params._1 , UserRealm, (info:LoginInfo,r:Realm)=> params._2(info))
  )

  implicit def futureAuthorize(params:(LoginInfo,LoginInfo=>Future[Boolean])):PermissionMagnet =  futureAuthorize(
    (params._1 , UserRealm, (info:LoginInfo,r:Realm)=> params._2(info))
  )


  implicit def authorize(params:(LoginInfo,Realm,(LoginInfo,Realm)=>Boolean)):PermissionMagnet = PermissionMagnet(
      Directive[Unit]{inner=>ctx=>
        val (user,realm,check) = params
        if(check(user,realm)) inner()(ctx) else ctx.reject(PermissionRejection(user,realm))
      }
  )

  implicit def tryAuthorize(params:(LoginInfo,Realm,(LoginInfo,Realm)=>Try[Boolean])):PermissionMagnet =  PermissionMagnet(
    Directive[Unit]{inner=>ctx=>
        val (user,realm,check) = params
        check(user,realm) match {
           case Success(true)=> inner()(ctx)
           case Success(false)=> ctx.reject(PermissionRejection(user,realm))
           case Failure(th)=> ctx.reject(ReadErrorRejection(s"read error on authorization of $user",th))
        }
      }
  )

  implicit def futureAuthorize(params:(LoginInfo,Realm,(LoginInfo,Realm)=>Future[Boolean])):PermissionMagnet =  PermissionMagnet(
    Directive[Unit]{inner=>ctx=>
        val (user,realm,check) = params
        import ctx.executionContext
        check(user,realm).fast.flatMap {
           case true=> inner()(ctx)
           case false=> ctx.reject(PermissionRejection(user,realm))
           } recoverWith{  case th=> ctx.reject(ReadErrorRejection(s"read error on authorization of $user",th))     }
      }
  )

}