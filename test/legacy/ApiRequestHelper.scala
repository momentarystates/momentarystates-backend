package legacy

import controllers.api.ApiProtocol.{RegisterUser, User}
import play.api.libs.json.Json

trait ApiRequestHelper extends SpecHelper {

  object authApi {
    def registerUser(username: String, password: String): User = {
      val requestData = RegisterUser(username, password)
      val response    = post("/api/auth/register", Json.toJson(requestData))
      as[User](response)
    }


  }
}
