package controllers.api.game

import java.util.UUID

import commons._
import controllers.api.game.GameProtocol.{Citizenship, CreatePrivateState, JoinPrivateState, PrivateStateInvite}
import controllers.{AppActions, AppErrors, ControllerHelper}
import javax.inject.{Inject, Singleton}
import persistence.dao._
import persistence.model._
import play.api.Configuration
import play.api.mvc.{AbstractController, ControllerComponents, EssentialAction}
import scalaz.Scalaz._
import scalaz.{-\/, \/-}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class PrivateStateController @Inject()(
    cc: ControllerComponents,
    actions: AppActions,
    config: Configuration,
    publicStateDao: PublicStateDao,
    privateStateDao: PrivateStateDao,
    citizenDao: CitizenDao,
    emailDao: EmailDao,
    joinPrivateStateInviteDao: JoinPrivateStateInviteDao
) extends AbstractController(cc)
    with ControllerHelper {

  def create(): EssentialAction = actions.AuthenticatedAction.async(parse.json) { implicit request =>
    def validate(publicState: PublicStateEntity): Future[Option[AppError]] = {
      for {
        privateStates <- privateStateDao.byPublicState(publicState)
        citizens      <- citizenDao.activeByPrivateStates(privateStates)
      } yield {
        if (citizens.exists(_.userId == request.auth.user.id.get)) Option(AppErrors.AlreadyActiveCitizenError)
        else None
      }
    }

    def createPrivateState(in: CreatePrivateState, publicState: PublicStateEntity) = {
      val entity = PrivateStateEntity.generate(publicState, in.name, in.socialOrder, request.auth.user, in.characteristics)
      privateStateDao.insert(entity) map {
        case Left(error) => -\/(AppErrors.DatabaseError(error))
        case Right(uuid) => \/-(entity.copy(id = Option(uuid)))
      }
    }

    def createCitizenship(privateState: PrivateStateEntity, in: CreatePrivateState) = {
      val entity = CitizenEntity.generate(request.auth.user, privateState, in.citizenName)
      citizenDao.insert(entity) map {
        case Left(error) => -\/(AppErrors.DatabaseError(error))
        case Right(uuid) => \/-(entity.copy(id = Option(uuid)))
      }
    }

    def postProcessPrivateState(publicState: PublicStateEntity, privateState: PrivateStateEntity, citizen: CitizenEntity) = {
      val masterId = if (privateState.socialOrder == SocialOrder.SinglePerson || privateState.socialOrder == SocialOrder.SinglePersonRotation) citizen.id else None
      val status   = if (publicState.params.minCitizenPerState <= 1) PrivateStateStatus.Founded else PrivateStateStatus.Founding
      privateStateDao.update(privateState.copy(masterId = masterId, status = status)) map {
        case Left(error)          => -\/(AppErrors.DatabaseError(error))
        case Right(updatedEntity) => \/-(updatedEntity)
      }
    }

    val res = for {
      in                  <- AppResult[CreatePrivateState](validateJson[CreatePrivateState](request))
      publicState         <- publicStateDao.byId(in.publicStateId).handleEntityNotFound("PublicState")
      _                   <- AppResult(publicState.goddessId != request.auth.user.id.get)(AppErrors.GoddessCantCreatePrivateStateError)
      _                   <- AppResult.fromFutureOptionError(validate(publicState))
      privateState        <- AppResult[PrivateStateEntity](createPrivateState(in, publicState))
      citizen             <- AppResult[CitizenEntity](createCitizenship(privateState, in))
      updatedPrivateState <- AppResult[PrivateStateEntity](postProcessPrivateState(publicState, privateState, citizen))
    } yield Citizenship(updatedPrivateState, citizen)

    res.runResult()
  }

  def invite(id: UUID): EssentialAction = actions.CitizenAction(id).async(parse.json) { implicit request =>
    val domain                                                   = config.get[String]("app.domain")
    val registerPath                                             = config.get[String]("app.ui.registerPath")
    val joinPrivateStatePath                                     = config.get[String]("app.ui.joinPrivateStatePath")
    val registerUrl                                              = domain + "://" + registerPath
    def joinPrivateStateUrl(token: String, privateStateId: UUID) = domain + "://" + joinPrivateStatePath.replace(":token", token).replace(":privateStateId", privateStateId.toString)

    def validateSocialOrder() = {
      request.privateState.socialOrder match {
        case SocialOrder.SinglePerson => None
        case SocialOrder.Consensus    => None
        case _ =>
          if (request.privateState.masterId.isDefined && request.privateState.masterId.get == request.citizen.id.get) None
          else Option(AppErrors.ForbiddenDueToSocialOrderError)
      }
    }

    def createInvite(in: PrivateStateInvite, privateState: PublicStateEntity) = {
      val entity = JoinPrivateStateInviteEntity.generate(privateState.id.get, in.email)
      joinPrivateStateInviteDao.insert(entity) map {
        case Left(error) => -\/(AppErrors.DatabaseError(error))
        case Right(uuid) => \/-(entity.copy(id = Option(uuid)))
      }
    }

    def sendEmail(invite: JoinPrivateStateInviteEntity, privateState: PublicStateEntity, publicState: PublicStateEntity) = {
      val template = EmailTemplate.getJoinPrivateStateInviteEmailTemplate(request.auth.user.username, publicState.name, joinPrivateStateUrl(invite.token, privateState.id.get), registerUrl)
      val email    = EmailEntity.generate(template.subject, Seq(invite.email), template.body)
      emailDao.insert(email) map {
        case Left(error) => Option(AppErrors.DatabaseError(error))
        case Right(_)    => None
      }
    }

    val res = for {
      in     <- AppResult[PrivateStateInvite](validateJson[PrivateStateInvite](request))
      _      <- AppResult.fromOptionError(validateSocialOrder())
      invite <- AppResult[JoinPrivateStateInviteEntity](createInvite(in, request.publicState))
      _      <- AppResult.fromFutureOptionError(sendEmail(invite, request.publicState, request.publicState))
    } yield ""

    res.runResultEmptyOk()
  }

  def join(id: UUID): EssentialAction = actions.PrivateStateAction(id).async(parse.json) { implicit request =>
    def checkCitizenship() = {
      for {
        privateStates <- privateStateDao.byPublicState(request.publicState)
        citizenships  <- citizenDao.activeByPrivateStates(privateStates)
      } yield {
        val activeCitizenships       = citizenships.filter(_.endedAt.isEmpty)
        val privateStateCitizenships = activeCitizenships.filter(_.privateStateId == request.privateState.id.get)
        if (activeCitizenships.exists(_.userId == request.auth.user.id.get)) -\/(AppErrors.AlreadyActiveCitizenError)
        else if (request.publicState.params.maxCitizenPerState > 0 && privateStateCitizenships.size >= request.publicState.params.maxCitizenPerState) -\/(AppErrors.MaxCitizenPerPrivateStateError)
        else \/-(activeCitizenships)
      }
    }

    def createCitizenship(in: JoinPrivateState) = {
      val entity = CitizenEntity.generate(request.auth.user, request.privateState, in.citizenName)
      citizenDao.insert(entity) map {
        case Left(error) => -\/(AppErrors.DatabaseError(error))
        case Right(uuid) => \/-(entity.copy(id = Option(uuid)))
      }
    }

    def postProcessPrivateState(citizens: Seq[CitizenEntity]) = {
      if (citizens.size >= request.publicState.params.minCitizenPerState) {
        privateStateDao.update(request.privateState.copy(status = PrivateStateStatus.Founded)) map {
          case Left(error)          => -\/(AppErrors.DatabaseError(error))
          case Right(updatedEntity) => \/-(updatedEntity)
        }
      } else Future.successful(\/-(request.privateState))
    }

    val res = for {
      in                  <- AppResult[JoinPrivateState](validateJson[JoinPrivateState](request))
      activeCitizens      <- AppResult[Seq[CitizenEntity]](checkCitizenship())
      invite              <- joinPrivateStateInviteDao.byToken(request.privateState, in.token).handleEntityNotFound("joinInvite")
      _                   <- AppResult(invite.usedAt.isEmpty)(AppErrors.JoinPrivateStateInviteAlreadyUsedError)
      citizen             <- AppResult[CitizenEntity](createCitizenship(in))
      _                   <- joinPrivateStateInviteDao.update(invite.copy(usedBy = request.auth.user.id, usedAt = Option(AppUtils.now))).toAppResult()
      updatedPrivateState <- AppResult[PrivateStateEntity](postProcessPrivateState(activeCitizens :+ citizen))
    } yield Citizenship(updatedPrivateState, citizen)

    res.runResult()
  }
}
