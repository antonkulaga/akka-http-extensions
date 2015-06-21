package org.denigma.preview.routes

import akka.http.extensions.stubs._
import akka.http.scaladsl.server.Directives


class Router extends Directives {
  val sessionController:SessionController = new InMemorySessionController
  val loginController:FutureLoginController = new InMemoryLoginController

  def routes = new Head().routes ~
    new Registration(
      loginController.loginByName,
      loginController.loginByEmail,
      loginController.register,
      sessionController.withToken)
      .routes ~
    new Pages().routes

}
