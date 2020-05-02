package controllers.api

import javax.inject.{Inject, Singleton}
import persistence.dao.UserDao
import persistence.model.UserEntity
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents, EssentialAction}

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class PlaygroundController @Inject()(
    cc: ControllerComponents,
    userDao: UserDao
) extends AbstractController(cc) {

  def test1(): EssentialAction = Action.async { implicit request =>
    val user = UserEntity.generate("testname", "testHash")
    userDao.insert(user) map {
      case Left(error) => BadRequest(Json.toJson(error))
      case Right(uuid) => Ok(Json.toJson(user.copy(id = Option(uuid))))
    }
  }
}
