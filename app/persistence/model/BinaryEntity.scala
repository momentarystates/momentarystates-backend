package persistence.model

import java.time.OffsetDateTime
import java.util.UUID

import commons.AppUtils
import play.api.libs.json.{Format, Json}

case class BinaryEntity(
    id: Option[UUID],
    fileName: String,
    path: String,
    contentType: String,
    length: Long,
    md5: String,
    ts: OffsetDateTime,
    lm: OffsetDateTime,
    v: Int
)

object BinaryEntity {
  implicit val jsonFormat: Format[BinaryEntity] = Json.format[BinaryEntity]

  def generate(fileName: String, contentType: String, length: Long, md5: String): BinaryEntity = {
    val now = AppUtils.now
    val id = UUID.randomUUID()
    val path = s"$id/$fileName"
    BinaryEntity(
      id = Option(UUID.randomUUID()),
      fileName = fileName,
      path = path,
      contentType = contentType,
      length = length,
      md5 = md5,
      ts = now,
      lm = now,
      v = 0
    )
  }
}
