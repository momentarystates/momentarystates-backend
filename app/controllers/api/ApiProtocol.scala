package controllers.api

import java.time.OffsetDateTime
import java.util.UUID

import persistence.model.{UserEntity, UserRole}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads.{email, maxLength, minLength}
import play.api.libs.json._

object ApiProtocol {

  case class RegisterUser(
      username: String,
      password: String,
      email: String
  )

  object RegisterUser {

    implicit val jsonReads: Reads[RegisterUser] = (
      (__ \ "username").read[String](minLength[String](3) keepAnd maxLength[String](32)) and
        (__ \ "password").read[String](minLength[String](6) keepAnd maxLength[String](128)) and
        (__ \ "email").read[String](email)
    )(RegisterUser.apply _)

    implicit val jsonWrites: Writes[RegisterUser] = Json.writes[RegisterUser]
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
