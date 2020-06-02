package persistence

import com.github.tminglei.slickpg._
import persistence.model.{EmailStatus, PublicStateStatus, SocialOrder, UserRole}
import play.api.libs.json.{JsValue, Json}
import slick.jdbc.JdbcType

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
      new AdvancedArrayJdbcType[JsValue](pgjson, (s) => utils.SimpleArrayUtils.fromString[JsValue](Json.parse)(s).orNull, (v) => utils.SimpleArrayUtils.mkString[JsValue](_.toString())(v)).to(_.toList)

    implicit val userRoleTypeMapper: JdbcType[UserRole.Value] = createEnumJdbcType("role", UserRole)

    implicit val emailStatusTypeMapper: JdbcType[EmailStatus.Value] = createEnumJdbcType("status", EmailStatus)

    implicit val publicStateStatusTypeMapper: JdbcType[PublicStateStatus.Value] = createEnumJdbcType("status", PublicStateStatus)

    implicit val socialOrderTypeMapper: JdbcType[SocialOrder.Value] = createEnumJdbcType("social_order", SocialOrder)
  }
}

object AppPostgresProfile extends AppPostgresProfile
