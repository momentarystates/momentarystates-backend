package controllers.api.user

import commons.AppResult
import controllers.api.user.UserProtocol.UserData
import controllers.{AppActions, ControllerHelper}
import javax.inject.{Inject, Singleton}
import persistence.dao.{CitizenDao, PrivateStateDao, PublicStateDao}
import persistence.model.PublicStateStatus
import play.api.mvc.{AbstractController, ControllerComponents, EssentialAction}

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class UserController @Inject()(
    cc: ControllerComponents,
    actions: AppActions,
    publicStateDao: PublicStateDao,
    privateStateDao: PrivateStateDao,
    citizenDao: CitizenDao
) extends AbstractController(cc)
    with ControllerHelper {

  def getData: EssentialAction = actions.AuthenticatedAction.async { implicit request =>
    val data = for {
      citizens      <- citizenDao.byUser(request.auth.user)
      privateStates <- privateStateDao.byIds(citizens.map(_.privateStateId).distinct)
      publicStates  <- publicStateDao.byIds(privateStates.map(_.publicStateId).distinct, PublicStateStatus.Running)
      goddessStates <- publicStateDao.byGoddess(request.auth.user, Seq(PublicStateStatus.Created, PublicStateStatus.Running))
    } yield {
      val filteredPrivateStates = privateStates.filter(p => publicStates.exists(_.id.get == p.publicStateId))
      UserData(citizens, filteredPrivateStates, (publicStates ++ goddessStates).distinct)
    }

    val res = AppResult.fromFuture[UserData](data)

    res.runResult()
  }

}
