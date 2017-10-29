package akka.http.extensions.pjax

import akka.http.scaladsl.server.{Directive1, Directive}

case class PJaxMagnet[Template<:TemplateEngine](directive: Directive1[Template#Html])

trait TemplateEngine{
  type Html
}

trait PJax {
  def pjax[Template<:TemplateEngine](magnet: PJaxMagnet[Template]): Directive[Tuple1[Template#Html]] = magnet.directive
}