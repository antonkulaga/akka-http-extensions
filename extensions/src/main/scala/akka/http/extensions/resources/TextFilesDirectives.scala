package akka.http.extensions.resources

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ContentType
import akka.http.scaladsl.server.Directive
import akka.http.scaladsl.server.directives.ContentTypeResolver
import akka.http.scaladsl.server.directives.FileAndResourceDirectives.ResourceFile

trait TextFilesDirectives {

  def resource(resourceName: String,
               classLoader: ClassLoader = classOf[ActorSystem].getClassLoader)
              (implicit resolver: ContentTypeResolver) =  Directive[Tuple1[ResourceFile]]{ inner=>ctx=>
    if (!resourceName.endsWith("/"))
        Option(classLoader.getResource(resourceName)) flatMap ResourceFile.apply match {
          case Some(resource) ⇒  inner(Tuple1(resource))(ctx)
           case other=> ctx.reject()
        }
    else ctx.reject()
  }
}
