package commons

import java.time.ZoneOffset

import com.github.tminglei.slickpg._
import play.api.libs.json.{JsValue, Json}

trait BaPostgresProfile
    extends ExPostgresProfile
    with PgArraySupport
    with PgDate2Support
    with PgRangeSupport
    with PgHStoreSupport
    with PgPlayJsonSupport
    with PgSearchSupport
    with PgNetSupport
    with PgLTreeSupport {

  val ZoneOffsetUTC: ZoneOffset = ZoneOffset.UTC

  def pgjson = "jsonb"

  override val api = BackendApi

  object BackendApi
      extends API
      with ArrayImplicits
      with DateTimeImplicits
      with JsonImplicits
      with NetImplicits
      with LTreeImplicits
      with RangeImplicits
      with HStoreImplicits
      with SearchImplicits
      with SearchAssistants {

    implicit val strListTypeMapper = new SimpleArrayJdbcType[String]("text").to(_.toList)

    implicit val playJsonArrayTypeMapper =
      new AdvancedArrayJdbcType[JsValue](pgjson,
                                         (s) => utils.SimpleArrayUtils.fromString[JsValue](Json.parse)(s).orNull,
                                         (v) => utils.SimpleArrayUtils.mkString[JsValue](_.toString())(v)).to(_.toList)

//    implicit val localDateColumnType = MappedColumnType.base[LocalDate, Timestamp](
//      localDate => new Timestamp(localDate.atStartOfDay().toInstant(ZoneOffsetUTC).toEpochMilli),
//      timestamp => timestamp.toLocalDateTime.toLocalDate
//    )
//
//    implicit val offsetDateTimeColumnType = MappedColumnType.base[OffsetDateTime, Timestamp](
//      dateTime => new Timestamp(dateTime.toInstant.toEpochMilli),
//      timestamp => timestamp.toInstant.atOffset(ZoneOffsetUTC)
//    )

  }
}

object BaPostgresProfile extends BaPostgresProfile
