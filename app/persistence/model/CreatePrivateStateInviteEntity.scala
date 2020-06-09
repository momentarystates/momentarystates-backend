package persistence.model

import java.time.OffsetDateTime
import java.util.UUID

import commons.AppUtils
import play.api.libs.json.{Format, Json}

case class CreatePrivateStateInviteEntity(
    id: Option[UUID],
    publicStateId: UUID,
    token: String,
    email: String,
    usedAt: Option[OffsetDateTime],
    usedBy: Option[UUID],
    ts: OffsetDateTime,
    lm: OffsetDateTime,
    v: Int
)

object CreatePrivateStateInviteEntity {
  implicit val jsonFormat: Format[CreatePrivateStateInviteEntity] = Json.format[CreatePrivateStateInviteEntity]

  def generate(publicStateId: UUID, email: String): CreatePrivateStateInviteEntity = {
    val now = AppUtils.now
    CreatePrivateStateInviteEntity(
      id = Option(UUID.randomUUID()),
      publicStateId = publicStateId,
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
