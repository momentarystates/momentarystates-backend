package persistence.model

import java.time.{Instant, OffsetDateTime, ZoneOffset}
import java.util.UUID

import commons.AuthUtil
import persistence.DaoDefinitions
import play.api.libs.json.{Format, Json}

case class UserEntity(
    id: Option[UUID],
    username: String,
    passwordHash: String,
    passwordSalt: String,
    role: UserRole.Value,
    ts: OffsetDateTime,
    lm: OffsetDateTime,
    v: Int
)

object UserEntity extends DaoDefinitions {
  implicit val jsonFormat: Format[UserEntity] = Json.format[UserEntity]

  def generate(username: String, password: String, role: UserRole.Value = UserRole.User): UserEntity = {
    val salt = randomAlphanumeric(16)
    val passwordHash = AuthUtil.createHash(password, salt)
    UserEntity(
      id = Option(UUID.randomUUID()),
      username = username,
      passwordHash = passwordHash,
      passwordSalt = salt,
      role = role,
      ts = now,
      lm = now,
      v = 0
    )
  }
}
