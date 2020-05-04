package controllers.api.auth

import commons.AppActions
import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, ControllerComponents, EssentialAction}

import scala.concurrent.Future

@Singleton
class LoginController @Inject()(
    cc: ControllerComponents,
    appActions: AppActions
) extends AbstractController(cc) {

  def login(): EssentialAction = appActions.LoggingAction.async(parse.json) { implicit request =>
    Future.successful(NotImplemented)
  }

}
