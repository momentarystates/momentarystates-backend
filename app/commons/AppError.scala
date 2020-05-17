package commons

import play.api.libs.json.{Format, Json}

case class AppError(code: String, error: String) {
  def error(error: String): AppError = AppError(code, error)
}

object  AppError {
  implicit val jsonFormat: Format[AppError] = Json.format[AppError]
}
