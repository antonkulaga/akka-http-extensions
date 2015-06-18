package akka.http.extensions.security

import akka.http.scaladsl.server._


trait AuthDirectives {

  def withSession(magnet: SessionMagnet): Directive[Tuple1[String]] = magnet.directive

  def withLogin(magnet: LoginMagnet): Directive1[LoginInfo] = magnet.directive

  def withRegistration(magnet:RegisterMagnet): Directive1[LoginInfo] = magnet.directive

}

case class ReadErrorRejection(message:String,exception:Throwable) extends Rejection








