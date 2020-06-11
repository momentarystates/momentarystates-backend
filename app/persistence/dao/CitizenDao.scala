package persistence.dao

import java.time.OffsetDateTime
import java.util.UUID

import commons.AppUtils
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import persistence.AppPostgresProfile.api._
import persistence.model.{CitizenEntity, CitizenshipEndReason, PrivateStateEntity, UserEntity}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CitizenDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with DaoHelper {

  private class CitizensTable(tag: Tag) extends Table[CitizenEntity](tag, "citizens") {
    def id: Rep[UUID]                              = column[UUID]("id", O.PrimaryKey)
    def userId: Rep[UUID]                          = column[UUID]("user_id")
    def privateStateId: Rep[UUID]                  = column[UUID]("private_state_id")
    def name: Rep[String]                          = column[String]("name")
    def startedAt: Rep[OffsetDateTime]             = column[OffsetDateTime]("started_at")
    def endedAt: Rep[Option[OffsetDateTime]]       = column[Option[OffsetDateTime]]("ended_at")
    def endReason: Rep[CitizenshipEndReason.Value] = column[CitizenshipEndReason.Value]("end_reason")
    def ts: Rep[OffsetDateTime]                    = column[OffsetDateTime]("ts")
    def lm: Rep[OffsetDateTime]                    = column[OffsetDateTime]("lm")
    def v: Rep[Int]                                = column[Int]("v")

    def * =
      (
        id.?,
        userId,
        privateStateId,
        name,
        startedAt,
        endedAt,
        endReason.?,
        ts,
        lm,
        v
      ) <> ((CitizenEntity.apply _).tupled, CitizenEntity.unapply)
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

  def byUserAndPrivateState(user: UserEntity, privateState: PrivateStateEntity): Future[Option[CitizenEntity]] = {
    val action = Citizens.filter(c => c.userId === user.id.get && c.privateStateId === privateState.id.get).result.headOption
    db.run(action)
  }

  def byPrivateStates(privateStates: Seq[PrivateStateEntity]): Future[Seq[CitizenEntity]] = {
    val action = Citizens.filter(_.privateStateId inSet privateStates.flatMap(_.id)).result
    db.run(action)
  }

  def activeByPrivateStates(privatesStates: Seq[PrivateStateEntity]): Future[Seq[CitizenEntity]] = {
    val action = Citizens.filter(c => c.privateStateId.inSet(privatesStates.flatMap(_.id)) && c.endedAt.isEmpty).result
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
