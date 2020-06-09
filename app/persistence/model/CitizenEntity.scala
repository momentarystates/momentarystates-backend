package persistence.model

import java.time.OffsetDateTime
import java.util.UUID

import play.api.libs.json.{Format, Json, Reads, Writes}

object CitizenshipEndReason extends Enumeration {
  val Kicked, Ended = Value

  implicit val jsonReads: Reads[CitizenshipEndReason.Value]   = Reads.enumNameReads(CitizenshipEndReason)
  implicit val jsonWrites: Writes[CitizenshipEndReason.Value] = Writes.enumNameWrites
}

case class CitizenEntity(
    id: Option[UUID],
    userId: UUID,
    privateStateId: UUID,
    name: String,
    avatar: Option[UUID],
    startedAt: OffsetDateTime,
    endedAt: Option[OffsetDateTime],
    endReason: Option[CitizenshipEndReason.Value],
    ts: OffsetDateTime,
    lm: OffsetDateTime,
    v: Int
)

object CitizenEntity {
  implicit val jsonFormat: Format[CitizenEntity] = Json.format[CitizenEntity]
}
