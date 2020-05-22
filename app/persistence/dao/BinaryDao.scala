package persistence.dao

import java.time.OffsetDateTime
import java.util.UUID

import commons.AppUtils
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import persistence.AppPostgresProfile.api._
import persistence.model.BinaryEntity
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BinaryDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with DaoHelper {

  private class BinariesTable(tag: Tag) extends Table[BinaryEntity](tag, "binaries") {
    def id: Rep[UUID]            = column[UUID]("id", O.PrimaryKey)
    def fileName: Rep[String]    = column[String]("file_name")
    def path: Rep[String]        = column[String]("path")
    def contentType: Rep[String] = column[String]("content_type")
    def length: Rep[Long]        = column[Long]("length")
    def md5: Rep[String]         = column[String]("md5")
    def ts: Rep[OffsetDateTime]  = column[OffsetDateTime]("ts")
    def lm: Rep[OffsetDateTime]  = column[OffsetDateTime]("lm")
    def v: Rep[Int]              = column[Int]("v")

    def * = (id.?, fileName, path, contentType, length, md5, ts, lm, v) <> ((BinaryEntity.apply _).tupled, BinaryEntity.unapply)
  }

  private val Binaries = TableQuery[BinariesTable]

  def byId(id: UUID): Future[Option[BinaryEntity]] = {
    val action = Binaries.filter(_.id === id).result.headOption
    db.run(action)
  }

  def byIds(ids: Seq[UUID]): Future[Seq[BinaryEntity]] = {
    val action = Binaries.filter(_.id.inSet(ids)).result
    db.run(action)
  }

  def insert(binary: BinaryEntity): Future[Either[String, UUID]] = {
    db.run(Binaries.returning(Binaries.map(_.id)) += binary)
      .map(Right(_))
      .recover {
        case psqlex: PSQLException => Left(psqlex.getServerErrorMessage.toString)
        case ex: Exception         => Left(ex.getMessage)
      }
  }

  def delete(id: UUID): Future[Option[String]] = {
    val action = Binaries.filter(_.id === id).delete
    db.run(action) map {
      case 1 => None
      case _ => Option("error deleting binary")
    }
  }

  def update(entity: BinaryEntity): Future[Either[String, BinaryEntity]] = {
    val updatedEntity = entity.copy(lm = AppUtils.now, v = entity.v + 1)
    val query         = for { binary <- Binaries if binary.id === entity.id.get } yield binary
    val updateAction  = query.update(updatedEntity)
    db.run(updateAction) map { wrapUpdateInEither(entity.id, updatedEntity) }
  }

}
