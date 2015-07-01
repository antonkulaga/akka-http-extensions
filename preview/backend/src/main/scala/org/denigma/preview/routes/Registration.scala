package org.denigma.preview.routes

import akka.http.extensions.security._
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.model.headers.HttpCookie
import akka.http.scaladsl.server._

import scala.concurrent.Future

class Registration(
                    usernameLogin:(String,String)=>Future[LoginResult],
                    emailLogin:(String,String)=>Future[LoginResult],
                    register:(String,String,String)=>Future[RegistrationResult],
                    tokenByUser:LoginInfo=>Future[String]
                    ) extends AuthDirectives
                  with Directives
                  with WithLoginRejections
                  with WithRegistrationRejections
{



  def routes: Route =
    pathPrefix("users") {
        pathPrefix("login") {
          put
            {
              handleRejections(loginRejectionHandlers){
                login(usernameLogin,emailLogin) { user=>
                  startSession(user, tokenByUser) {
                    complete(s"The user ${user.username} was logged in")
                  }
                }
              }
            }
          }
        }~
      pathPrefix("register"){
        put
        {
          handleRejections(registerRejectionHandlers){
            registration(register) {  user=>
              startSession(user, tokenByUser) {
                  complete(s"The user ${user.username} has been registered")
              }
            }
          }
        }
      }~
      pathPrefix("logout"){
          deleteCookie("token") { c=>
            c.complete(s"the user has been logged out!")
          }
        }
}

