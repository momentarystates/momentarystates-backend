package commons

import commons.BaResult.wrapFutureFailure
import controllers.AppErrors
import play.api.Logger
import scalaz.{-\/, EitherT, \/, \/-}

import scala.concurrent.{ExecutionContext, Future}

object BaResult {

  private val logger = Logger(this.getClass)

  val unit: BaResult[Unit] = BaResult[Unit](())(ExecutionContext.global)

  def apply[A](future: Future[BaError \/ A]): BaResult[A] = EitherT(future)

  def apply[A](value: => A)(implicit ec: ExecutionContext): BaResult[A] = BaResult[A](Future(\/-(value)))

  def apply[A](error: BaError): BaResult[A] = BaResult(Future.successful(-\/(error)))

  def apply[A](value: BaError \/ A): BaResult[A] = EitherT(Future.successful(value))

  def apply(value: Boolean)(error: => BaError): BaResult[_] = {
    val result =
      if (value)
        \/-(true)
      else
        -\/(error)

    BaResult(Future.successful(result))
  }

//  def fromBoolean(value: Boolean)(error: => BaError): BaResult[_] = {
//    val result =
//      if (value)
//        \/-(true)
//      else
//        -\/(error)
//
//    BaResult(Future.successful(result))
//  }

  private val wrapFutureFailure: PartialFunction[Throwable, -\/[BaError]] = {
    case exception =>
      logger.error("There was an error converting the future to BeResult", exception)
      -\/(BaError(s"Something went wrong. ${exception.getMessage}"))
  }

  def fromOption[A](maybeA: Option[A])(error: => BaError)(implicit ec: ExecutionContext): BaResult[A] = {
    maybeA match {
      case Some(a) => BaResult[A](a)
      case None    => BaResult[A](error)
    }
  }

  def fromFuture[A](futureValue: Future[A])(implicit ec: ExecutionContext): BaResult[A] = {
    val wrappedFuture: Future[BaError \/ A] = futureValue.map(\/.right[BaError, A]).recover(wrapFutureFailure)

    BaResult(wrappedFuture)
  }

  def fromFutureOption[A](value: Future[Option[A]])(error: => BaError)(implicit ec: ExecutionContext): BaResult[A] = {
    def wrapInDisjunction(maybeA: Option[A]): BaError \/ A = maybeA match {
      case Some(a) => \/-(a)
      case None    => -\/(error)
    }

    val e: Future[BaError \/ A] = value.map(wrapInDisjunction).recover(wrapFutureFailure)

    BaResult(e)
  }
}
