package controllers.api.game

import controllers.AppActions
import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, ControllerComponents, EssentialAction}

import scala.concurrent.Future

@Singleton
class PrivateStateController @Inject()(
    cc: ControllerComponents,
    actions: AppActions
) extends AbstractController(cc) {

  def create(): EssentialAction = actions.AuthenticatedAction.async(parse.json) { implicit request =>
    Future.successful(NotImplemented)
  }
}
