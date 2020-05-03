package persistence.model

import play.api.libs.json.{Reads, Writes}

object UserRole extends Enumeration {
  val User, Admin = Value

  implicit val jsonReads: Reads[UserRole.Value] = Reads.enumNameReads(UserRole)
  implicit val jsonWrites: Writes[UserRole.Value] = Writes.enumNameWrites
}
