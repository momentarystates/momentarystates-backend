package persistence.dao

import java.time.OffsetDateTime
import java.util.UUID

import commons.AppUtils
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import persistence.AppPostgresProfile.api._
import persistence.model.{UserEntity, UserRole}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
final class UserDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with DaoHelper {

  private class UsersTable(tag: Tag) extends Table[UserEntity](tag, "users") {
    def id: Rep[UUID]                         = column[UUID]("id", O.PrimaryKey)
    def username: Rep[String]                 = column[String]("username")
    def passwordHash: Rep[String]             = column[String]("password_hash")
    def passwordSalt: Rep[String]             = column[String]("password_salt")
    def role: Rep[UserRole.Value]             = column[UserRole.Value]("role")
    def email: Rep[String]                    = column[String]("email")
    def emailConfirmedAt: Rep[OffsetDateTime] = column[OffsetDateTime]("email_confirmed_at")
    def confirmationCode: Rep[String]         = column[String]("confirmation_code")
    def avatar: Rep[UUID]                     = column[UUID]("avatar")
    def ts: Rep[OffsetDateTime]               = column[OffsetDateTime]("ts")
    def lm: Rep[OffsetDateTime]               = column[OffsetDateTime]("lm")
    def v: Rep[Int]                           = column[Int]("v")

    def * = (id.?, username, passwordHash, passwordSalt, role, email, emailConfirmedAt.?, confirmationCode, avatar.?, ts, lm, v) <> ((UserEntity.apply _).tupled, UserEntity.unapply)
  }

  private val Users = TableQuery[UsersTable]

  def byId(id: UUID): Future[Option[UserEntity]] = {
    val action = Users.filter(_.id === id).result.headOption
    db.run(action)
  }

  def byIds(ids: Seq[UUID]): Future[Seq[UserEntity]] = {
    val action = Users.filter(_.id.inSet(ids)).result
    db.run(action)
  }

  def insert(entity: UserEntity): Future[Either[String, UUID]] = {
    db.run(Users.returning(Users.map(_.id)) += entity)
      .map(Right(_))
      .recover {
        case psqlex: PSQLException => Left(psqlex.getServerErrorMessage.toString)
        case ex: Exception         => Left(ex.getMessage)
      }
  }

  def byUsername(username: String): Future[Option[UserEntity]] = {
    val action = Users.filter(_.username === username).result.headOption
    db.run(action)
  }

  def byEmail(email: String): Future[Option[UserEntity]] = {
    val action = Users.filter(_.email === email).result.headOption
    db.run(action)
  }

  def update(entity: UserEntity): Future[Either[String, UserEntity]] = {
    val updatedEntity = entity.copy(lm = AppUtils.now, v = entity.v + 1)
    val query         = for { user <- Users if user.id === entity.id.get } yield user
    val updateAction  = query.update(updatedEntity)
    db.run(updateAction) map { wrapUpdateInEither(entity.id, updatedEntity) }
  }

}
