package core

import actors.EmailWorkerActor
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

class AppModules extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {

    // scheduler
    bind(classOf[AppScheduler]).asEagerSingleton()

    // actors
    bindActor[EmailWorkerActor](name = "email-worker-actor")
  }
}
