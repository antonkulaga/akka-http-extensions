package akka.http.extensions.security

import akka.http.scaladsl.server._

import akka.http.scaladsl.server.Directives

/**
 * just inherit from this trait to use new directives
 */
trait AuthDirectives {

  def startSession(magnet: TokenMagnet): Directive0 = magnet.directive

  def login(magnet: LoginMagnet): Directive1[LoginInfo] = magnet.directive

  def logout: Directive0 = Directives.deleteCookie("X-Token",path="/")

  def registration(magnet:RegisterMagnet): Directive1[LoginInfo] = magnet.directive

  def allow(permission:PermissionMagnet):Directive0 = permission.directive

  def authenticate(magnet: AuthenticateMagnet): Directive1[magnet.Result] = magnet.directive

}

case class ReadErrorRejection(message:String,exception:Throwable) extends Rejection








