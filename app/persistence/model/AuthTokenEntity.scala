package persistence.model

import java.time.OffsetDateTime
import java.util.UUID

import persistence.DaoDefinitions
import play.api.libs.json.{Format, Json}

case class AuthTokenEntity(
    id: Option[UUID],
    token: String,
    userId: UUID,
    valid: Boolean,
    expires: Option[OffsetDateTime],
    remoteAddress: String,
    ts: OffsetDateTime,
    lm: OffsetDateTime,
    v: Int
)

object AuthTokenEntity extends DaoDefinitions {
  implicit val jsonFormat: Format[AuthTokenEntity] = Json.format[AuthTokenEntity]

  def generate(user: UserEntity, remoteAddress: String, expires: Option[OffsetDateTime] = None): AuthTokenEntity = {
    AuthTokenEntity(
      id = Option(UUID.randomUUID()),
      token = UUID.randomUUID().toString,
      userId = user.id.get,
      valid = true,
      expires = expires,
      remoteAddress = remoteAddress,
      ts = now,
      lm = now,
      v = 0
    )
  }
}
