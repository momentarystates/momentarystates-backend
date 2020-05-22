package controllers.api.user

import java.util.{Date, UUID}

import commons._
import controllers.{AppActions, AppErrors, ControllerHelper}
import javax.inject.{Inject, Singleton}
import persistence.dao.{BinaryDao, UserDao}
import persistence.model.{BinaryEntity, UserEntity}
import play.api.Configuration
import play.api.mvc.{AbstractController, ControllerComponents, EssentialAction, Results}
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
      _      <- AppResult.fromFutureOptionError(deleteAvatarIfExistent(request.auth.user))
      binary <- AppResult[Binary](readImage)
      entity <- AppResult[BinaryEntity](createBinary(binary))
      user   <- userDao.update(request.auth.user.copy(avatar = Option(entity.id.get))).toAppResult()
    } yield user

    res.runResult
  }

  def getAvatar(id: UUID): EssentialAction = actions.LoggingAction.async { implicit request =>
    val res = for {
      user     <- userDao.byId(id).handleEntityNotFound("user")
      binaryId <- AppResult.fromOption(user.avatar)(AppErrors.NotFoundError)
      binary   <- binaryDao.byId(binaryId).handleEntityNotFound("binary")
      source   <- blobStorageService.getUserBinary(user, binary).toAppResult()
    } yield (binary, source)

    res.run.map {
      case -\/(error) => ErrorResult(error, Results.NotFound)
      case \/-((binary, source)) =>
        Ok.chunked(source)
          .withHeaders(
            "Accept-Ranges" -> "bytes",
            "Cache-Control" -> "max-age=864000",
            "Last-Modified" -> s"${new Date().getTime}"
          )
          .as(binary.contentType)
    }
  }

  private def deleteAvatarIfExistent(user: UserEntity) = {
    user.avatar match {
      case Some(uuid) =>
        val res = for {
          binary <- binaryDao.byId(uuid).handleEntityNotFound("binary")
          _      <- AppResult.fromFutureOptionError(blobStorageService.deleteUserBinary(user, binary))
          _      <- binaryDao.delete(binary.id.get).handleDeleteEntityError()
        } yield ""
        res.run map {
          case -\/(error) => Option(error)
          case \/-(_)     => None
        }
      case _ => Future.successful(None)
    }
  }

}
