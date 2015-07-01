package org.denigma.preview

import akka.actor._
import akka.http.scaladsl.Http.{IncomingConnection, ServerBinding}
import akka.http.scaladsl.{Http, _}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import org.denigma.preview.routes.Router

import scala.concurrent.Future

class MainActor  extends Actor with ActorLogging // Routes
{
  implicit val system = context.system
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher


  val server: HttpExt = Http(context.system)
  var serverSource: Source[IncomingConnection, Future[ServerBinding]] = null
  val router = new Router()


  override def receive: Receive = {
    case AppMessages.Start(config)=>
      val (host,port) = (config.getString("app.host") , config.getInt("app.port"))
      server.bindAndHandle(router.routes, host, port)

    case AppMessages.Stop=> onStop()
  }

  def onStop() = {
    log.info("Main actor has been stoped...")
  }

  override def postStop() = {
    onStop()
  }

}
