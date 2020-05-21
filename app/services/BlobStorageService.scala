package services

import commons.AppError
import javax.inject.{Inject, Singleton}
import persistence.model.{BinaryEntity, UserEntity}

import scala.concurrent.Future

@Singleton
class BlobStorageService @Inject()(
    s3Service: S3Service
) {

  def putUserBinary(user: UserEntity, binary: BinaryEntity, data: Array[Byte]): Future[Option[AppError]] = {
    s3Service.put(s"dgdg-user-avatar-${user.id.get}", binary.path, data)
  }

}
