package legacy

import java.util.concurrent.TimeUnit

import akka.util.Timeout
import play.api.libs.json.{JsValue, Json, Reads}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.test.Helpers.await

import scala.util.Random

trait SpecHelper {

  val host: String
  val wsClient: WSClient
  val timeout: Int

  def post(relUrl: String, data: JsValue, queryParams: (String, String)*): WSResponse                 = wsMethod("post", relUrl, Option(data), None, queryParams: _*)
  def post(relUrl: String, data: JsValue, cookie: String, queryParams: (String, String)*): WSResponse = wsMethod("post", relUrl, Option(data), Option(cookie), queryParams: _*)
  def put(relUrl: String, data: JsValue): WSResponse                                                  = wsMethod("put", relUrl, Option(data), None)
  def put(relUrl: String, data: JsValue, cookie: String): WSResponse                                  = wsMethod("put", relUrl, Option(data), Option(cookie))
  def get(relUrl: String, queryParams: (String, String)*): WSResponse                                 = wsMethod("get", relUrl, None, None, queryParams: _*)
  def get(relUrl: String, cookie: String, queryParams: (String, String)*): WSResponse                 = wsMethod("get", relUrl, None, Option(cookie), queryParams: _*)
  def delete(relUrl: String, queryParams: (String, String)*): WSResponse                              = wsMethod("delete", relUrl, None, None, queryParams: _*)
  def delete(relUrl: String, cookie: String, queryParams: (String, String)*): WSResponse =
    wsMethod("delete", relUrl, None, Option(cookie), queryParams: _*)

  private def wsMethod(
      method: String,
      relativeUrlPath: String,
      data: Option[JsValue],
      cookie: Option[String],
      queryParams: (String, String)*
  ): WSResponse = {
    val url         = host + relativeUrlPath
    val anonRequest = wsClient.url(url).addQueryStringParameters(queryParams: _*)
    val request     = if (cookie.isDefined) anonRequest.addHttpHeaders("Cookie" -> cookie.get) else anonRequest
    val future = method match {
      case "post"   => request.post(data.get)
      case "put"    => request.put(data.get)
      case "delete" => request.delete()
      case "get"    => request.get()
    }
    await(future)(Timeout(timeout, TimeUnit.MILLISECONDS))
  }

  def rnd(num: Int): String = (Random.alphanumeric take num).mkString

  def as[A](response: WSResponse)(implicit reads: Reads[A]): A = Json.parse(response.body).as[A]
}
