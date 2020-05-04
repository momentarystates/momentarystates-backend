package persistence

import com.github.tminglei.slickpg._
import persistence.model.UserRole
import play.api.libs.json.{JsValue, Json}

trait AppPostgresProfile
    extends ExPostgresProfile
    with PgArraySupport
    with PgDate2Support
    with PgEnumSupport
    with PgRangeSupport
    with PgHStoreSupport
    with PgPlayJsonSupport
    with PgSearchSupport
    with PgNetSupport
    with PgLTreeSupport {

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

    implicit val userRoleTypeMapper = createEnumJdbcType("role", UserRole)
  }
}

object AppPostgresProfile extends AppPostgresProfile
