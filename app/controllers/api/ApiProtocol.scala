package controllers.api

import java.time.OffsetDateTime
import java.util.UUID

import persistence.model.{UserEntity, UserRole}
import play.api.libs.json.{Format, Json}

object ApiProtocol {

  case class RegisterUser(
      username: String,
      password: String,
      email: String
  )

  object RegisterUser {
    implicit val jsonFormat: Format[RegisterUser] = Json.format[RegisterUser]
  }

  case class LoginUser(
      username: String,
      password: String
  )

  object LoginUser {
    implicit val jsonFormat: Format[LoginUser] = Json.format[LoginUser]
  }

  case class ConfirmEmail(
      email: String,
      code: String
  )

  object ConfirmEmail {
    implicit val jsonFormat: Format[ConfirmEmail] = Json.format[ConfirmEmail]
  }

  case class User(
      id: UUID,
      username: String,
      email: String,
      confirmed: Boolean,
      role: UserRole.Value,
      ts: OffsetDateTime,
      lm: OffsetDateTime,
      v: Int
  )

  object User {
    implicit val jsonFormat: Format[User] = Json.format[User]

    def fromUserEntity(entity: UserEntity): User = User(
      id = entity.id.get,
      username = entity.username,
      email = entity.email,
      confirmed = entity.emailConfirmedAt.isDefined,
      role = entity.role,
      ts = entity.ts,
      lm = entity.lm,
      v = entity.v
    )
  }

}
