package commons

import play.api.libs.json.{Format, Json}

case class BaError(error: String)

object  BaError {
  implicit val jsonFormat: Format[BaError] = Json.format[BaError]
}
