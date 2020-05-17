package persistence.model

import java.time.OffsetDateTime
import java.util.UUID

import commons.AppUtils
import play.api.libs.json.{Format, Json, Reads, Writes}

object EmailStatus extends Enumeration {
  val Error, New, Processing, Sent = Value

  implicit val jsonReads: Reads[EmailStatus.Value]   = Reads.enumNameReads(EmailStatus)
  implicit val jsonWrites: Writes[EmailStatus.Value] = Writes.enumNameWrites
}

case class EmailEntity(
    id: Option[UUID],
    subject: String,
    recipients: List[String],
    body: String,
    status: EmailStatus.Value,
    messageId: Option[String],
    retries: Int,
    ts: OffsetDateTime,
    lm: OffsetDateTime,
    v: Int
)

object EmailEntity {
  implicit val jsonFormat: Format[EmailEntity] = Json.format[EmailEntity]

  def generate(subject: String, recipients: List[String], body: String): EmailEntity = {
    val now = AppUtils.now
    EmailEntity(
      id = Option(UUID.randomUUID()),
      subject = subject,
      recipients = recipients,
      body = body,
      status = EmailStatus.New,
      messageId = None,
      retries = 0,
      ts = now,
      lm = now,
      v = 0
    )
  }
}
