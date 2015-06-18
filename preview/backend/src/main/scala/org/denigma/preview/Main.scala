package org.denigma.preview

import akka.actor.{ActorSystem, _}
import akka.http.scaladsl.Http
import com.typesafe.config.Config

/**
 * For running as kernel
 */
object Main extends App
{
  implicit val system = ActorSystem()
  sys.addShutdownHook(system.shutdown())

  val config: Config = system.settings.config
  var main:ActorRef = system.actorOf(Props[MainActor])
  main ! AppMessages.Start(config)

}