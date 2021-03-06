package controllers.api.auth

import commons.{AppResult, EmailTemplate}
import controllers.AppErrors.DatabaseError
import controllers.api.ApiProtocol.RegisterUser
import controllers.{AppActions, AppErrors, ControllerHelper}
import javax.inject.{Inject, Singleton}
import persistence.dao.{EmailDao, UserDao}
import persistence.model.{EmailEntity, UserEntity}
import play.api.Configuration
import play.api.mvc.{AbstractController, ControllerComponents, EssentialAction}
import scalaz.Scalaz._
import scalaz.{-\/, \/-}

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class RegisterUserController @Inject()(
    cc: ControllerComponents,
    appActions: AppActions,
    userDao: UserDao,
    emailDao: EmailDao,
    config: Configuration
) extends AbstractController(cc)
    with ControllerHelper {

  def register(): EssentialAction = appActions.LoggingAction.async(parse.json) { implicit request =>
    val domain                                  = config.get[String]("app.domain")
    val confirmPath                             = config.get[String]("app.ui.confirmEmailPath")
    def confirmUrl(email: String, code: String) = domain + confirmPath.replace(":email", email).replace(":code", code)
    val registerPath                            = config.get[String]("app.ui.registerPath")
    val registerUrl                             = domain + registerPath

    def registerUser(in: RegisterUser) = {
      val user = UserEntity.generate(in.username, in.password, in.email)
      userDao.insert(user) map {
        case Left(error) => -\/(DatabaseError(error))
        case Right(uuid) => \/-(user.copy(id = Option(uuid)))
      }
    }

    def sendEmailConfirmation(user: UserEntity, in: RegisterUser) = {
      val template = EmailTemplate.getRegisterEmail(user.username, confirmUrl(user.email, user.confirmationCode), registerUrl)
      val email    = EmailEntity.generate(template.subject, Seq(user.email), template.body)
      emailDao.insert(email) map {
        case Left(error) => -\/(AppErrors.DatabaseError(error))
        case Right(uuid) => \/-(email.copy(id = Option(uuid)))
      }
    }

    val res = for {
      in   <- AppResult[RegisterUser](validateJson[RegisterUser](request))
      user <- AppResult[UserEntity](registerUser(in))
      _    <- AppResult[EmailEntity](sendEmailConfirmation(user, in))
    } yield user

    res.runResultEmptyOk()
  }
}
