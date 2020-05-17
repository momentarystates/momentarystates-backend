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

    logger.info("--------")
    logger.info("EmailService.send(...)")
    logger.info("--------")

    val senderEmail = config.get[String]("app.email.senderEmail")
    val senderName  = config.get[String]("app.email.senderName")

    def setStatusProcessing() = {
      logger.info("setStatusProcessing()")
      emailDao.update(email.copy(status = EmailStatus.Processing)) map {
        case Left(error)         => -\/(AppErrors.DatabaseError(error))
        case Right(updatedEmail) => \/-(updatedEmail)
      }
    }

    def setEmailSuccess(sentEmail: EmailEntity, messageId: String) = {
      logger.info("setEmailSuccess()")
      emailDao.update(email.copy(status = EmailStatus.Sent, messageId = Option(messageId))) map {
        case Left(error)         => -\/(AppErrors.DatabaseError(error))
        case Right(updatedEmail) => \/-(updatedEmail)
      }
    }

    def setEmailError(sentEmail: EmailEntity, error: String): Future[BaError \/ EmailEntity] = {
      logger.info("setEmailError(): " + error)
      emailDao.update(email.copy(status = EmailStatus.Error)) map {
        case Left(error) => -\/(AppErrors.DatabaseError(error))
        case Right(_)    => -\/(AppErrors.MailerError)
      }
    }

    def sendEmail(e: EmailEntity) = {
      val from = s"$senderName<$senderEmail>"
      val em = Email(
        subject = e.subject,
        from = from,
        to = e.recipients,
        bodyText = None,
        bodyHtml = Option(e.body)
      )
      try {
        logger.info("try to send an email: " + em.subject)
        logger.info("----------------")
        val messageId = mailerClient.send(em)
        logger.info("message id = " + messageId)
        setEmailSuccess(e, messageId)
      } catch {
        case ex: Exception =>
          ex.printStackTrace()
          setEmailError(e, ex.getMessage)
      }
    }

    val res = for {
      processingEmail <- BaResult[EmailEntity](setStatusProcessing())
      sentEmail       <- BaResult[EmailEntity](sendEmail(processingEmail))
    } yield sentEmail

    res.run
  }
}
