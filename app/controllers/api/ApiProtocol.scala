package controllers.api

import play.api.libs.json.{Format, Json}

object ApiProtocol {

  case class RegisterUser(
      username: String,
      password: String
  )

  object RegisterUser {
    implicit val jsonFormat: Format[RegisterUser] = Json.format[RegisterUser]
  }

}
