package persistence.model

import java.time.OffsetDateTime
import java.util.UUID

import commons.AppUtils
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
    startedAt: OffsetDateTime,
    endedAt: Option[OffsetDateTime],
    endReason: Option[CitizenshipEndReason.Value],
    ts: OffsetDateTime,
    lm: OffsetDateTime,
    v: Int
)

object CitizenEntity {
  implicit val jsonFormat: Format[CitizenEntity] = Json.format[CitizenEntity]

  def generate(user: UserEntity, privateState: PrivateStateEntity, name: Option[String]): CitizenEntity = {
    val now = AppUtils.now
    CitizenEntity(
      id = Option(UUID.randomUUID),
      userId = user.id.get,
      privateStateId = privateState.id.get,
      name = name.getOrElse(user.username),
      startedAt = now,
      endedAt = None,
      endReason = None,
      ts = now,
      lm = now,
      v = 0
    )
  }
}
