package services

import commons._
import controllers.AppErrors
import javax.inject.{Inject, Singleton}
import persistence.dao.EmailDao
import persistence.model.{EmailEntity, EmailStatus}
import play.api.{Configuration, Logger}
import play.api.libs.mailer._
import scalaz.Scalaz._
import scalaz.{-\/, \/, \/-}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class EmailService @Inject()(
    mailerClient: MailerClient,
    emailDao: EmailDao,
    config: Configuration
) {

  private val logger = Logger(classOf[EmailService])

  def send(email: EmailEntity) = {

    val senderEmail = config.get[String]("app.email.senderEmail")
    val senderName  = config.get[String]("app.email.senderName")

    def setEmailSuccess(sentEmail: EmailEntity, messageId: String) = {
      logger.info(s"email with id=${sentEmail.id.get} has been sent successfully")
      emailDao.update(email.copy(status = EmailStatus.Sent, messageId = Option(messageId))) map {
        case Left(error)         => -\/(AppErrors.DatabaseError(error))
        case Right(updatedEmail) => \/-(updatedEmail)
      }
    }

    def setEmailError(sentEmail: EmailEntity, error: String): Future[BaError \/ EmailEntity] = {
      logger.info(s"error sending email with id=${sentEmail.id.get}. error: $error")
      emailDao.update(sentEmail.copy(retries = sentEmail.retries + 1)) map {
        case Left(error) => -\/(AppErrors.DatabaseError(error))
        case Right(_)    => -\/(AppErrors.MailerError)
      }
    }

    def sendEmail() = {
      val from = s"$senderName<$senderEmail>"
      val em = Email(
        subject = email.subject,
        from = from,
        to = email.recipients,
        bodyText = None,
        bodyHtml = Option(email.body)
      )
      try {
        val messageId = mailerClient.send(em)
        setEmailSuccess(email, messageId)
      } catch {
        case ex: Exception =>
          ex.printStackTrace()
          setEmailError(email, ex.getMessage)
      }
    }

    val res = for {
      sentEmail <- BaResult[EmailEntity](sendEmail())
    } yield sentEmail

    res.run
  }
}
