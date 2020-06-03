package controllers.api.admin

import controllers.AppActions
import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, ControllerComponents, EssentialAction}
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class CreatePublicStateController @Inject()(
    cc: ControllerComponents,
    actions: AppActions
) extends AbstractController(cc) {

  def create(): EssentialAction = actions.AdminAction().async(parse.json) { implicit request =>




    ???
  }

}
