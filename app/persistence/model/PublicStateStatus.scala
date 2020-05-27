package persistence.model

import play.api.libs.json.{Reads, Writes}

object PublicStateStatus extends Enumeration {

  val Created, Running, Finished = Value

  implicit val jsonReads: Reads[PublicStateStatus.Value]   = Reads.enumNameReads(PublicStateStatus)
  implicit val jsonWrites: Writes[PublicStateStatus.Value] = Writes.enumNameWrites
}
