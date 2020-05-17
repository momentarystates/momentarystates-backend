package persistence.model

import java.time.OffsetDateTime
import java.util.UUID

import commons.AppUtils
import play.api.libs.json.{Format, Json}

case class AppParamEntity(
    id: Option[UUID],
    key: String,
    value: String,
    ts: OffsetDateTime,
    lm: OffsetDateTime,
    v: Int
)

object AppParamEntity {

  val KEY_EMAIL_WORKER_BUSY = "emailWorkerBusy"

  implicit val jsonFormat: Format[AppParamEntity] = Json.format[AppParamEntity]

  def generate(key: String, value: String): AppParamEntity = {
    val now = AppUtils.now
    AppParamEntity(
      id = Option(UUID.randomUUID),
      key = key,
      value = value,
      ts = now,
      lm = now,
      v = 0
    )
  }
}
