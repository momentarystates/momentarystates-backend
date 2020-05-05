package commons

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

object AuthUtils {

  def createHash(password: String, salt: String): String = {
    val digest = MessageDigest.getInstance("SHA-256")
    val bytes  = digest.digest(s"$password$salt".getBytes(StandardCharsets.UTF_8))
    bytes.map("%02x".format(_)).mkString
  }

  def checkHash(password: String, salt: String, hash: String): Boolean = createHash(password, salt) == hash
}
