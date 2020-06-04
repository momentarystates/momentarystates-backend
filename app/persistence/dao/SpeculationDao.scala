package persistence.dao

import java.time.OffsetDateTime
import java.util.UUID

import commons.AppUtils
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import persistence.AppPostgresProfile.api._
import persistence.model.SpeculationEntity
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SpeculationDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with DaoHelper {

  private class SpeculationsTable(tag: Tag) extends Table[SpeculationEntity](tag, "speculations") {
    def id: Rep[UUID]            = column[UUID]("id", O.PrimaryKey)
    def email: Rep[String]       = column[String]("email")
    def token: Rep[String]       = column[String]("token")
    def publicStateId: Rep[UUID] = column[UUID]("public_state_id")
    def ts: Rep[OffsetDateTime]  = column[OffsetDateTime]("ts")
    def lm: Rep[OffsetDateTime]  = column[OffsetDateTime]("lm")
    def v: Rep[Int]              = column[Int]("v")

    def * = (id.?, email, token, publicStateId.?, ts, lm, v) <> ((SpeculationEntity.apply _).tupled, SpeculationEntity.unapply)
  }

  private val Speculations = TableQuery[SpeculationsTable]

  def byId(id: UUID): Future[Option[SpeculationEntity]] = {
    val action = Speculations.filter(_.id === id).result.headOption
    db.run(action)
  }

  def byIds(ids: Seq[UUID]): Future[Seq[SpeculationEntity]] = {
    val action = Speculations.filter(_.id.inSet(ids)).result
    db.run(action)
  }

  def insert(entity: SpeculationEntity): Future[Either[String, UUID]] = {
    db.run(Speculations.returning(Speculations.map(_.id)) += entity)
      .map(Right(_))
      .recover {
        case psqlex: PSQLException => Left(psqlex.getServerErrorMessage.toString)
        case ex: Exception         => Left(ex.getMessage)
      }
  }

  def update(entity: SpeculationEntity): Future[Either[String, SpeculationEntity]] = {
    val updatedEntity = entity.copy(lm = AppUtils.now, v = entity.v + 1)
    val query         = for { user <- Speculations if user.id === entity.id.get } yield user
    val updateAction  = query.update(updatedEntity)
    db.run(updateAction) map { wrapUpdateInEither(entity.id, updatedEntity) }
  }

}
