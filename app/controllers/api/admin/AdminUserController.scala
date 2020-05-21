package controllers.api.admin

import java.util.UUID

import commons._
import controllers.api.admin.AdminProtocol.AdminUser
import controllers.{AppActions, ControllerHelper}
import javax.inject.{Inject, Singleton}
import persistence.dao.UserDao
import play.api.mvc.{AbstractController, ControllerComponents, EssentialAction}
import scalaz.Scalaz._

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class AdminUserController @Inject()(
    cc: ControllerComponents,
    actions: AppActions,
    userDao: UserDao
) extends AbstractController(cc)
    with ControllerHelper {

  def get(userId: UUID): EssentialAction = actions.AdminAction().async { implicit request =>
    val res = for {
      user <- userDao.byId(userId).handleEntityNotFound("user")
    } yield AdminUser.fromUserEntity(user)

    res.runResult
  }
}
