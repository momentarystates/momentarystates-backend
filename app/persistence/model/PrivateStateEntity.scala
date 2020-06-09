package persistence.model

import java.time.OffsetDateTime
import java.util.UUID

import commons.AppUtils
import play.api.libs.json.{Format, Json, Reads, Writes}

object PrivateStateStatus extends Enumeration {
  val Founding, Founded = Value

  implicit val jsonReads: Reads[PrivateStateStatus.Value]   = Reads.enumNameReads(PrivateStateStatus)
  implicit val jsonWrites: Writes[PrivateStateStatus.Value] = Writes.enumNameWrites
}

case class PrivateStateEntity(
    id: Option[UUID],
    publicStateId: UUID,
    name: String,
    logo: Option[UUID],
    socialOrder: SocialOrder.Value,
    masterId: Option[UUID],
    createdBy: UUID,
    status: PrivateStateStatus.Value,
    journalistId: Option[UUID],
    characteristics: Seq[String],
    ts: OffsetDateTime,
    lm: OffsetDateTime,
    v: Int
)

object PrivateStateEntity {
  implicit val jsonFormat: Format[PrivateStateEntity] = Json.format[PrivateStateEntity]

  def generate(publicState: PublicStateEntity, name: String, socialOrder: SocialOrder.Value, createdBy: UserEntity, characteristics: Seq[String]): PrivateStateEntity = {
    val now = AppUtils.now
    PrivateStateEntity(
      id = Option(UUID.randomUUID()),
      publicStateId = publicState.id.get,
      name = name,
      logo = None,
      socialOrder = socialOrder,
      masterId = None,
      createdBy = createdBy.id.get,
      status = PrivateStateStatus.Founding,
      journalistId = None,
      characteristics = characteristics,
      ts = now,
      lm = now,
      v = 0
    )
  }
}
