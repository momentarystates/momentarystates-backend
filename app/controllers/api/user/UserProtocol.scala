package controllers.api.user

import persistence.model.{CitizenEntity, PrivateStateEntity, PublicStateEntity}
import play.api.libs.json.{Format, Json}

object UserProtocol {

  case class UserData(
      citizens: Seq[CitizenEntity],
      privateStates: Seq[PrivateStateEntity],
      publicStates: Seq[PublicStateEntity]
  )

  object UserData {
    implicit val jsonFormat: Format[UserData] = Json.format[UserData]
  }
}
