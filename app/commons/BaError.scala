package commons

import play.api.libs.json.{Format, Json}

case class BaError(error: String) {
  def error(error: String): BaError = BaError(error)
}

object  BaError {
  implicit val jsonFormat: Format[BaError] = Json.format[BaError]
}
