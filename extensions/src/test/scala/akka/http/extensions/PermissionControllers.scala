package akka.http.extensions


import akka.http.extensions.security._
import akka.http.scaladsl.server.{Directives, Route}

import scala.collection.mutable


case object SpecialRealm extends Realm
case object VIPRealm extends Realm

trait PermissionControllers extends RegistrationControllers
{
  class Permissions(sessionController: TestSessionController,loginController: TestLoginController) extends AuthDirectives with Directives
  {
    case object OtherRealm extends Realm

    var drugs:Set[String] = Set.empty

    val permissions = new mutable.HashMap[Realm, mutable.Set[LoginInfo]] with mutable.MultiMap[Realm, LoginInfo]

    def checkRights(user:LoginInfo,realm:Realm):Boolean = if(realm==UserRealm)
        loginController.exists(user)
      else
        permissions.get(realm).exists(_.contains(user))

    def add2realm(user:LoginInfo,realm: Realm) ={
      permissions.addBinding(realm,user)
    }

    def removeFromRealm(user:LoginInfo,realm: Realm) ={
      permissions.removeBinding(realm,user)
    }

    lazy val realms: Map[String, Realm] = Map("user"->UserRealm,"vip"->VIPRealm,"special"->SpecialRealm,""->UserRealm)


    def routes: Route =
      pathPrefix("add") {
        pathPrefix("drug") {
          put
          {
            parameter("name","kind"){ (name,kind)=>
              authenticate(sessionController.userByToken _){ user=>
                val realm: Realm = realms.getOrElse(kind, realms("user"))
                allow(user,realm, checkRights _)
                {
                  drugs = drugs + name
                  complete(s"drug $name added!")
                }
              }

            }
          }
        }
      }
  }
}
