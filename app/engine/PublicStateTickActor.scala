package engine

import akka.actor.{Actor, ActorLogging}
import commons.AppUtils
import javax.inject.{Inject, Singleton}
import persistence.dao.PublicStateDao
import persistence.model.{PublicStateEntity, PublicStateStatus}
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class PublicStateTickActor @Inject()(
    publicStateDao: PublicStateDao
) extends Actor
    with ActorLogging {

  private val logger = Logger(classOf[PublicStateTickActor])

  override def receive: Receive = {
    case publicState: PublicStateEntity => tick(publicState)
    case _                              => // do nothing
  }

  private def tick(publicState: PublicStateEntity): Unit = {
    logger.info("process public state: " + publicState.name + " - " + publicState.id.get)
    val now = AppUtils.now
    publicState.startedAt match {
      case Some(startedAt) =>
        if (startedAt.plusSeconds(publicState.params.speculationDuration).isBefore(now)) {
          // automatically stop the speculation
          val updatedPublicState = publicState.copy(status = PublicStateStatus.Finished)
          publicStateDao.update(updatedPublicState) map {
            case Left(error) => logger.error("unable to finish public state: " + publicState.name + " - " + publicState.id.get + ". Error: " + error)
            case Right(_)    => logger.info("public state has been finished: " + publicState.name + " - " + publicState.id.get)
          }
        } else {
          // continue with other processing steps
        }
      case _ => // do nothing
    }
  }
}
