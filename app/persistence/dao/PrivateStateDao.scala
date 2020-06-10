package persistence.dao

import java.time.OffsetDateTime
import java.util.UUID

import commons.AppUtils
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import persistence.AppPostgresProfile.api._
import persistence.model.{PrivateStateEntity, PrivateStateStatus, PublicStateEntity, SocialOrder}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PrivateStateDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with DaoHelper {

  private class PrivateStateTable(tag: Tag) extends Table[PrivateStateEntity](tag, "private_states") {
    def id: Rep[UUID]                         = column[UUID]("id", O.PrimaryKey)
    def publicStateId: Rep[UUID]              = column[UUID]("public_state_id")
    def name: Rep[String]                     = column[String]("name")
    def logo: Rep[UUID]                       = column[UUID]("logo")
    def socialOrder: Rep[SocialOrder.Value]   = column[SocialOrder.Value]("social_order")
    def masterId: Rep[UUID]                   = column[UUID]("master_id")
    def createdBy: Rep[UUID]                  = column[UUID]("created_by")
    def status: Rep[PrivateStateStatus.Value] = column[PrivateStateStatus.Value]("status")
    def journalistId: Rep[UUID]               = column[UUID]("journalist_id")
    def characteristics: Rep[Seq[String]]     = column[Seq[String]]("characteristics")
    def ts: Rep[OffsetDateTime]               = column[OffsetDateTime]("ts")
    def lm: Rep[OffsetDateTime]               = column[OffsetDateTime]("lm")
    def v: Rep[Int]                           = column[Int]("v")

    def * =
      (
        id.?,
        publicStateId,
        name,
        logo.?,
        socialOrder,
        masterId.?,
        createdBy,
        status,
        journalistId.?,
        characteristics,
        ts,
        lm,
        v
      ) <> ((PrivateStateEntity.apply _).tupled, PrivateStateEntity.unapply)
  }

  private val PrivateStates = TableQuery[PrivateStateTable]

  def byId(id: UUID): Future[Option[PrivateStateEntity]] = {
    val action = PrivateStates.filter(_.id === id).result.headOption
    db.run(action)
  }

  def byIds(ids: Seq[UUID]): Future[Seq[PrivateStateEntity]] = {
    val action = PrivateStates.filter(_.id.inSet(ids)).result
    db.run(action)
  }

  def byPublicState(publicState: PublicStateEntity): Future[Seq[PrivateStateEntity]] = {
    val action = PrivateStates.filter(_.publicStateId === publicState.id.get).result
    db.run(action)
  }

  def insert(entity: PrivateStateEntity): Future[Either[String, UUID]] = {
    db.run(PrivateStates.returning(PrivateStates.map(_.id)) += entity)
      .map(Right(_))
      .recover {
        case psqlex: PSQLException => Left(psqlex.getServerErrorMessage.toString)
        case ex: Exception         => Left(ex.getMessage)
      }
  }

  def update(entity: PrivateStateEntity): Future[Either[String, PrivateStateEntity]] = {
    val updatedEntity = entity.copy(lm = AppUtils.now, v = entity.v + 1)
    val query         = for { e <- PrivateStates if e.id === entity.id.get } yield e
    val updateAction  = query.update(updatedEntity)
    db.run(updateAction) map { wrapUpdateInEither(entity.id, updatedEntity) }
  }
}
