package actors

import akka.actor.{Actor, ActorLogging}
import commons.AppResult
import controllers.AppErrors
import javax.inject.{Inject, Singleton}
import persistence.dao.{AppParamDao, EmailDao}
import persistence.model.{AppParamEntity, EmailEntity}
import play.api.Logger
import scalaz.Scalaz._
import scalaz.{-\/, \/-}
import services.EmailService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class EmailWorkerActor @Inject()(
    emailDao: EmailDao,
    emailService: EmailService,
    appParamDao: AppParamDao
) extends Actor
    with ActorLogging {

  private val logger = Logger(classOf[EmailWorkerActor])

  override def receive: Receive = {
    case _ => doWork()
  }

  private def doWork(): Unit = {
    appParamDao.byKey(AppParamEntity.KEY_EMAIL_WORKER_BUSY) map {
      case Some(param) =>
        if (param.value == "0") processEmailQueue(param)
        else logger.info("email worker is busy. no further processing of emails.")
      case _ => logger.error("email worker busy status could not be found in database.")
    }
  }

  private def processEmailQueue(workerStatus: AppParamEntity): Unit = {

    def setEmailWorkerStatus(param: AppParamEntity, busy: Boolean) = {
      val value         = if (busy) "1" else "0"
      val modifiedParam = param.copy(value = value)
      appParamDao.update(modifiedParam) map {
        case Left(error) =>
          logger.error(s"could not update email worker status in the database. error: $error")
          -\/(AppErrors.DatabaseError(error))
        case Right(updatedParam) => \/-(updatedParam)
      }
    }

    def fetchEmails() = {
      emailDao.emailsToProcess() map { emails =>
        if (emails.isEmpty) logger.info(s"no emails to process")
        else logger.info(s"email worker is going to process ${emails.size} emails")
        emails
      }
    }

    def processSingleEmail(email: EmailEntity) = {
      logger.info(s"process single email with id=${email.id.get}")
      if (email.retries >= 3) {
        logger.info("email was already tried to be sent 3 times, so it is ignored now.")
        Future.successful(None)
      } else {
        emailService.send(email) map {
          case -\/(_)         => None
          case \/-(sentEmail) => Option(sentEmail)
        }
      }
    }

    def processEmails(emails: Seq[EmailEntity], akku: Seq[Option[EmailEntity]]): Future[Seq[Option[EmailEntity]]] = {
      if (emails.isEmpty) Future.successful(akku)
      else {
        processSingleEmail(emails.head) flatMap {
          case Some(sentEmail) =>
            Thread.sleep(1000)
            processEmails(emails.tail, akku.+:(Option(sentEmail)))
          case _ => processEmails(emails.tail, akku)
        }
      }
    }

    val res = for {
      updatedParam <- AppResult[AppParamEntity](setEmailWorkerStatus(workerStatus, busy = true))
      emails       <- AppResult.fromFuture[Seq[EmailEntity]](fetchEmails())
      processed    <- AppResult.fromFuture[Seq[Option[EmailEntity]]](processEmails(emails, Nil))
      _            <- AppResult[AppParamEntity](setEmailWorkerStatus(updatedParam, busy = false))
    } yield (emails, processed)

    res.run.map {
      case \/-(data)  => if (data._1.nonEmpty) logger.info(s"email worker processed ${data._2.size} emails.")
      case -\/(error) => logger.error(s"error during email processing: ${error.error}")
    }
  }
}
