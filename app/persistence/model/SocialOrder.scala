package persistence.model

import play.api.libs.json.{Reads, Writes}

object SocialOrder extends Enumeration {
  val SinglePerson, SimpleMajority, Consensus, SinglePersonRotation = Value

  implicit val jsonReads: Reads[SocialOrder.Value]   = Reads.enumNameReads(SocialOrder)
  implicit val jsonWrites: Writes[SocialOrder.Value] = Writes.enumNameWrites
}
