package controllers.api

import base.BaseSpec
import play.api.test.FakeRequest
import play.api.test.Helpers._

final class PlaygroundControllerSpec extends BaseSpec {

  "PlaygroundController" should {
    "succeed" in withDatabase {
      val request  = FakeRequest(GET, "/api/playground/1")
      val response = route(app, request).get
      status(response) mustBe OK
    }
  }
}