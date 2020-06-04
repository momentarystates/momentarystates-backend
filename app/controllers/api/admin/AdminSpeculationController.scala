package controllers.api.admin

import commons._
import controllers.api.admin.AdminProtocol.CreateSpeculation
import controllers.{AppActions, AppErrors, ControllerHelper}
import javax.inject.{Inject, Singleton}
import persistence.dao.{EmailDao, SpeculationDao}
import persistence.model.{EmailEntity, SpeculationEntity}
import play.api.Configuration
import play.api.mvc.{AbstractController, ControllerComponents, EssentialAction}
import scalaz.Scalaz._
import scalaz.{-\/, \/-}

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class AdminSpeculationController @Inject()(
    cc: ControllerComponents,
    config: Configuration,
    actions: AppActions,
    speculationDao: SpeculationDao,
    emailDao: EmailDao
) extends AbstractController(cc)
    with ControllerHelper {

  def create(): EssentialAction = actions.AdminAction().async(parse.json) { implicit request =>
    val domain                = config.get[String]("app.domain")
    val registerPath          = config.get[String]("app.ui.registerPath")
    val createPublicStatePath = config.get[String]("app.ui.createPublicStatePath")
    val registerUrl           = domain + registerPath
    val createPublicStateUrl  = domain + createPublicStatePath

    def createSpeculation(in: CreateSpeculation) = {
      val speculation = SpeculationEntity.generate(in.email, in.token)
      speculationDao.insert(speculation) map {
        case Left(error) => -\/(AppErrors.DatabaseError(error))
        case Right(uuid) => \/-(speculation.copy(id = Option(uuid)))
      }
    }

    def sendEmail(in: CreateSpeculation, speculation: SpeculationEntity) = {
      val template = EmailTemplate.getCreatePublicStateEmailTemplate(request.auth.user.username, speculation.token, createPublicStateUrl, registerUrl)
      val email    = EmailEntity.generate(template.subject, List(in.email), template.body)
      emailDao.insert(email) map {
        case Left(error) => Option(AppErrors.DatabaseError(error))
        case Right(_)    => None
      }
    }

    val res = for {
      in          <- AppResult[CreateSpeculation](validateJson[CreateSpeculation](request))
      speculation <- AppResult[SpeculationEntity](createSpeculation(in))
      _           <- AppResult.fromFutureOptionError(sendEmail(in, speculation))
    } yield speculation

    res.runResult()
  }
}
