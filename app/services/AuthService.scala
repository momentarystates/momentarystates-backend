package services

import commons.{AppUtils, AppError, AppResult}
import controllers.AppErrors
import javax.inject.{Inject, Singleton}
import persistence.dao.{AuthTokenDao, UserDao}
import persistence.model.{AuthTokenEntity, UserEntity}
import play.api.Configuration
import scalaz.Scalaz._
import scalaz.{-\/, \/, \/-}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.Duration

@Singleton
class AuthService @Inject()(
    authTokenDao: AuthTokenDao,
    userDao: UserDao,
    configuration: Configuration
) {

  private val authTokenExpiresAfter = configuration.get[String]("app.auth.tokenExpiresAfter")

  def validateAuthToken(token: String): Future[AppError \/ UserEntity] = {

    val res = for {
      authToken <- AppResult.fromFutureOption[AuthTokenEntity](authTokenDao.byToken(token))(AppErrors.EntityNotFoundError("auth_token"))
      user      <- AppResult.fromFutureOption[UserEntity](userDao.byId(authToken.userId))(AppErrors.EntityNotFoundError("user"))
    } yield user

    res.run
  }

  def createAuthToken(user: UserEntity, remoteAddress: String) = {

    val expires = authTokenExpiresAfter match {
      case "never" => None
      case _       => Option(AppUtils.now.plusSeconds(Duration(authTokenExpiresAfter).toSeconds))
    }
    val token = AuthTokenEntity.generate(user, remoteAddress, expires)
    authTokenDao.insert(token) map {
      case Left(error) => -\/(AppErrors.DatabaseError(error))
      case Right(uuid) => \/-(token.copy(id = Option(uuid)))
    }
  }

  def invalidateAuthToken(token: String): Future[Option[String]] = {
    authTokenDao.byToken(token) flatMap {
      case Some(authToken) =>
        val now = AppUtils.now
        val updatedAuthToken = authToken.copy(
          valid = false,
          lm = now,
          v = authToken.v + 1
        )
        authTokenDao.update(updatedAuthToken) map {
          case Right(_)    => None
          case Left(error) => Option(error)
        }
      case _ => Future(None)
    }
  }
}
