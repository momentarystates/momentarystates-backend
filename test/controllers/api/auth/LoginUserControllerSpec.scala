package controllers.api.auth

import base.BaseSpec
import controllers.api.ApiProtocol.{LoginUser, User}
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._

final class LoginUserControllerSpec extends BaseSpec {

  "LoginController" should {
    "succeed with correct credentials" in withDatabase {

      // prepare data
      val username = s"testuser-${rnd(10)}"
      val password = rnd(50)

      // register a new user
      AuthApi.registerUser(username, password)

      // test
      val request  = FakeRequest(POST, "/api/auth/login").withHeaders(HeaderNames.CONTENT_TYPE -> "application/json")
      val payload  = Json.toJson(LoginUser(username, password))
      val response = route(app, request, payload).get
      status(response) mustBe OK
      val user = contentAsJson(response).as[User]
      user.username mustBe username

      println("--------")
      val c = cookies(response)
      println("cookies: " + c)
      val h = headers(response)
      println("headers: " + h)
      println("--------")

      val foo = await(response)
      println("--------")
    }
  }
}
