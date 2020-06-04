package persistence.model

import java.time.OffsetDateTime
import java.util.UUID

import commons.AppUtils
import play.api.libs.json.{Format, Json}

case class SpeculationEntity(
    id: Option[UUID],
    email: String,
    token: String,
    publicStateId: Option[UUID],
    ts: OffsetDateTime,
    lm: OffsetDateTime,
    v: Int
)

object SpeculationEntity {
  implicit val jsonFormat: Format[SpeculationEntity] = Json.format[SpeculationEntity]

  def generate(email: String, tokenOpt: Option[String]): SpeculationEntity = {
    val now = AppUtils.now
    val token = tokenOpt.getOrElse(AppUtils.randomAlphanumeric(8))
    SpeculationEntity(
      id = Option(UUID.randomUUID()),
      email = email,
      token = token,
      publicStateId = None,
      ts = now,
      lm = now,
      v = 0
    )
  }
}
