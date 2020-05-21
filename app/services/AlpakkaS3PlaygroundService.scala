package services

import akka.actor.ActorSystem
import akka.stream.alpakka.s3._
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.scaladsl.Sink
import akka.stream.{Attributes, Materializer}
import javax.inject.{Inject, Singleton}
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, AwsCredentials, AwsCredentialsProvider}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.regions.providers.AwsRegionProvider

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class AlpakkaS3PlaygroundService @Inject()(
    materializer: Materializer,
    system: ActorSystem,
) {

  def makeBucket(bucketName: String): Future[String] = {
    S3.makeBucket(bucketName)(materializer) map { _ =>
      "bucket created"
    }
  }

  def makeBucket2(bucketName: String): Future[String] = {

    val s3Host      = "http://localhost:9000"
    val s3AccessKey = "V0RFNPUE3O2PXN43R90S"
    val s3SecretKey = "n+aCkZNCGiJtoV9uNDUShG8m3bfTr16stmBvbbwR"
    val s3Region    = "eu-central-1"

    val credentialsProvider = new AwsCredentialsProvider {
      override def resolveCredentials(): AwsCredentials = AwsBasicCredentials.create(s3AccessKey, s3SecretKey)
    }

    val regionProvider = new AwsRegionProvider {
      override def getRegion: Region = Region.of(s3Region)
    }

    val settings: S3Settings = S3Ext(system).settings
      .withEndpointUrl(s3Host)
      .withBufferType(MemoryBufferType)
      .withCredentialsProvider(credentialsProvider)
      .withListBucketApiVersion(ApiVersion.ListBucketVersion2)
      .withS3RegionProvider(regionProvider)

    val attributes: Attributes = S3Attributes.settings(settings)

    S3.makeBucketSource(bucketName)
      .withAttributes(attributes)
      .runWith(Sink.head)(materializer) map { _ =>
      "bucket created"
    }
  }
}
