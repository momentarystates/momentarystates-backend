package controllers.api

import akka.stream.Materializer
import akka.stream.alpakka.s3.scaladsl.S3
import controllers.AppActions
import javax.inject.{Inject, Singleton}
import persistence.dao.EmailDao
import persistence.model.EmailEntity
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents, EssentialAction, Result}
import play.api.{Configuration, Logger}
import scalaz.{-\/, \/-}
import services.EmailService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class PlaygroundController @Inject()(
    cc: ControllerComponents,
    actions: AppActions,
    emailDao: EmailDao,
    emailService: EmailService,
    config: Configuration,
    materializer: Materializer
) extends AbstractController(cc) {

  private val logger = Logger(classOf[PlaygroundController])

  def exp(num: Int): EssentialAction = actions.LoggingAction.async { implicit request =>
    num match {
      case 1 => exp1
      case 2 => exp2
      case 3 => exp3
      case _ => Future.successful(BadRequest("unknown exp number: " + num))
    }
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

  private def exp2: Future[Result] = {

    logger.info("config")
    logger.info("\talpakka.s3.buffer: " + config.get[String]("alpakka.s3.buffer"))
    logger.info("\talpakka.s3.aws.credentials.secret-access-key: " + config.getOptional[String]("alpakka.s3.aws.credentials.secret-access-key"))

    Future.successful(Ok)
  }

  private def exp3: Future[Result] = {
    val bucketName = "some-test-bucket"
    try {
      S3.makeBucket(bucketName)(materializer) map { _ =>
        Ok("bucket created")
      }
    } catch {
      case e: Exception =>
        logger.info("error error: " + e.getMessage)
        Future.successful(BadRequest(e.getMessage))
    }
  }
}
