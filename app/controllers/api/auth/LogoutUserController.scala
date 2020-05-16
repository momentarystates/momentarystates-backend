package controllers.api.auth

import controllers.AppActions
import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, ControllerComponents, EssentialAction}
import scalaz.{-\/, \/-}
import services.AuthService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class LogoutUserController @Inject()(
    cc: ControllerComponents,
    actions: AppActions,
    authService: AuthService
) extends AbstractController(cc) {

  def logout: EssentialAction = actions.AuthenticatedAction.async { implicit request =>
    request.session.get("token") match {
      case Some(token) =>
        authService.validateAuthToken(token) flatMap {
          case -\/(_) => Future.successful(Ok.withNewSession)
          case \/-(_) => authService.invalidateAuthToken(token).map(_ => Ok.withNewSession)
        }
      case _ => Future.successful(Ok.withNewSession.withHeaders("Clear-Site-Data" -> """"cache", "cookies", "storage", "executionContexts""""))
    }
  }

}
