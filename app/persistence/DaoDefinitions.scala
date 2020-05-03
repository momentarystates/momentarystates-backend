package persistence

import java.time.{Instant, OffsetDateTime, ZoneOffset}

import slick.relational.RelationalProfile.ColumnOption.Length

import scala.util.Random

trait DaoDefinitions {
  final def now: OffsetDateTime = Instant.now().atOffset(ZoneOffset.UTC)
  final def randomAlphanumeric(length: Int): String = (Random.alphanumeric take length).mkString
}
