package controllers

import javax.inject.{Inject, Singleton}
import persistence.dao.EmailDao
import persistence.model.EmailEntity
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents, EssentialAction, Result}
import scalaz.{-\/, \/-}
import services.EmailService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class PlaygroundController @Inject()(
    cc: ControllerComponents,
    actions: AppActions,
    emailDao: EmailDao,
    emailService: EmailService
) extends AbstractController(cc) {

  def exp(num: Int): EssentialAction = actions.LoggingAction.async { implicit request =>
    exp1
  }

  private def exp1: Future[Result] = {
    val body =
      """
        |<h2>Test Email</h2>
        |<br/>
        |<p>
        |This is a test email sent for debugging purposes
        |</p>
        |""".stripMargin
    val email = EmailEntity.generate("Test Email", List("markus@toto.io", "markus@silverorbit.de"), body)
    emailDao.insert(email) flatMap {
      case Left(error) => Future.successful(BadRequest(error))
      case Right(uuid) =>
        emailService.send(email.copy(id = Option(uuid))) map {
          case -\/(error)     => BadRequest(Json.toJson(error))
          case \/-(sentEmail) => Ok(Json.toJson(sentEmail))
        }
    }
  }
}
