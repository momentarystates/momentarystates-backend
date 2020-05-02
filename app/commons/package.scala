import scalaz.EitherT

import scala.concurrent.Future

package object commons {

  type BaResult[A] = EitherT[Future, BaError, A]
}
