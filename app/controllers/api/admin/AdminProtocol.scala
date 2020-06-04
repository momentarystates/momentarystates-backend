package controllers.api.admin

import java.time.OffsetDateTime
import java.util.UUID

import persistence.model.{UserEntity, UserRole}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads.email
import play.api.libs.json._

object AdminProtocol {

  case class AdminUser(
      id: UUID,
      username: String,
      role: UserRole.Value,
      email: String,
      emailConfirmedAt: Option[OffsetDateTime],
      confirmationCode: String,
      ts: OffsetDateTime,
      lm: OffsetDateTime,
      v: Int
  )

  object AdminUser {
    implicit val jsonFormat: Format[AdminUser] = Json.format[AdminUser]

    def fromUserEntity(entity: UserEntity): AdminUser = {
      AdminUser(
        id = entity.id.get,
        username = entity.username,
        role = entity.role,
        email = entity.email,
        emailConfirmedAt = entity.emailConfirmedAt,
        confirmationCode = entity.confirmationCode,
        ts = entity.ts,
        lm = entity.lm,
        v = entity.v
      )
    }
  }

  case class CreateSpeculation(
      email: String,
      token: Option[String]
  )

  object CreateSpeculation {
    implicit val jsonReads: Reads[CreateSpeculation] = (
      (__ \ "email").read[String](email) and
        (__ \ "token").readNullable[String]
    )(CreateSpeculation.apply _)

    implicit val jsonWrites: Writes[CreateSpeculation] = Json.writes[CreateSpeculation]
  }

}
