package controllers.api.auth

import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, ControllerComponents, EssentialAction}

import scala.concurrent.Future

@Singleton
class RegisterUserController @Inject()(
    cc: ControllerComponents
) extends AbstractController(cc) {

  def register(): EssentialAction = Action.async(parse.json) { implicit request =>
    Future.successful(NotImplemented)
  }

  def registerAnon(): EssentialAction = Action.async(parse.json) { implicit request =>
    Future.successful(NotImplemented)
  }
}
