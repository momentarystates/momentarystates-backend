package persistence.model

import java.time.OffsetDateTime
import java.util.UUID

import play.api.libs.json.{Format, Json}

case class CitizenEntity(
    id: Option[UUID],
    userId: UUID,
    privateStateId: UUID,
    startedAt: OffsetDateTime,
    endedAt: Option[OffsetDateTime],
    ts: OffsetDateTime,
    lm: OffsetDateTime,
    v: Int
)

object CitizenEntity {
  implicit val jsonFormat: Format[CitizenEntity] = Json.format[CitizenEntity]
}
