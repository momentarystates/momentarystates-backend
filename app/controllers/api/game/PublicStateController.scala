package controllers.api.game

import java.util.UUID

import commons._
import controllers.api.game.GameProtocol.CreatePublicState
import controllers.{AppActions, AppErrors, ControllerHelper}
import javax.inject.{Inject, Singleton}
import persistence.dao.{PublicStateDao, SpeculationDao}
import persistence.model.{PublicStateEntity, PublicStateParams, PublicStateStatus, SpeculationEntity}
import play.api.mvc.{AbstractController, ControllerComponents, EssentialAction}
import scalaz.Scalaz._
import scalaz.{-\/, \/-}

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class PublicStateController @Inject()(
    cc: ControllerComponents,
    actions: AppActions,
    speculationDao: SpeculationDao,
    publicStateDao: PublicStateDao
) extends AbstractController(cc)
    with ControllerHelper {

  def create(): EssentialAction = actions.AuthenticatedAction.async(parse.json) { implicit request =>
    def createPublicState(in: CreatePublicState, speculation: SpeculationEntity) = {
      val params = PublicStateParams(
        minCitizenPerState = in.minCitizenPerState.getOrElse(-1),
        maxCitizenPerState = in.maxCitizenPerState.getOrElse(-1),
        consensusFactor = in.consensusFactor.getOrElse(100),
        sizeInfluence = in.sizeInfluence.getOrElse(0.0f),
        speculationDuration = in.speculationDuration.getOrElse(10800), // 3 hours
        rotationDuration = in.rotationDuration.getOrElse(1200), // 20 minutes
        ruleProposalDuration = in.ruleProposalDuration.getOrElse(1200), // 20 minutes
        ruleProposalIncrement = in.ruleProposalIncrement.getOrElse(60)  // 1 minute
      )
      val entity = PublicStateEntity.generate(speculationId = speculation.id.get, name = in.name, goddessId = request.auth.user.id.get, params = params)
      publicStateDao.insert(entity) map {
        case Left(error) => -\/(AppErrors.DatabaseError(error))
        case Right(uuid) => \/-(entity.copy(id = Option(uuid)))
      }
    }

    val res = for {
      in          <- AppResult[CreatePublicState](validateJson[CreatePublicState](request))
      speculation <- speculationDao.byId(in.speculationId).handleEntityNotFound("speculation")
      _           <- AppResult(speculation.token == in.token)(AppErrors.InvalidSpeculationTokenError)
      publicState <- AppResult[PublicStateEntity](createPublicState(in, speculation))
    } yield publicState

    res.runResult()
  }

  def start(id: UUID): EssentialAction = actions.GoddessAction(id).async { implicit request =>
    val res = for {
      _                  <- AppResult(request.publicState.status == PublicStateStatus.Created)(AppErrors.InvalidPublicStateStatusError)
      updatedPublicState <- publicStateDao.update(request.publicState.copy(status = PublicStateStatus.Running)).toAppResult()
    } yield updatedPublicState

    res.runResult()
  }

  def stop(id: UUID): EssentialAction = actions.RunningPublicStateAction(id).async { implicit request =>
    publicStateDao
      .update(request.publicState.copy(status = PublicStateStatus.Finished))
      .toAppResult()
      .runResult()
  }
}
