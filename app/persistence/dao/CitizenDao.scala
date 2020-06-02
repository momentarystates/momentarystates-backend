package persistence.dao

import java.time.OffsetDateTime
import java.util.UUID

import commons.AppUtils
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import persistence.AppPostgresProfile.api._
import persistence.model.CitizenEntity
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CitizenDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with DaoHelper {

  private class CitizensTable(tag: Tag) extends Table[CitizenEntity](tag, "citizens") {
    def id: Rep[UUID]                  = column[UUID]("id", O.PrimaryKey)
    def userId: Rep[UUID]              = column[UUID]("user_id")
    def privateStateId: Rep[UUID]      = column[UUID]("private_state_id")
    def startedAt: Rep[OffsetDateTime] = column[OffsetDateTime]("started_at")
    def endedAt: Rep[OffsetDateTime]   = column[OffsetDateTime]("ended_at")
    def ts: Rep[OffsetDateTime]        = column[OffsetDateTime]("ts")
    def lm: Rep[OffsetDateTime]        = column[OffsetDateTime]("lm")
    def v: Rep[Int]                    = column[Int]("v")

    def * = (id.?, userId, privateStateId, startedAt, endedAt.?, ts, lm, v) <> ((CitizenEntity.apply _).tupled, CitizenEntity.unapply)
  }

  private val Citizens = TableQuery[CitizensTable]

  def byId(id: UUID): Future[Option[CitizenEntity]] = {
    val action = Citizens.filter(_.id === id).result.headOption
    db.run(action)
  }

  def byIds(ids: Seq[UUID]): Future[Seq[CitizenEntity]] = {
    val action = Citizens.filter(_.id.inSet(ids)).result
    db.run(action)
  }

  def insert(entity: CitizenEntity): Future[Either[String, UUID]] = {
    db.run(Citizens.returning(Citizens.map(_.id)) += entity)
      .map(Right(_))
      .recover {
        case psqlex: PSQLException => Left(psqlex.getServerErrorMessage.toString)
        case ex: Exception         => Left(ex.getMessage)
      }
  }

  def update(entity: CitizenEntity): Future[Either[String, CitizenEntity]] = {
    val updatedEntity = entity.copy(lm = AppUtils.now, v = entity.v + 1)
    val query         = for { e <- Citizens if e.id === entity.id.get } yield e
    val updateAction  = query.update(updatedEntity)
    db.run(updateAction) map { wrapUpdateInEither(entity.id, updatedEntity) }
  }
}
