package services

import akka.stream.scaladsl.Source
import akka.util.ByteString
import commons.AppError

import scala.concurrent.Future

trait S3Service {

  def put(bucketName: String, fileName: String, data: Array[Byte]): Future[Option[AppError]]

  def get(bucketName: String, fileName: String): Future[Either[AppError, Source[ByteString, _]]]

  def delete(bucketName: String, fileName: String): Future[Option[AppError]]

  def deleteByPrefix(bucketName: String, prefix: String): Future[Option[AppError]]
}
