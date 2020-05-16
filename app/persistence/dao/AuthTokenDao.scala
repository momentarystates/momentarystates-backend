package persistence.dao

import java.time.OffsetDateTime
import java.util.UUID

import commons.AppUtils
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import persistence.AppPostgresProfile.api._
import persistence.model.AuthTokenEntity
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthTokenDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with DaoHelper {

  private class AuthTokenTable(tag: Tag) extends Table[AuthTokenEntity](tag, "auth_tokens") {
    def id: Rep[UUID]                = column[UUID]("id", O.PrimaryKey)
    def token: Rep[String]           = column[String]("token")
    def userId: Rep[UUID]            = column[UUID]("user_id")
    def valid: Rep[Boolean]          = column[Boolean]("valid")
    def expires: Rep[OffsetDateTime] = column[OffsetDateTime]("expires")
    def remoteAddress: Rep[String]   = column[String]("remote_address")
    def ts: Rep[OffsetDateTime]      = column[OffsetDateTime]("ts")
    def lm: Rep[OffsetDateTime]      = column[OffsetDateTime]("lm")
    def v: Rep[Int]                  = column[Int]("v")

    def * = (id.?, token, userId, valid, expires.?, remoteAddress, ts, lm, v) <> ((AuthTokenEntity.apply _).tupled, AuthTokenEntity.unapply)
  }

  private val AuthTokens = TableQuery[AuthTokenTable]

  def byId(id: UUID): Future[Option[AuthTokenEntity]] = {
    val action = AuthTokens.filter(_.id === id).result.headOption
    db.run(action)
  }

  def byIds(ids: Seq[UUID]): Future[Seq[AuthTokenEntity]] = {
    val action = AuthTokens.filter(_.id.inSet(ids)).result
    db.run(action)
  }

  def insert(user: AuthTokenEntity): Future[Either[String, UUID]] = {
    db.run(AuthTokens.returning(AuthTokens.map(_.id)) += user)
      .map(Right(_))
      .recover {
        case psqlex: PSQLException => Left(psqlex.getServerErrorMessage.toString)
        case ex: Exception         => Left(ex.getMessage)
      }
  }

  def byToken(token: String): Future[Option[AuthTokenEntity]] = {
    val action = AuthTokens.filter(_.token === token).result.headOption
    db.run(action)
  }

  def update(entity: AuthTokenEntity): Future[Either[String, AuthTokenEntity]] = {
    val updatedEntity = entity.copy(lm = AppUtils.now)
    val query         = for { authToken <- AuthTokens if authToken.id === entity.id.get } yield authToken
    val updateAction  = query.update(updatedEntity)
    db.run(updateAction) map { wrapUpdateInEither(entity.id, updatedEntity) }
  }

}
