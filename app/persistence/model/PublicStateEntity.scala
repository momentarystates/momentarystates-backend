package persistence.model

import java.time.OffsetDateTime
import java.util.UUID

import commons.AppUtils
import play.api.libs.json.{Format, Json}

case class PublicStateEntity(
    id: Option[UUID],
    name: String,
    logo: Option[UUID],
    status: PublicStateStatus.Value,
    minCitizenPerState: Int,
    maxCitizenPerState: Int,
    startedAt: Option[OffsetDateTime],
    marketUrl: Option[String],
    isProcessing: Boolean,
    ts: OffsetDateTime,
    lm: OffsetDateTime,
    v: Int
)

object PublicStateEntity {
  implicit val jsonFormat: Format[PublicStateEntity] = Json.format[PublicStateEntity]

  def generate(name: String, minCitizenPerState: Int = -1, maxCitizenPerState: Int = -1): PublicStateEntity = {
    val now = AppUtils.now
    PublicStateEntity(
      id = Option(UUID.randomUUID),
      name = name,
      logo = None,
      status = PublicStateStatus.Created,
      minCitizenPerState = minCitizenPerState,
      maxCitizenPerState = maxCitizenPerState,
      startedAt = None,
      marketUrl = None,
      isProcessing = false,
      ts = now,
      lm = now,
      v = 0
    )
  }
}
