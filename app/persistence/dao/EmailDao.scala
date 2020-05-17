package persistence.dao

import java.time.OffsetDateTime
import java.util.UUID

import commons.AppUtils
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import persistence.AppPostgresProfile.api._
import persistence.model.{EmailEntity, EmailStatus}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with DaoHelper {

  private class EmailsTable(tag: Tag) extends Table[EmailEntity](tag, "emails") {
    def id: Rep[UUID]                  = column[UUID]("id", O.PrimaryKey)
    def subject: Rep[String]           = column[String]("subject")
    def recipients: Rep[List[String]]  = column[List[String]]("recipients")
    def body: Rep[String]              = column[String]("body")
    def status: Rep[EmailStatus.Value] = column[EmailStatus.Value]("status")
    def messageId: Rep[String]         = column[String]("message_id")
    def retries: Rep[Int]              = column[Int]("retries")
    def ts: Rep[OffsetDateTime]        = column[OffsetDateTime]("ts")
    def lm: Rep[OffsetDateTime]        = column[OffsetDateTime]("lm")
    def v: Rep[Int]                    = column[Int]("v")

    def * = (id.?, subject, recipients, body, status, messageId.?, retries, ts, lm, v) <> ((EmailEntity.apply _).tupled, EmailEntity.unapply)
  }

  private val Emails = TableQuery[EmailsTable]

  def byId(id: UUID): Future[Option[EmailEntity]] = {
    val action = Emails.filter(_.id === id).result.headOption
    db.run(action)
  }

  def byIds(ids: Seq[UUID]): Future[Seq[EmailEntity]] = {
    val action = Emails.filter(_.id.inSet(ids)).result
    db.run(action)
  }

  def insert(entity: EmailEntity): Future[Either[String, UUID]] = {
    db.run(Emails.returning(Emails.map(_.id)) += entity)
      .map(Right(_))
      .recover {
        case psqlex: PSQLException => Left(psqlex.getServerErrorMessage.toString)
        case ex: Exception         => Left(ex.getMessage)
      }
  }

  def update(entity: EmailEntity): Future[Either[String, EmailEntity]] = {
    val updatedEntity = entity.copy(lm = AppUtils.now, v = entity.v + 1)
    val query         = for { email <- Emails if email.id === entity.id.get } yield email
    val updateAction  = query.update(updatedEntity)
    db.run(updateAction) map { wrapUpdateInEither(entity.id, updatedEntity) }
  }
}
