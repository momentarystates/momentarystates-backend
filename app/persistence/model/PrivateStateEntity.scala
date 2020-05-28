package persistence.model

import java.time.OffsetDateTime
import java.util.UUID

import play.api.libs.json.{Format, Json}

case class PrivateStateEntity(
    id: Option[UUID],
    name: String,
    logo: Option[UUID],
    ts: OffsetDateTime,
    lm: OffsetDateTime,
    v: Int
)

object PrivateStateEntity {
  implicit val jsonFormat: Format[PrivateStateEntity] = Json.format[PrivateStateEntity]

  
}