package akka.http.extensions.resources

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{DateTime, HttpEntity, ContentType}
import akka.http.scaladsl.server.directives.ContentTypeResolver
import akka.http.scaladsl.server.directives.FileAndResourceDirectives.ResourceFile
import akka.http.scaladsl.server.{Directives, Directive}
import akka.stream.ActorAttributes
import akka.stream.io.InputStreamSource

import scala.io.Source

trait TextFilesDirectives {

  /*def resourceFolder(path:String) =  Directive[Tuple1[Any]]{ inner=>ctx=>
    Directives.getFromResource(path)
    inner(Tuple1(""))(ctx)
  }*/
  def resource(resourceName:String,
               classLoader: ClassLoader = classOf[ActorSystem].getClassLoader)
              (implicit resolver: ContentTypeResolver) =  Directive[Tuple1[ResourceFile]]{ inner=>ctx=>
    val contentType:ContentType = resolver(resourceName)
    if (!resourceName.endsWith("/"))
        Option(classLoader.getResource(resourceName)) flatMap ResourceFile.apply match {
          case Some(resource) ⇒  inner(Tuple1(resource))(ctx)
           case other=> ctx.reject()
        }
    else ctx.reject()
  }
}
