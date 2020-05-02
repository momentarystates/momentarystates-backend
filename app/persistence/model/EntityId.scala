package persistence.model

import java.util.UUID

import play.api.libs.json.{JsString, Reads, Writes}
import play.api.mvc.PathBindable

private[model] trait EntityId[T] {
  implicit final def jsonReads: Reads[T]   = Reads.of[UUID].map(apply)
  implicit final def jsonWrites: Writes[T] = (id: T) => JsString(id.toString)

  final def generate(): T = apply(UUID.randomUUID())

  implicit final def pathBinder(implicit uuidBinder: PathBindable[UUID]): PathBindable[T] =
    new PathBindable[T] {
      override def bind(key: String, value: String): Either[String, T] = uuidBinder.bind(key, value).map(apply)

      override def unbind(key: String, t: T): String = t.toString
    }

  def apply(uuid: UUID): T
}
