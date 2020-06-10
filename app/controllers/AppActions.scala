package controllers

import java.util.{Date, UUID}

import com.google.inject.Inject
import commons._
import persistence.dao.{CitizenDao, PrivateStateDao, PublicStateDao}
import persistence.model._
import play.api.Logger
import play.api.libs.json.{Format, Json}
import play.api.mvc._
import scalaz.Scalaz._
import scalaz.{-\/, \/-}
import services.AuthService

import scala.concurrent.{ExecutionContext, Future}

class AppActions @Inject()(
    loggingActionBuilder: LoggingActionBuilder,
    userAwareActionBuilder: UserAwareActionBuilder,
    anonymousActionRefiner: AnonymousActionRefiner,
    authenticatedActionRefiner: AuthenticatedActionRefiner,
    publicStateDao: PublicStateDao,
    privateStateDao: PrivateStateDao,
    citizenDao: CitizenDao
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

  def GoddessAction(id: UUID)(implicit ec: ExecutionContext): ActionBuilder[PublicStateRequest, AnyContent] = {
    val refiner = new ActionRefiner[AuthenticatedRequest, PublicStateRequest] {
      override def executionContext: ExecutionContext = ec

      override def refine[A](request: AuthenticatedRequest[A]): Future[Either[Result, PublicStateRequest[A]]] = {
        publicStateDao.byId(id) map {
          case Some(publicState) =>
            if (publicState.goddessId == request.auth.user.id.get) Right(new PublicStateRequest[A](request, request.auth, publicState))
            else Left(Results.BadRequest(Json.toJson(AppErrors.InvalidGoddessError)))
          case _ => Left(Results.BadRequest(Json.toJson(AppErrors.EntityNotFoundError("publicState"))))
        }
      }
    }
    AuthenticatedAction andThen refiner
  }

  def RunningPublicStateAction(id: UUID)(implicit ec: ExecutionContext): ActionBuilder[PublicStateRequest, AnyContent] = {
    GoddessAction(id) andThen new ActionFilter[PublicStateRequest] {
      override def executionContext: ExecutionContext = ec

      override def filter[A](request: PublicStateRequest[A]): Future[Option[Result]] = Future {
        if (request.publicState.status == PublicStateStatus.Running) None else Option(Results.BadRequest(Json.toJson(AppErrors.InvalidPublicStateStatusError)))
      }
    }
  }

  def PrivateStateAction(id: UUID)(implicit ec: ExecutionContext): ActionBuilder[PrivateStateRequest, AnyContent] = {
    val refiner = new ActionRefiner[AuthenticatedRequest, PrivateStateRequest] {

      override def executionContext: ExecutionContext = ec

      override def refine[A](request: AuthenticatedRequest[A]): Future[Either[Result, PrivateStateRequest[A]]] = {
        val res = for {
          privateState <- privateStateDao.byId(id).handleEntityNotFound("privateState")
          publicState  <- publicStateDao.byId(privateState.publicStateId).handleEntityNotFound("publicState")
          _            <- AppResult(publicState.status == PublicStateStatus.Running)(AppErrors.InvalidPublicStateStatusError)
        } yield (privateState, publicState)
        res.run map {
          case -\/(error) => Left(Results.BadRequest(Json.toJson(error)))
          case \/-(data)  => Right(new PrivateStateRequest[A](request, request.auth, data._2, data._1))
        }
      }
    }

    AuthenticatedAction andThen refiner
  }

  def CitizenAction(id: UUID)(implicit ec: ExecutionContext): ActionBuilder[CitizenRequest, AnyContent] = {
    val refiner = new ActionRefiner[PrivateStateRequest, CitizenRequest] {
      override def executionContext: ExecutionContext = ec

      override def refine[A](request: PrivateStateRequest[A]): Future[Either[Result, CitizenRequest[A]]] = {
        citizenDao.byUserAndPrivateState(request.auth.user, request.privateState) map {
          case Some(citizen) => Right(new CitizenRequest[A](request, request.auth, request.publicState, request.privateState, citizen))
          case _             => Left(Results.BadRequest(Json.toJson(AppErrors.NotCitizenOfPrivateStateError)))
        }
      }
    }

    PrivateStateAction(id) andThen refiner
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

class PublicStateRequest[A](request: Request[A], val auth: AuthPayload, val publicState: PublicStateEntity) extends WrappedRequest[A](request)

class PrivateStateRequest[A](request: Request[A], val auth: AuthPayload, val publicState: PublicStateEntity, val privateState: PrivateStateEntity) extends WrappedRequest[A](request)

class CitizenRequest[A](request: Request[A], val auth: AuthPayload, val publicState: PublicStateEntity, val privateState: PrivateStateEntity, val citizen: CitizenEntity)
    extends WrappedRequest[A](request)

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

class AnonymousActionRefiner @Inject()(val parser: BodyParsers.Default)(implicit val executionContext: ExecutionContext) extends ActionRefiner[UserAwareRequest, Request] with ControllerHelper {
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
