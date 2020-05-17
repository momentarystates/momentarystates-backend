import controllers.AppErrors
import scalaz.{-\/, \/-, EitherT}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

package object commons {

  type AppResult[A] = EitherT[Future, AppError, A]

  implicit class DatabaseResult[A](value: Future[Either[String, A]]) {
    def toAppResult()(implicit ec: ExecutionContext): AppResult[A] = {
      val res = value map {
        case Left(error) => -\/(AppErrors.DatabaseError(error))
        case Right(v)    => \/-(v)
      }
      EitherT(res)
    }
  }

  implicit class OptionalEntity[A](value: Future[Option[A]]) {
    def handleEntityNotFound(name: String)(implicit ec: ExecutionContext): AppResult[A] = {
      val res = value map {
        case Some(v) => \/-(v)
        case _       => -\/(AppErrors.EntityNotFoundError(name))
      }
      EitherT(res)
    }
  }

  implicit def databaseResult[A](value: Future[Either[String, A]])(implicit ec: ExecutionContext): AppResult[A] = {
    val res = value map {
      case Left(error) => -\/(AppErrors.DatabaseError(error))
      case Right(v)    => \/-(v)
    }
    EitherT(res)
  }
}
