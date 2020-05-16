package controllers.api.auth

import controllers.AppActions
import controllers.api.ApiProtocol.User
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents, EssentialAction}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class IdentityController @Inject()(
    cc: ControllerComponents,
    actions: AppActions
) extends AbstractController(cc) {

  def identity: EssentialAction = actions.UserAwareAction.async { implicit request =>
    Future {
      request.authOpt match {
        case Some(auth) => Ok(Json.toJson(User.fromUserEntity(auth.user)))
        case _          => Ok(Json.toJson(Json.obj()))
      }
    }
  }
}
