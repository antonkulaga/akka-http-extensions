package akka.http.extensions

import akka.http.extensions.security._
import akka.http.scaladsl.server.{Directives, _}

import scala.concurrent.Future

class DummyRegistration(
                    usernameLogin:(String,String)=>Future[LoginResult],
                    emailLogin:(String,String)=>Future[LoginResult],
                    register:(String,String,String)=>Future[RegistrationResult],
                    userByToken:String=>Option[LoginInfo],
                    makeToken:LoginInfo=>Future[String]
                    ) extends AuthDirectives
                  with Directives
                  with WithLoginRejections
                  with WithRegistrationRejections
{
  def routes: Route =
    pathPrefix("users") {
      pathPrefix("login") {
        put {
          handleRejections(loginRejectionHandlers) {
            login(usernameLogin, emailLogin) { user =>
              startSession(user, makeToken) {
                complete(s"The user ${user.username} was logged in")
              }
            }
          }
        }
      } ~
        pathPrefix("register") {
          put {
            handleRejections(registerRejectionHandlers) {
              registration(register) { user =>
                startSession(user, makeToken) {
                  complete(s"The user ${user.username} has been registered")
                }
              }
            }
          }
        } ~
        pathPrefix("logout") {
          deleteCookie("X-Token",path="/") { c =>
            c.complete(s"The user was logged out")
        }
      } ~
        pathPrefix("status"){
          this.authenticate(userByToken){user=>
            complete(user.username)
          }
        }
    }
}

