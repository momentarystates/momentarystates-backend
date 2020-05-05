package controllers.api.auth

import commons.{AuthUtils, BaResult}
import controllers.api.ApiProtocol.LoginUser
import controllers.{AppActions, AppErrors, AuthPayload, ControllerHelper}
import javax.inject.{Inject, Singleton}
import persistence.dao.UserDao
import persistence.model.{AuthTokenEntity, UserEntity}
import play.api.Configuration
import play.api.mvc.{AbstractController, ControllerComponents, EssentialAction}
import scalaz.Scalaz._
import services.AuthService

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class LoginUserController @Inject()(
    cc: ControllerComponents,
    appActions: AppActions,
    userDao: UserDao,
    authService: AuthService,
    config: Configuration
) extends AbstractController(cc)
    with ControllerHelper {

  def login(): EssentialAction = appActions.AnonymousAction.async(parse.json) { implicit request =>
    println("-------- foobar ----------")
    val res = for {
      in    <- BaResult[LoginUser](validateJson[LoginUser](request))
      user  <- BaResult.fromFutureOption[UserEntity](userDao.byUsername(in.username))(AppErrors.EntityNotFoundError("user"))
      _     <- BaResult(AuthUtils.checkHash(in.password, user.passwordSalt, user.passwordHash))(AppErrors.AuthenticationFailed)
      token <- BaResult[AuthTokenEntity](authService.createAuthToken(user, request.remoteAddress))
    } yield AuthPayload(user, token.token)

    res.runResultWithNewSession
  }
}
