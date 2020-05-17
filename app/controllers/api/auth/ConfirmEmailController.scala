package controllers.api.auth

import commons._
import controllers.api.ApiProtocol.{ConfirmEmail, User}
import controllers.{AppActions, AppErrors, ControllerHelper}
import javax.inject.{Inject, Singleton}
import persistence.dao.UserDao
import persistence.model.UserEntity
import play.api.mvc.{AbstractController, ControllerComponents, EssentialAction}
import scalaz.Scalaz._

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class ConfirmEmailController @Inject()(
    cc: ControllerComponents,
    actions: AppActions,
    userDao: UserDao
) extends AbstractController(cc)
    with ControllerHelper {

  def confirm: EssentialAction = actions.UserAwareAction.async(parse.json) { implicit request =>
    def checkUser(user: UserEntity) = {
      request.authOpt match {
        case Some(auth) => auth.user.id.get == user.id.get
        case _          => true
      }
    }

    val res = for {
      in          <- AppResult[ConfirmEmail](validateJson[ConfirmEmail](request))
      user        <- userDao.byEmail(in.email).handleEntityNotFound("user")
      _           <- AppResult(checkUser(user))(AppErrors.InvalidUserError)
      _           <- AppResult(user.confirmationCode == in.code)(AppErrors.InvalidActivationCode)
      updatedUser <- userDao.update(user.copy(emailConfirmedAt = Option(AppUtils.now))).toAppResult()
    } yield User.fromUserEntity(updatedUser)

    res.runResult
  }

}
