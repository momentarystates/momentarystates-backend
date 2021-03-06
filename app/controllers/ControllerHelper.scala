package controllers

import commons.{AppError, AppResult}
import controllers.AppErrors.InvalidJsonPayloadError
import controllers.api.ApiProtocol.User
import play.api.Logger
import play.api.libs.json.{JsValue, Json, Reads, Writes}
import play.api.mvc.{Request, Result, Results}
import scalaz.{-\/, \/, \/-}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait ControllerHelper {

  private val logger = Logger(classOf[ControllerHelper])

  protected def validateJson[A](request: Request[JsValue])(implicit reads: Reads[A]): AppError \/ A = {
    request.body
      .validate[A]
      .fold(_ => -\/(InvalidJsonPayloadError), a => \/-(a))
  }

  def ErrorResult(err: AppError, status: Results.Status = Results.BadRequest): Result = {
    val json = Json.toJson(err)
    logger.error(" error: " + json)
    status(json)
  }

  implicit class RichBaResult[A: Writes](r: AppResult[A]) {

    private def handleError(err: AppError)(implicit request: Request[_]): Result = ErrorResult(err)

    private def runInner(f: A => Result)(implicit request: Request[_]): Future[Result] = r.run.map {
      case -\/(err)     => handleError(err)
      case \/-(payload) => f(payload)
    }

    def runResult()(implicit request: Request[_]): Future[Result] = runInner { payload =>
      Results.Ok(Json.toJson(payload))
    }

    def runResultEmptyOk()(implicit request: Request[_]): Future[Result] = runInner { _ =>
      Results.Ok
    }
  }

  implicit class RichResultWithNewSession(r: AppResult[AuthPayload]) {

    import scala.concurrent.ExecutionContext.Implicits.global

    def runResultWithNewSession()(implicit request: Request[_]): Future[Result] = {
      r.run.map {
        case -\/(err)     => ErrorResult(err)
        case \/-(payload) => Results.Ok(Json.toJson(User.fromUserEntity(payload.user))).withSession("token" -> payload.token)
      }
    }
  }
}
