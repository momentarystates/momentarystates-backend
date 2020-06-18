package controllers.api.game

import java.util.UUID

import commons._
import controllers.api.game.GameProtocol.{CreatePublicState, PrivateStateInvite, UpdatePublicState}
import controllers.{AppActions, AppErrors, ControllerHelper}
import javax.inject.{Inject, Singleton}
import persistence.dao.{CreatePrivateStateInviteDao, EmailDao, PublicStateDao, SpeculationDao}
import persistence.model._
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents, EssentialAction}
import scalaz.Scalaz._
import scalaz.{-\/, \/-}

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class PublicStateController @Inject()(
    cc: ControllerComponents,
    actions: AppActions,
    speculationDao: SpeculationDao,
    publicStateDao: PublicStateDao,
    emailDao: EmailDao,
    createPrivateStateInviteDao: CreatePrivateStateInviteDao,
    config: Configuration
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
        ruleProposalIncrement = in.ruleProposalIncrement.getOrElse(60) // 1 minute
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

  def stop(id: UUID): EssentialAction = actions.GoddessAction(id).async { implicit request =>
    publicStateDao
      .update(request.publicState.copy(status = PublicStateStatus.Finished))
      .toAppResult()
      .runResult()
  }

  def invite(id: UUID): EssentialAction = actions.RunningPublicStateAction(id).async(parse.json) { implicit request =>
    val domain                                                    = config.get[String]("app.domain")
    val registerPath                                              = config.get[String]("app.ui.registerPath")
    val createPrivateStatePath                                    = config.get[String]("app.ui.createPrivateStatePath")
    val registerUrl                                               = domain + "://" + registerPath
    def createPrivateStateUrl(token: String, publicStateId: UUID) = domain + "://" + createPrivateStatePath.replace(":token", token).replace(":publicStateId", publicStateId.toString)

    def createInvite(in: PrivateStateInvite, publicState: PublicStateEntity) = {
      val entity = CreatePrivateStateInviteEntity.generate(publicState.id.get, in.email)
      createPrivateStateInviteDao.insert(entity) map {
        case Left(error) => -\/(AppErrors.DatabaseError(error))
        case Right(uuid) => \/-(entity.copy(id = Option(uuid)))
      }
    }

    def sendEmail(invite: CreatePrivateStateInviteEntity, publicState: PublicStateEntity) = {
      val template = EmailTemplate.getCreatePrivateStateInviteEmailTemplate(request.auth.user.username, publicState.name, createPrivateStateUrl(invite.token, publicState.id.get), registerUrl)
      val email    = EmailEntity.generate(template.subject, Seq(invite.email), template.body)
      emailDao.insert(email) map {
        case Left(error) => Option(AppErrors.DatabaseError(error))
        case Right(_)    => None
      }
    }

    val res = for {
      in     <- AppResult[PrivateStateInvite](validateJson[PrivateStateInvite](request))
      invite <- AppResult[CreatePrivateStateInviteEntity](createInvite(in, request.publicState))
      _      <- AppResult.fromFutureOptionError(sendEmail(invite, request.publicState))
    } yield ""

    res.runResultEmptyOk()
  }

  def invites(id: UUID): EssentialAction = actions.GoddessAction(id).async { implicit request =>
    createPrivateStateInviteDao.byPublicState(request.publicState, request.getQueryString("email")) map { invites =>
      Ok(Json.toJson(invites))
    }
  }

  def update(id: UUID): EssentialAction = actions.GoddessAction(id).async(parse.json) { implicit request =>
    val res = for {
      in                 <- AppResult[UpdatePublicState](validateJson[UpdatePublicState](request))
      updatedPublicState <- publicStateDao.update(request.publicState.copy(marketUrl = in.marketUrl, params = in.params)).toAppResult()
    } yield updatedPublicState
    res.runResult()
  }
}
