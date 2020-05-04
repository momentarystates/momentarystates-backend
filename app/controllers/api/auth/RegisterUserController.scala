package controllers.api.auth

import commons.{AppActions, BaResult}
import controllers.AppErrors.DatabaseError
import controllers.ControllerHelper
import controllers.api.ApiProtocol.RegisterUser
import javax.inject.{Inject, Singleton}
import persistence.dao.UserDao
import persistence.model.UserEntity
import play.api.mvc.{AbstractController, ControllerComponents, EssentialAction}
import scalaz.Scalaz._
import scalaz.{-\/, \/-}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class RegisterUserController @Inject()(
    cc: ControllerComponents,
    appActions: AppActions,
    userDao: UserDao
) extends AbstractController(cc)
    with ControllerHelper {

  def register(): EssentialAction = appActions.LoggingAction.async(parse.json) { implicit request =>
    def registerUser(in: RegisterUser) = {
      val user = UserEntity.generate(in.username, in.password)
      userDao.insert(user) map {
        case Left(error) => -\/(DatabaseError(error))
        case Right(uuid) => \/-(user.copy(id = Option(uuid)))
      }
    }

    val res = for {
      in   <- BaResult[RegisterUser](validateJson[RegisterUser](request))
      user <- BaResult[UserEntity](registerUser(in))
    } yield user

    res.runResultEmptyOk()
  }

  def registerAnon(): EssentialAction = appActions.LoggingAction.async(parse.json) { implicit request =>
    Future.successful(NotImplemented)
  }
}
