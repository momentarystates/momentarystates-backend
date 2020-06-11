package persistence.dao

import java.time.OffsetDateTime
import java.util.UUID

import commons.AppUtils
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import persistence.AppPostgresProfile.api._
import persistence.model.{CreatePrivateStateInviteEntity, PublicStateEntity}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreatePrivateStateInviteDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with DaoHelper {

  private class CreatePrivateStateInviteTable(tag: Tag) extends Table[CreatePrivateStateInviteEntity](tag, "create_private_state_invites") {
    def id: Rep[UUID]               = column[UUID]("id", O.PrimaryKey)
    def publicStateId: Rep[UUID]    = column[UUID]("public_state_id")
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
        publicStateId,
        token,
        email,
        usedAt.?,
        usedBy.?,
        ts,
        lm,
        v
      ) <> ((CreatePrivateStateInviteEntity.apply _).tupled, CreatePrivateStateInviteEntity.unapply)
  }

  private val CreatePrivateStateInvites = TableQuery[CreatePrivateStateInviteTable]

  def byId(id: UUID): Future[Option[CreatePrivateStateInviteEntity]] = {
    val action = CreatePrivateStateInvites.filter(_.id === id).result.headOption
    db.run(action)
  }

  def byIds(ids: Seq[UUID]): Future[Seq[CreatePrivateStateInviteEntity]] = {
    val action = CreatePrivateStateInvites.filter(_.id.inSet(ids)).result
    db.run(action)
  }

  def byPublicState(publicState: PublicStateEntity, email: Option[String] = None): Future[Seq[CreatePrivateStateInviteEntity]] = {
    val action = email match {
      case Some(query) => CreatePrivateStateInvites.filter(i => (i.publicStateId === publicState.id.get) && (i.email like s"%$query%")).result
      case _ => CreatePrivateStateInvites.filter(_.publicStateId === publicState.id.get).result
    }
    db.run(action)
  }

  def insert(entity: CreatePrivateStateInviteEntity): Future[Either[String, UUID]] = {
    db.run(CreatePrivateStateInvites.returning(CreatePrivateStateInvites.map(_.id)) += entity)
      .map(Right(_))
      .recover {
        case psqlex: PSQLException => Left(psqlex.getServerErrorMessage.toString)
        case ex: Exception         => Left(ex.getMessage)
      }
  }

  def update(entity: CreatePrivateStateInviteEntity): Future[Either[String, CreatePrivateStateInviteEntity]] = {
    val updatedEntity = entity.copy(lm = AppUtils.now, v = entity.v + 1)
    val query         = for { e <- CreatePrivateStateInvites if e.id === entity.id.get } yield e
    val updateAction  = query.update(updatedEntity)
    db.run(updateAction) map { wrapUpdateInEither(entity.id, updatedEntity) }
  }

}
