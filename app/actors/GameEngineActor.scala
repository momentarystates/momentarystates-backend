package actors

import akka.actor.{Actor, ActorLogging, ActorRef}
import javax.inject.{Inject, Named, Singleton}
import persistence.dao.PublicStateDao
import persistence.model.PublicStateStatus
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class GameEngineActor @Inject()(
    publicStateDao: PublicStateDao,
    @Named("public-state-tick-actor") publicStateTickActor: ActorRef
) extends Actor
    with ActorLogging {

  private val logger = Logger(classOf[GameEngineActor])

  override def receive: Receive = {
    case _ => tick()
  }

  private def tick(): Unit = {

    logger.info("tick")

    publicStateDao.byStatus(PublicStateStatus.Running) map { publicStates =>
      publicStates.foreach(publicState => publicStateTickActor ! publicState)
    }
  }
}
