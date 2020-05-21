package core

import actors.EmailWorkerActor
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import services.S3Service
import services.impl.AlpakkaS3Service

class AppModules extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {

    // scheduler
    bind(classOf[AppScheduler]).asEagerSingleton()

    // actors
    bindActor[EmailWorkerActor](name = "email-worker-actor")

    // services
    bind(classOf[S3Service]).to(classOf[AlpakkaS3Service])
  }
}
