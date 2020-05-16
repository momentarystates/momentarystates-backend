package persistence.dao

import scala.reflect.ClassTag

private[dao] trait DaoHelper {

  final protected def wrapUpdateInEither[A: ClassTag, I](id: I, a: A)(changedRows: Int): Either[String, A] =
    changedRows match {
      case 0 =>
        val classTag  = implicitly[ClassTag[A]]
        val className = classTag.runtimeClass.getSimpleName
        Left(s"$className $id not found")
      case _ => Right(a)
    }

}
