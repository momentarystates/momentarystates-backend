package controllers.api.game

import controllers.{AppActions, ControllerHelper}
import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, ControllerComponents, EssentialAction}

@Singleton
class CreateGameController @Inject()(
    cc: ControllerComponents,
    actions: AppActions
) extends AbstractController(cc) with ControllerHelper {

  def create: EssentialAction = actions.AuthenticatedAction.async(parse.json) { implicit request =>
    ???
  }

}
