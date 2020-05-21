package controllers

import java.util.Date

import com.google.inject.Inject
import persistence.model.{UserEntity, UserRole}
import play.api.Logger
import play.api.libs.json.{Format, Json}
import play.api.mvc._
import scalaz.{-\/, \/-}
import services.AuthService

import scala.concurrent.{ExecutionContext, Future}

class AppActions @Inject()(
    loggingActionBuilder: LoggingActionBuilder,
    userAwareActionBuilder: UserAwareActionBuilder,
    anonymousActionRefiner: AnonymousActionRefiner,
    authenticatedActionRefiner: AuthenticatedActionRefiner
) {
  def LoggingAction: ActionBuilder[Request, AnyContent] = loggingActionBuilder

  def UserAwareAction: ActionBuilder[UserAwareRequest, AnyContent] = LoggingAction andThen userAwareActionBuilder

  def AnonymousAction: ActionBuilder[Request, AnyContent] = UserAwareAction andThen anonymousActionRefiner

  def AuthenticatedAction: ActionBuilder[AuthenticatedRequest, AnyContent] = UserAwareAction andThen authenticatedActionRefiner

  def AdminAction()(implicit ec: ExecutionContext): ActionBuilder[AuthenticatedRequest, AnyContent] = {
    AuthenticatedAction andThen new ActionFilter[AuthenticatedRequest] {
      override def executionContext: ExecutionContext = ec
      override def filter[A](request: AuthenticatedRequest[A]): Future[Option[Result]] = Future {
        if (request.auth.user.role == UserRole.Admin) None else Option(Results.Forbidden)
      }
    }
  }
}

class LoggingActionBuilder @Inject()(parser: BodyParsers.Default)(implicit ec: ExecutionContext) extends ActionBuilderImpl(parser) {

  private val logger = Logger(classOf[LoggingActionBuilder])

  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
    val start = (new Date).getTime
    logger.info(s"[${request.id}] ${request.method} ${request.uri}")
    block(request) map { res =>
      val duration = (new Date).getTime - start
      logger.info(s"[${request.id}] took: " + duration + "ms")
      res
    }
  }
}

case class AuthPayload(user: UserEntity, token: String)

object AuthPayload {
  implicit val jsonFormat: Format[AuthPayload] = Json.format[AuthPayload]
}

class UserAwareRequest[A](request: Request[A], val authOpt: Option[AuthPayload]) extends WrappedRequest[A](request)

class AuthenticatedRequest[A](request: Request[A], val auth: AuthPayload) extends WrappedRequest[A](request)

class UserAwareActionBuilder @Inject()(
    val parser: BodyParsers.Default,
    val authService: AuthService
)(implicit val executionContext: ExecutionContext)
    extends ActionBuilder[UserAwareRequest, AnyContent]
    with ActionRefiner[Request, UserAwareRequest]
    with ControllerHelper {
  override def refine[A](request: Request[A]): Future[Either[Result, UserAwareRequest[A]]] = {
    request.session.get("token") match {
      case Some(token) =>
        authService.validateAuthToken(token) map {
          case -\/(error)   => Left(Results.BadRequest(Json.toJson(error)).withNewSession)
          case \/-(account) => Right(new UserAwareRequest[A](request, Option(AuthPayload(account, token))))
        }
      case _ => Future.successful(Right(new UserAwareRequest[A](request, None)))
    }
  }
}

class AnonymousActionRefiner @Inject()(val parser: BodyParsers.Default)(implicit val executionContext: ExecutionContext)
    extends ActionRefiner[UserAwareRequest, Request]
    with ControllerHelper {
  override def refine[A](request: UserAwareRequest[A]): Future[Either[Result, Request[A]]] = {
    Future {
      if (request.authOpt.isDefined) Left(ErrorResult(AppErrors.AlreadyLoggedInError))
      else Right(request)
    }
  }
}

class AuthenticatedActionRefiner @Inject()(val parser: BodyParsers.Default)(implicit val executionContext: ExecutionContext)
    extends ActionRefiner[UserAwareRequest, AuthenticatedRequest]
    with ControllerHelper {
  override def refine[A](request: UserAwareRequest[A]): Future[Either[Result, AuthenticatedRequest[A]]] = {
    Future {
      request.authOpt match {
        case Some(auth) => Right(new AuthenticatedRequest(request, auth))
        case _          => Left(Results.Unauthorized)
      }
    }
  }
}
