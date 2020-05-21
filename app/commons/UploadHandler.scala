package commons

import java.io.IOException
import java.nio.file.Paths
import java.security.MessageDigest

import controllers.AppErrors
import javax.imageio.ImageIO
import org.apache.tika.config.TikaConfig
import org.apache.tika.io.TikaInputStream
import org.apache.tika.metadata.Metadata
import play.api.libs.Files
import play.api.mvc.MultipartFormData
import scalaz.{-\/, \/, \/-}

object UploadHandler {

  val defaultContentTypes: List[String] = List(
    "audio/mpeg",
    "audio/aac",
    "audio/mp4",
    "video/mp4",
    "image/png",
    "image/jpeg",
    "image/gif"
  )

  val imageContentTypes: List[String] = defaultContentTypes.filter(_.startsWith("image/"))

  def md5(a: Array[Byte]): String = MessageDigest.getInstance("MD5").digest(a).map("%02x".format(_)).mkString

  def processFilePart(filePart: MultipartFormData.FilePart[Files.TemporaryFile], maxSize: Int, validContentTypes: List[String]): AppError \/ Binary = {

    val byteArray = java.nio.file.Files.readAllBytes(Paths.get(filePart.ref.getAbsolutePath))

    def go(data: Array[Byte], contentType: String): Binary = {
      Binary(
        fileName = filePart.filename,
        contentType = contentType,
        length = data.length.toLong,
        md5 = md5(data),
        data = data
      )
    }

    if (byteArray.length.toLong <= maxSize) {

      val tika        = new TikaConfig()
      val metadata    = new Metadata()
      val mediaType   = tika.getDetector.detect(TikaInputStream.get(byteArray), metadata)
      val contentType = s"${mediaType.getType}/${mediaType.getSubtype}"

      if (validContentTypes.contains(contentType)) {

        // Additional checks for images
        if (mediaType.getType == "image") {
          try {
            // check if file is an image
            val image = ImageIO.read(filePart.ref)
            if (image == null) -\/(AppErrors.UploadImageError)
            else \/-(go(byteArray, contentType))
          } catch {
            case ex: IOException => -\/(AppErrors.UploadImageError.error("not an image: " + ex.getMessage))
          }
        } else \/-(go(byteArray, contentType))
      } else -\/(AppErrors.UnsupportedContentTypeError)
    } else -\/(AppErrors.MaxFileSizeError)
  }
}
