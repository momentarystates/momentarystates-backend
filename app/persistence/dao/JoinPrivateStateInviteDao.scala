package persistence.dao

import java.time.OffsetDateTime
import java.util.UUID

import commons.AppUtils
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import persistence.AppPostgresProfile.api._
import persistence.model.{JoinPrivateStateInviteEntity, PrivateStateEntity}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JoinPrivateStateInviteDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with DaoHelper {

  private class JoinPrivateStateInviteTable(tag: Tag) extends Table[JoinPrivateStateInviteEntity](tag, "join_private_state_invites") {
    def id: Rep[UUID]               = column[UUID]("id", O.PrimaryKey)
    def privateStateId: Rep[UUID]   = column[UUID]("private_state_id")
    def token: Rep[String]          = column[String]("token")
    def email: Rep[String]          = column[String]("email")
    def usedAt: Rep[OffsetDateTime] = column[OffsetDateTime]("used_at")
    def usedBy: Rep[UUID]           = column[UUID]("used_by")
    def ts: Rep[OffsetDateTime]     = column[OffsetDateTime]("ts")
    def lm: Rep[OffsetDateTime]     = column[OffsetDateTime]("lm")
    def v: Rep[Int]                 = column[Int]("v")

    def * =
      (
        id.?,
        privateStateId,
        token,
        email,
        usedAt.?,
        usedBy.?,
        ts,
        lm,
        v
      ) <> ((JoinPrivateStateInviteEntity.apply _).tupled, JoinPrivateStateInviteEntity.unapply)
  }

  private val JoinPrivateStateInvites = TableQuery[JoinPrivateStateInviteTable]

  def byId(id: UUID): Future[Option[JoinPrivateStateInviteEntity]] = {
    val action = JoinPrivateStateInvites.filter(_.id === id).result.headOption
    db.run(action)
  }

  def byIds(ids: Seq[UUID]): Future[Seq[JoinPrivateStateInviteEntity]] = {
    val action = JoinPrivateStateInvites.filter(_.id.inSet(ids)).result
    db.run(action)
  }

  def byPrivateStates(privateStates: Seq[PrivateStateEntity]): Future[Seq[JoinPrivateStateInviteEntity]] = {
    val action = JoinPrivateStateInvites.filter(_.id.inSet(privateStates.flatMap(_.id))).result
    db.run(action)
  }

  def byToken(privateState: PrivateStateEntity, token: String): Future[Option[JoinPrivateStateInviteEntity]] = {
    val action = JoinPrivateStateInvites.filter(i => i.privateStateId === privateState.id.get && i.token === token).result.headOption
    db.run(action)
  }

  def insert(entity: JoinPrivateStateInviteEntity): Future[Either[String, UUID]] = {
    db.run(JoinPrivateStateInvites.returning(JoinPrivateStateInvites.map(_.id)) += entity)
      .map(Right(_))
      .recover {
        case psqlex: PSQLException => Left(psqlex.getServerErrorMessage.toString)
        case ex: Exception         => Left(ex.getMessage)
      }
  }

  def update(entity: JoinPrivateStateInviteEntity): Future[Either[String, JoinPrivateStateInviteEntity]] = {
    val updatedEntity = entity.copy(lm = AppUtils.now, v = entity.v + 1)
    val query         = for { e <- JoinPrivateStateInvites if e.id === entity.id.get } yield e
    val updateAction  = query.update(updatedEntity)
    db.run(updateAction) map { wrapUpdateInEither(entity.id, updatedEntity) }
  }
}
