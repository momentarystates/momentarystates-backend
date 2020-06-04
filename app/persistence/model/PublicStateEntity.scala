package persistence.model

import java.time.OffsetDateTime
import java.util.UUID

import commons.AppUtils
import play.api.libs.json.{Format, Json}

case class PublicStateParams(
    minCitizenPerState: Int,
    maxCitizenPerState: Int,
    consensusFactor: Int,
    sizeInfluence: Double,
    speculationDuration: Long,
    rotationDuration: Long,
    ruleProposalDuration: Long,
    ruleProposalIncrement: Long
)

object PublicStateParams {
  implicit val jsonFormat: Format[PublicStateParams] = Json.format[PublicStateParams]
  def generate: PublicStateParams = PublicStateParams(
    minCitizenPerState = -1,
    maxCitizenPerState = -1,
    consensusFactor = 100,
    sizeInfluence = 0.0f,
    speculationDuration = 1000,
    rotationDuration = 1000,
    ruleProposalDuration = 1000,
    ruleProposalIncrement = 30,
  )
}

case class PublicStateEntity(
    id: Option[UUID],
    speculationId: UUID,
    name: String,
    logo: Option[UUID],
    goddess: UUID,
    status: PublicStateStatus.Value,
    startedAt: Option[OffsetDateTime],
    marketUrl: Option[String],
    params: PublicStateParams,
    isProcessing: Boolean,
    ts: OffsetDateTime,
    lm: OffsetDateTime,
    v: Int
)

object PublicStateEntity {
  implicit val jsonFormat: Format[PublicStateEntity] = Json.format[PublicStateEntity]

  def generate(speculationId: UUID, name: String, goddess: UUID, params: PublicStateParams): PublicStateEntity = {
    val now = AppUtils.now
    PublicStateEntity(
      id = Option(UUID.randomUUID),
      speculationId = speculationId,
      name = name,
      goddess = goddess,
      logo = None,
      status = PublicStateStatus.Created,
      params = params,
      startedAt = None,
      marketUrl = None,
      isProcessing = false,
      ts = now,
      lm = now,
      v = 0
    )
  }
}
