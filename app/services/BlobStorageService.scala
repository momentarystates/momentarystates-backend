package services

import akka.stream.scaladsl.Source
import akka.util.ByteString
import commons.AppError
import javax.inject.{Inject, Singleton}
import persistence.model.{BinaryEntity, UserEntity}

import scala.concurrent.Future

@Singleton
class BlobStorageService @Inject()(
    s3Service: S3Service
) {

  def putUserBinary(user: UserEntity, binary: BinaryEntity, data: Array[Byte]): Future[Option[AppError]] = {
    s3Service.put(userBucketName(user), binary.path, data)
  }

  def getUserBinary(user: UserEntity, binary: BinaryEntity): Future[Either[AppError, Source[ByteString, _]]] = {
    s3Service.get(userBucketName(user), binary.path)
  }

  def deleteUserBinary(user: UserEntity, binary: BinaryEntity): Future[Option[AppError]] = {
    s3Service.delete(userBucketName(user), binary.path)
  }

  private def userBucketName(user: UserEntity): String = s"dgdg-user-avatar-${user.id.get}"

}
