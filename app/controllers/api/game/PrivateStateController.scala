package controllers.api.game

import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, ControllerComponents}

@Singleton
class PrivateStateController @Inject()(
    cc: ControllerComponents
) extends AbstractController(cc) {}
