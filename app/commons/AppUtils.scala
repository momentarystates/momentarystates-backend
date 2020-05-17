package commons

import java.time.{Instant, OffsetDateTime, ZoneOffset}

import scala.util.Random

object AppUtils {
  final def now: OffsetDateTime                     = Instant.now().atOffset(ZoneOffset.UTC)
  final def randomAlphanumeric(length: Int): String = (Random.alphanumeric take length).mkString
  final def otp = f"${Random.nextInt(1000000)}%06d"
}
