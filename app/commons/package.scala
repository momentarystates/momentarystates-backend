import controllers.AppErrors
import scalaz.{-\/, \/-, EitherT}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

package object commons {

  type BaResult[A] = EitherT[Future, BaError, A]

  implicit def databaseResult[A](value: Future[Either[String, A]])(implicit ec: ExecutionContext): BaResult[A] = {
    val res = value map {
      case Left(error) => -\/(AppErrors.DatabaseError(error))
      case Right(v)    => \/-(v)
    }
    EitherT(res)
  }
}
