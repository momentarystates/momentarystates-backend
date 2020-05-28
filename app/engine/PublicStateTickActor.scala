package engine

import akka.actor.{Actor, ActorLogging}
import javax.inject.{Inject, Singleton}
import persistence.model.PublicStateEntity
import play.api.Logger

@Singleton
class PublicStateTickActor @Inject()() extends Actor with ActorLogging {

  private val logger = Logger(classOf[PublicStateTickActor])

  override def receive: Receive = {
    case publicState: PublicStateEntity => tick(publicState)
    case _                              => // do nothing
  }

  private def tick(publicState: PublicStateEntity) = {
    logger.info("simulate public state tick")
  }
}
