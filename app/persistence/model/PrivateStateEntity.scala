package persistence.model

import java.time.OffsetDateTime
import java.util.UUID

import commons.AppUtils
import play.api.libs.json.{Format, Json}

case class PrivateStateEntity(
    id: Option[UUID],
    publicStateId: UUID,
    name: String,
    logo: Option[UUID],
    socialOrder: SocialOrder.Value,
    masterId: Option[UUID],
    createdBy: UUID,
    journalistId: Option[UUID],
    ts: OffsetDateTime,
    lm: OffsetDateTime,
    v: Int
)

object PrivateStateEntity {
  implicit val jsonFormat: Format[PrivateStateEntity] = Json.format[PrivateStateEntity]

  def generate(publicState: PublicStateEntity, name: String, socialOrder: SocialOrder.Value, createdBy: UserEntity): PrivateStateEntity = {
    val now = AppUtils.now
    PrivateStateEntity(
      id = Option(UUID.randomUUID()),
      publicStateId = publicState.id.get,
      name = name,
      logo = None,
      socialOrder = socialOrder,
      masterId = None,
      createdBy = createdBy.id.get,
      journalistId = None,
      ts = now,
      lm = now,
      v = 0
    )
  }
}
