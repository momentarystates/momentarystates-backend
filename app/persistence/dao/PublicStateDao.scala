package persistence.dao

import java.time.OffsetDateTime
import java.util.UUID

import commons.AppUtils
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import persistence.AppPostgresProfile.api._
import persistence.model.{PublicStateEntity, PublicStateStatus}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PublicStateDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with DaoHelper {

  private class PublicStatesTable(tag: Tag) extends Table[PublicStateEntity](tag, "public_states") {
    def id: Rep[UUID]                        = column[UUID]("id", O.PrimaryKey)
    def name: Rep[String]                    = column[String]("name")
    def logo: Rep[UUID]                      = column[UUID]("logo")
    def status: Rep[PublicStateStatus.Value] = column[PublicStateStatus.Value]("status")
    def minCitizenPerState: Rep[Int]         = column[Int]("min_citizen_per_state")
    def maxCitizenPerState: Rep[Int]         = column[Int]("max_citizen_per_state")
    def startedAt: Rep[OffsetDateTime]       = column[OffsetDateTime]("started_at")
    def marketUrl: Rep[String]               = column[String]("market_url")
    def ts: Rep[OffsetDateTime]              = column[OffsetDateTime]("ts")
    def lm: Rep[OffsetDateTime]              = column[OffsetDateTime]("lm")
    def v: Rep[Int]                          = column[Int]("v")

    def * = (id.?, name, logo.?, status, minCitizenPerState, maxCitizenPerState, startedAt.?, marketUrl.?, ts, lm, v) <> ((PublicStateEntity.apply _).tupled, PublicStateEntity.unapply)
  }
  
  private val PublicStates = TableQuery[PublicStatesTable]

  def byId(id: UUID): Future[Option[PublicStateEntity]] = {
    val action = PublicStates.filter(_.id === id).result.headOption
    db.run(action)
  }

  def byIds(ids: Seq[UUID]): Future[Seq[PublicStateEntity]] = {
    val action = PublicStates.filter(_.id.inSet(ids)).result
    db.run(action)
  }

  def insert(entity: PublicStateEntity): Future[Either[String, UUID]] = {
    db.run(PublicStates.returning(PublicStates.map(_.id)) += entity)
      .map(Right(_))
      .recover {
        case psqlex: PSQLException => Left(psqlex.getServerErrorMessage.toString)
        case ex: Exception         => Left(ex.getMessage)
      }
  }

  def update(entity: PublicStateEntity): Future[Either[String, PublicStateEntity]] = {
    val updatedEntity = entity.copy(lm = AppUtils.now, v = entity.v + 1)
    val query         = for { e <- PublicStates if e.id === entity.id.get } yield e
    val updateAction  = query.update(updatedEntity)
    db.run(updateAction) map { wrapUpdateInEither(entity.id, updatedEntity) }
  }

}