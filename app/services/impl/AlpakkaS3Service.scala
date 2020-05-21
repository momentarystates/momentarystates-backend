package services.impl

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.alpakka.s3._
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{Attributes, Materializer}
import akka.util.ByteString
import commons.AppError
import controllers.AppErrors
import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Logger}
import services.S3Service
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, AwsCredentials, AwsCredentialsProvider}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.regions.providers.AwsRegionProvider

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class AlpakkaS3Service @Inject()(
    config: Configuration,
    system: ActorSystem,
    materializer: Materializer
) extends S3Service {

  private val logger = Logger(classOf[AlpakkaS3Service])

  private val s3Host      = config.get[String]("app.s3.host")
  private val s3AccessKey = config.get[String]("app.s3.accessKey")
  private val s3SecretKey = config.get[String]("app.s3.secretKey")
  private val s3Region    = config.get[String]("app.s3.region")

  private val credentialsProvider = new AwsCredentialsProvider {
    override def resolveCredentials(): AwsCredentials = AwsBasicCredentials.create(s3AccessKey, s3SecretKey)
  }

  private val regionProvider = new AwsRegionProvider {
    override def getRegion: Region = Region.of(s3Region)
  }

  private val settings: S3Settings = S3Ext(system).settings
    .withEndpointUrl(s3Host)
    .withBufferType(MemoryBufferType)
    .withCredentialsProvider(credentialsProvider)
    .withListBucketApiVersion(ApiVersion.ListBucketVersion2)
    .withS3RegionProvider(regionProvider)

  implicit private val attributes: Attributes = S3Attributes.settings(settings)

  override def put(bucketName: String, fileName: String, data: Array[Byte]): Future[Option[AppError]] = {

    def createBucketIfNotExist() = {
      S3.checkIfBucketExistsSource(bucketName)
        .withAttributes(attributes)
        .runWith(Sink.head)(materializer) flatMap {
        case BucketAccess.NotExists =>
          logger.info(s"s3 bucket does not exist. create a new one with name $bucketName")
          S3.makeBucketSource(bucketName)
            .withAttributes(attributes)
            .runWith(Sink.head)(materializer) map { _ =>
            None
          }
        case BucketAccess.AccessDenied =>
          logger.info("access denied")
          Future.successful(Option(AppErrors.AccessS3BucketError))
        case BucketAccess.AccessGranted =>
          logger.info("bucket exists. just continue")
          Future.successful(None)
        case _ =>
          logger.info("unknown error")
          Future.successful(Option(AppErrors.UnknownS3ServiceError))
      }
    }

    def addBinaryToBucket(errorOpt: Option[AppError]): Future[Option[AppError]] = {

      logger.info("add a new binary to bucket: " + bucketName)

      if (errorOpt.isDefined) Future.successful(errorOpt)
      else {

        val file: Source[ByteString, NotUsed] = Source.single(ByteString(data))
        val s3Sink                            = S3.multipartUpload(bucketName, fileName).withAttributes(attributes)

        try {
          file.runWith(s3Sink)(materializer) map { _ =>
            None
          }
        } catch {
          case e: Exception => Future.successful(Some(AppErrors.UnknownS3ServiceError.error(e.getMessage)))
        }
      }
    }

    for {
      errorOpt  <- createBucketIfNotExist()
      errorOpt2 <- addBinaryToBucket(errorOpt)
    } yield errorOpt2
  }

  override def get(bucketName: String, fileName: String): Future[Either[AppError, Source[ByteString, _]]] = {

    val s3File: Source[Option[(Source[ByteString, NotUsed], ObjectMetadata)], NotUsed] =
      S3.download(bucketName, fileName).withAttributes(attributes)

    s3File.runWith(Sink.head)(materializer) map {
      case Some(result) => Right(result._1)
      case _            => Left(AppErrors.UnknownS3ServiceError.error("did not get any result from S3"))
    }
  }

  override def delete(bucketName: String, fileName: String): Future[Option[AppError]] = {
    S3.deleteObject(bucketName, fileName, None)
      .withAttributes(attributes)
      .runWith(Sink.seq)(materializer)
      .map(_ => None)
  }

  override def deleteByPrefix(bucketName: String, prefix: String): Future[Option[AppError]] = {
    S3.deleteObjectsByPrefix(bucketName, Option(prefix))
      .withAttributes(attributes)
      .runWith(Sink.seq)(materializer)
      .map(_ => None)
  }
}
