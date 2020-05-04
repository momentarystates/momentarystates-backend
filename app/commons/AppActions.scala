package commons

import java.util.Date

import com.google.inject.Inject
import play.api.Logger
import play.api.mvc.{ActionBuilder, ActionBuilderImpl, AnyContent, BodyParsers, Request, Result}

import scala.concurrent.{ExecutionContext, Future}

class AppActions @Inject()(
    loggingActionBuilder: LoggingActionBuilder
) {
  def LoggingAction: ActionBuilder[Request, AnyContent] = loggingActionBuilder
}

class LoggingActionBuilder @Inject()(parser: BodyParsers.Default)(implicit ec: ExecutionContext) extends ActionBuilderImpl(parser) {

  private val logger = Logger(classOf[LoggingActionBuilder])

  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
    val start = (new Date).getTime
    logger.info(s"[${request.id}] ${request.method} ${request.uri}")
    block(request) map { res =>
      val duration = (new Date).getTime - start
      logger.info(s"[${request.id}] took: " + duration + "ms")
      res
    }
  }
}
