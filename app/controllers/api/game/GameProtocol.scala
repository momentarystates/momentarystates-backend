package controllers.api.game

import java.util.UUID

import persistence.model.{CitizenEntity, PrivateStateEntity, SocialOrder}
import play.api.libs.json.{Format, Json}

object GameProtocol {

  case class CreatePublicState(
      speculationId: UUID,
      token: String,
      name: String,
      minCitizenPerState: Option[Int],
      maxCitizenPerState: Option[Int],
      consensusFactor: Option[Int],
      sizeInfluence: Option[Double],
      speculationDuration: Option[Long],
      rotationDuration: Option[Long],
      ruleProposalDuration: Option[Long],
      ruleProposalIncrement: Option[Long]
  )

  object CreatePublicState {
    implicit val jsonFormat: Format[CreatePublicState] = Json.format[CreatePublicState]
  }

  case class CreatePrivateState(
      name: String,
      token: String,
      publicStateId: UUID,
      socialOrder: SocialOrder.Value,
      characteristics: Seq[String],
      citizenName: Option[String]
  )

  object CreatePrivateState {
    implicit val jsonFormat: Format[CreatePrivateState] = Json.format[CreatePrivateState]
  }

  case class PrivateStateInvite(
      email: String,
      token: Option[String]
  )

  object PrivateStateInvite {
    implicit val jsonFormat: Format[PrivateStateInvite] = Json.format[PrivateStateInvite]
  }

  case class Citizenship(
      privateState: PrivateStateEntity,
      citizen: CitizenEntity
  )

  object Citizenship {
    implicit val jsonFormat: Format[Citizenship] = Json.format[Citizenship]
  }

  case class JoinPrivateState(
      token: String,
      citizenName: Option[String]
  )

  object JoinPrivateState {
    implicit val jsonFormat: Format[JoinPrivateState] = Json.format[JoinPrivateState]
  }

}
