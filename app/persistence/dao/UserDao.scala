package persistence.dao

import java.time.OffsetDateTime
import java.util.UUID

import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import persistence.model.{UserEntity, UserRole}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import persistence.AppPostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
final class UserDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile] {

  private class UsersTable(tag: Tag) extends Table[UserEntity](tag, "users") {
    def id: Rep[UUID]             = column[UUID]("id", O.PrimaryKey)
    def username: Rep[String]     = column[String]("username")
    def passwordHash: Rep[String] = column[String]("password_hash")
    def passwordSalt: Rep[String] = column[String]("password_salt")
    def role: Rep[UserRole.Value] = column[UserRole.Value]("role")
    def ts: Rep[OffsetDateTime]   = column[OffsetDateTime]("ts")
    def lm: Rep[OffsetDateTime]   = column[OffsetDateTime]("lm")
    def v: Rep[Int]               = column[Int]("v")

    def * = (id.?, username, passwordHash, passwordSalt, role, ts, lm, v) <> ((UserEntity.apply _).tupled, UserEntity.unapply)
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

  def insert(user: UserEntity): Future[Either[String, UUID]] = {
    db.run(Users.returning(Users.map(_.id)) += user)
      .map(Right(_))
      .recover {
        case psqlex: PSQLException =>
          Left(psqlex.getServerErrorMessage.toString)
        case ex: Exception =>
          ex.printStackTrace()
          Left(ex.getMessage)
      }
  }
}
