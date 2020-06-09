package persistence.model

import java.time.OffsetDateTime
import java.util.UUID

import commons.AppUtils
import play.api.libs.json.{Format, Json}

case class JoinPrivateStateInviteEntity(
    id: Option[UUID],
    privateStateId: UUID,
    token: String,
    email: String,
    usedAt: Option[OffsetDateTime],
    usedBy: Option[UUID],
    ts: OffsetDateTime,
    lm: OffsetDateTime,
    v: Int
)

object JoinPrivateStateInviteEntity {
  implicit val jsonFormat: Format[JoinPrivateStateInviteEntity] = Json.format[JoinPrivateStateInviteEntity]

  def generate(privateStateId: UUID, email: String): JoinPrivateStateInviteEntity = {
    val now = AppUtils.now
    JoinPrivateStateInviteEntity(
      id = Option(UUID.randomUUID()),
      privateStateId = privateStateId,
      token = AppUtils.randomAlphanumeric(5),
      email = email,
      usedAt = None,
      usedBy = None,
      ts = now,
      lm = now,
      v = 0
    )
  }
}
