package persistence.dao

import java.time.OffsetDateTime
import java.util.UUID

import commons.AppUtils
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import persistence.AppPostgresProfile.api._
import persistence.model.AppParamEntity
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AppParamDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with DaoHelper {

  private class AppParamsTable(tag: Tag) extends Table[AppParamEntity](tag, "app_params") {
    def id: Rep[UUID]           = column[UUID]("id", O.PrimaryKey)
    def key: Rep[String]        = column[String]("key")
    def value: Rep[String]      = column[String]("value")
    def ts: Rep[OffsetDateTime] = column[OffsetDateTime]("ts")
    def lm: Rep[OffsetDateTime] = column[OffsetDateTime]("lm")
    def v: Rep[Int]             = column[Int]("v")

    def * = (id.?, key, value, ts, lm, v) <> ((AppParamEntity.apply _).tupled, AppParamEntity.unapply)
  }

  private val AppParams = TableQuery[AppParamsTable]

  def byId(id: UUID): Future[Option[AppParamEntity]] = {
    val action = AppParams.filter(_.id === id).result.headOption
    db.run(action)
  }

  def byIds(ids: Seq[UUID]): Future[Seq[AppParamEntity]] = {
    val action = AppParams.filter(_.id.inSet(ids)).result
    db.run(action)
  }

  def byKey(key: String): Future[Option[AppParamEntity]] = {
    val action = AppParams.filter(_.key === key).result.headOption
    db.run(action)
  }

  def insert(entity: AppParamEntity): Future[Either[String, UUID]] = {
    db.run(AppParams.returning(AppParams.map(_.id)) += entity)
      .map(Right(_))
      .recover {
        case psqlex: PSQLException => Left(psqlex.getServerErrorMessage.toString)
        case ex: Exception         => Left(ex.getMessage)
      }
  }

  def update(entity: AppParamEntity): Future[Either[String, AppParamEntity]] = {
    val updatedEntity = entity.copy(lm = AppUtils.now, v = entity.v + 1)
    val query         = for { param <- AppParams if param.id === entity.id.get } yield param
    val updateAction  = query.update(updatedEntity)
    db.run(updateAction) map { wrapUpdateInEither(entity.id, updatedEntity) }
  }
}
