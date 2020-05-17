package persistence.model

import java.time.OffsetDateTime
import java.util.UUID

import commons.{AppUtils, AuthUtils}
import play.api.libs.json.{Format, Json}

import scala.util.Random

case class UserEntity(
    id: Option[UUID],
    username: String,
    passwordHash: String,
    passwordSalt: String,
    role: UserRole.Value,
    email: String,
    emailConfirmedAt: Option[OffsetDateTime],
    activationCode: String,
    ts: OffsetDateTime,
    lm: OffsetDateTime,
    v: Int
)

object UserEntity {
  implicit val jsonFormat: Format[UserEntity] = Json.format[UserEntity]

  def generate(username: String, password: String, email: String, role: UserRole.Value = UserRole.User): UserEntity = {
    val salt         = AppUtils.randomAlphanumeric(16)
    val passwordHash = AuthUtils.createHash(password, salt)
    val activationCode = (Random.alphanumeric take 5).mkString
    UserEntity(
      id = Option(UUID.randomUUID()),
      username = username,
      passwordHash = passwordHash,
      passwordSalt = salt,
      role = role,
      email = email,
      emailConfirmedAt = None,
      activationCode = activationCode,
      ts = AppUtils.now,
      lm = AppUtils.now,
      v = 0
    )
  }
}
