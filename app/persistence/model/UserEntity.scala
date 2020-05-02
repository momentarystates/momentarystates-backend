package persistence.model

import java.time.OffsetDateTime
import java.util.UUID

import play.api.libs.json.{Format, Json}

case class UserEntity(
    id: Option[UUID],
    username: String,
    passwordHash: String,
    ts: OffsetDateTime,
    lm: OffsetDateTime,
    v: Int
)

object UserEntity {
  implicit val jsonFormat: Format[UserEntity] = Json.format[UserEntity]
  def generate(username: String, passwordHash: String): UserEntity = {
    val now = OffsetDateTime.now
    UserEntity(
      id = Option(UUID.randomUUID()),
      username = username,
      passwordHash = passwordHash,
      ts = now,
      lm = now,
      v = 0
    )
  }
}
