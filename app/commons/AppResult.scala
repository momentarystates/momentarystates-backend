package commons

import play.api.Logger
import scalaz.{-\/, EitherT, \/, \/-}

import scala.concurrent.{ExecutionContext, Future}

object AppResult {

  private val logger = Logger(this.getClass)

  val unit: AppResult[Unit] = AppResult[Unit](())(ExecutionContext.global)

  def apply[A](future: Future[AppError \/ A]): AppResult[A] = EitherT(future)

  def apply[A](value: => A)(implicit ec: ExecutionContext): AppResult[A] = AppResult[A](Future(\/-(value)))

  def apply[A](error: AppError): AppResult[A] = AppResult(Future.successful(-\/(error)))

  def apply[A](value: AppError \/ A): AppResult[A] = EitherT(Future.successful(value))

  def apply(value: Boolean)(error: => AppError): AppResult[_] = {
    val result =
      if (value)
        \/-(true)
      else
        -\/(error)

    AppResult(Future.successful(result))
  }

  private val wrapFutureFailure: PartialFunction[Throwable, -\/[AppError]] = {
    case exception =>
      logger.error("There was an error converting the future to BeResult", exception)
      -\/(AppError("app:0", s"Something went wrong. ${exception.getMessage}"))
  }

  def fromOption[A](maybeA: Option[A])(error: => AppError)(implicit ec: ExecutionContext): AppResult[A] = {
    maybeA match {
      case Some(a) => AppResult[A](a)
      case None    => AppResult[A](error)
    }
  }

  def fromFuture[A](futureValue: Future[A])(implicit ec: ExecutionContext): AppResult[A] = {
    val wrappedFuture: Future[AppError \/ A] = futureValue.map(\/.right[AppError, A]).recover(wrapFutureFailure)

    AppResult(wrappedFuture)
  }

  def fromFutureOption[A](value: Future[Option[A]])(error: => AppError)(implicit ec: ExecutionContext): AppResult[A] = {
    def wrapInDisjunction(maybeA: Option[A]): AppError \/ A = maybeA match {
      case Some(a) => \/-(a)
      case None    => -\/(error)
    }

    val e: Future[AppError \/ A] = value.map(wrapInDisjunction).recover(wrapFutureFailure)

    AppResult(e)
  }
}
