package commons

import play.api.libs.json.{Format, Json}

case class BaError(code: String, error: String) {
  def error(error: String): BaError = BaError(code, error)
}

object  BaError {
  implicit val jsonFormat: Format[BaError] = Json.format[BaError]
}
