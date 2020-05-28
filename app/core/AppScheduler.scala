package core

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem}
import javax.inject.{Inject, Named, Singleton}
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

@Singleton
class AppScheduler @Inject()(
    actorSystem: ActorSystem,
    config: Configuration,
    @Named("email-worker-actor") emailWorkerActor: ActorRef,
    @Named("game-engine-actor") gameEngineActor: ActorRef
) {

  private val logger = Logger(classOf[AppScheduler])

  private val startEmailWorker = config.get[Boolean]("app.email.startWorker")

  def startup(): Unit = {
    logger.info("")
    logger.info("")
    logger.info("startup dgdg backend")
    logger.info("")
    startupEmailWorker()
    startupGameEngineActor()
  }

  private def startupEmailWorker(): Unit = {

    if (startEmailWorker) {
      actorSystem.scheduler.scheduleWithFixedDelay(
        initialDelay = FiniteDuration(30, TimeUnit.SECONDS),
        delay = FiniteDuration(30, TimeUnit.SECONDS),
        receiver = emailWorkerActor,
        message = "tick"
      )
      logger.info("email worker started.")
    } else {
      logger.info("email notification worker is disabled.")
    }
  }

  private def startupGameEngineActor(): Unit = {
    logger.info("startup game-engine-actor")
    val engineTickInterval = FiniteDuration(20, TimeUnit.SECONDS)
    actorSystem.scheduler.scheduleWithFixedDelay(
      initialDelay = engineTickInterval,
      delay = engineTickInterval,
      receiver = gameEngineActor,
      message = "tick"
    )
  }

  startup()
}
