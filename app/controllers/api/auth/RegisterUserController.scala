package controllers.api.auth

import commons.AppResult
import controllers.AppErrors.DatabaseError
import controllers.api.ApiProtocol.RegisterUser
import controllers.{AppActions, AppErrors, ControllerHelper}
import javax.inject.{Inject, Singleton}
import persistence.dao.{EmailDao, UserDao}
import persistence.model.{EmailEntity, UserEntity}
import play.api.mvc.{AbstractController, ControllerComponents, EssentialAction}
import scalaz.Scalaz._
import scalaz.{-\/, \/-}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class RegisterUserController @Inject()(
    cc: ControllerComponents,
    appActions: AppActions,
    userDao: UserDao,
    emailDao: EmailDao
) extends AbstractController(cc)
    with ControllerHelper {

  def register(): EssentialAction = appActions.LoggingAction.async(parse.json) { implicit request =>
    def registerUser(in: RegisterUser) = {
      val user = UserEntity.generate(in.username, in.password, in.email)
      userDao.insert(user) map {
        case Left(error) => -\/(DatabaseError(error))
        case Right(uuid) => \/-(user.copy(id = Option(uuid)))
      }
    }

    def sendEmailConfirmation(user: UserEntity) = {
      val subject = "DGDG - user registration"
      val body =
        s"""
          |Hi ${user.username},
          |<p>You have just registered. Please confirm your email address.</p>
          |<p>Your code is: ${user.confirmationCode}</p>
          |<p>have fun ;-)</p>
          |<p>your dgdg admin</p>
          |""".stripMargin
      val email = EmailEntity.generate(subject, List(user.email), body)
      emailDao.insert(email) map {
        case Left(error) => -\/(AppErrors.DatabaseError(error))
        case Right(uuid) => \/-(email.copy(id = Option(uuid)))
      }
    }

    val res = for {
      in   <- AppResult[RegisterUser](validateJson[RegisterUser](request))
      user <- AppResult[UserEntity](registerUser(in))
      _    <- AppResult[EmailEntity](sendEmailConfirmation(user))
    } yield user

    res.runResultEmptyOk()
  }
}
