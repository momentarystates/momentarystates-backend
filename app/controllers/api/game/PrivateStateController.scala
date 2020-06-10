package controllers.api.game

import java.util.UUID

import commons._
import controllers.api.game.GameProtocol.{CreatePrivateState, CreatedPrivateState, PrivateStateInvite}
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

    def handleSocialOrder(privateState: PrivateStateEntity, citizen: CitizenEntity) = {
      if (privateState.socialOrder == SocialOrder.SinglePerson || privateState.socialOrder == SocialOrder.SinglePersonRotation) {
        privateStateDao.update(privateState.copy(masterId = citizen.id)) map {
          case Left(error)          => -\/(AppErrors.DatabaseError(error))
          case Right(updatedEntity) => \/-(updatedEntity)
        }
      } else Future.successful(\/-(privateState))
    }

    val res = for {
      in                  <- AppResult[CreatePrivateState](validateJson[CreatePrivateState](request))
      publicState         <- publicStateDao.byId(in.publicStateId).handleEntityNotFound("PublicState")
      _                   <- AppResult(publicState.goddessId != request.auth.user.id.get)(AppErrors.GoddessCantCreatePrivateStateError)
      _                   <- AppResult.fromFutureOptionError(validate(publicState))
      privateState        <- AppResult[PrivateStateEntity](createPrivateState(in, publicState))
      citizen             <- AppResult[CitizenEntity](createCitizenship(privateState, in))
      updatedPrivateState <- AppResult[PrivateStateEntity](handleSocialOrder(privateState, citizen))
    } yield CreatedPrivateState(updatedPrivateState, citizen)

    res.runResult()
  }

  def invite(id: UUID): EssentialAction = actions.CitizenAction(id).async(parse.json) { implicit request =>
    val domain                                                   = config.get[String]("app.domain")
    val registerPath                                             = config.get[String]("app.ui.registerPath")
    val joinPrivateStatePath                                     = config.get[String]("app.ui.joinPrivateStatePath")
    val registerUrl                                              = domain + registerPath
    def joinPrivateStateUrl(token: String, privateStateId: UUID) = domain + joinPrivateStatePath.replace(":token", token).replace(":privateStateId", privateStateId.toString)

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

    def sendEmail(invite: JoinPrivateStateInviteEntity, privateState: PublicStateEntity) = {
      val template = EmailTemplate.getCreatePrivateStateInviteEmailTemplate(request.auth.user.username, joinPrivateStateUrl(invite.token, privateState.id.get), registerUrl)
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
      _      <- AppResult.fromFutureOptionError(sendEmail(invite, request.publicState))
    } yield ""

    res.runResultEmptyOk()
  }

  def join(id: UUID): EssentialAction = actions.PrivateStateAction(id).async(parse.json) { implicit request =>
    Future.successful(NotImplemented)
  }
}
