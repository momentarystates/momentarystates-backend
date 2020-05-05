package base

import controllers.api.ApiProtocol.RegisterUser
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._

abstract class BaseSpec extends SpecWithDatabase {

  object AuthApi {
    def registerUser(username: String, password: String): Unit = {
      val request  = FakeRequest(POST, "/api/auth/register").withHeaders(HeaderNames.CONTENT_TYPE -> "application/json")
      val payload  = Json.toJson(RegisterUser(username, password))
      val response = route(app, request, payload).get
      status(response) mustBe OK
    }
  }
}
