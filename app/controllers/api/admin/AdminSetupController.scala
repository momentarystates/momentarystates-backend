package controllers.api.admin

import commons._
import controllers.{AppActions, AppErrors, ControllerHelper}
import javax.inject.{Inject, Singleton}
import persistence.dao.UserDao
import persistence.model.UserRole
import play.api.Configuration
import play.api.mvc.{AbstractController, ControllerComponents, EssentialAction}
import scalaz.Scalaz._

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class AdminSetupController @Inject()(
    cc: ControllerComponents,
    actions: AppActions,
    config: Configuration,
    userDao: UserDao
) extends AbstractController(cc)
    with ControllerHelper {

  def setup(token: String): EssentialAction = actions.AuthenticatedAction.async { implicit request =>
    val tokenParam = config.get[String]("app.setup.token")

    val res = for {
      _ <- AppResult(tokenParam == token)(AppErrors.InvalidAdminSetupToken)
      _ <- userDao.update(request.auth.user.copy(role = UserRole.Admin)).toAppResult()
    } yield ""

    res.runResultEmptyOk()
  }
}
