package controllers.api.user

import commons._
import controllers.{AppActions, AppErrors, ControllerHelper}
import javax.inject.{Inject, Singleton}
import persistence.dao.{BinaryDao, UserDao}
import persistence.model.BinaryEntity
import play.api.Configuration
import play.api.mvc.{AbstractController, ControllerComponents, EssentialAction}
import scalaz.Scalaz._
import scalaz.{-\/, \/, \/-}
import services.BlobStorageService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class UserAvatarController @Inject()(
    cc: ControllerComponents,
    actions: AppActions,
    config: Configuration,
    userDao: UserDao,
    binaryDao: BinaryDao,
    blobStorageService: BlobStorageService
) extends AbstractController(cc)
    with ControllerHelper {

  def setAvatar(): EssentialAction = actions.AuthenticatedAction.async(parse.multipartFormData) { implicit request =>
    val avatarMaxFileSize = config.get[Int]("app.files.avatarMaxFileSize")

    def readImage: AppError \/ Binary = UploadHandler.processFilePart(request.body.files.head, avatarMaxFileSize, UploadHandler.imageContentTypes)

    def createBinary(binary: Binary) = {
      val entity = BinaryEntity.generate(
        binary.fileName,
        binary.contentType,
        binary.length,
        binary.md5
      )
      blobStorageService.putUserBinary(request.auth.user, entity, binary.data) flatMap {
        case Some(error) => Future.successful(-\/(error))
        case _ =>
          binaryDao.insert(entity) map {
            case Left(error) => -\/(AppErrors.DatabaseError(error))
            case Right(uuid) => \/-(entity.copy(id = Option(uuid)))
          }
      }
    }

    val res = for {
      _      <- AppResult(request.body.files.nonEmpty)(AppErrors.NoFileSpecifiedError)
      _      <- AppResult(request.body.files.size == 1)(AppErrors.TooManyFilesSpecifiedError)
      binary <- AppResult[Binary](readImage)
      entity <- AppResult[BinaryEntity](createBinary(binary))
      user   <- userDao.update(request.auth.user.copy(avatar = Option(entity.id.get))).toAppResult()
    } yield user

    res.runResult
  }
}
