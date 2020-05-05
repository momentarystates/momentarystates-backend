package controllers.api.auth

import base.BaseSpec
import controllers.api.ApiProtocol.RegisterUser
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._

class RegisterUserControllerSpec extends BaseSpec {

  "RegisterController" should {
    "succeed" in withDatabase {
      val username = s"testuser-${rnd(10)}"
      val password = rnd(50)
      val request = FakeRequest(POST, "/api/auth/register").withHeaders(HeaderNames.CONTENT_TYPE -> "application/json")
      val payload = Json.toJson(RegisterUser(username, password))
      val response = route(app, request, payload).get
      status(response) mustBe OK
    }

    "fail with existing username" in withDatabase {
      val username = s"testuser-${rnd(10)}"
      val password = rnd(50)

      {
        val request = FakeRequest(POST, "/api/auth/register").withHeaders(HeaderNames.CONTENT_TYPE -> "application/json")
        val payload = Json.toJson(RegisterUser(username, password))
        val response = route(app, request, payload).get
        status(response) mustBe OK
      }

      {
        val request = FakeRequest(POST, "/api/auth/register").withHeaders(HeaderNames.CONTENT_TYPE -> "application/json")
        val payload = Json.toJson(RegisterUser(username, password))
        val response = route(app, request, payload).get
        status(response) mustBe BAD_REQUEST
      }
    }
  }
}
