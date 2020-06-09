package persistence

import com.github.tminglei.slickpg._
import persistence.model._
import play.api.libs.json.{JsValue, Json}
import slick.basic.Capability
import slick.jdbc.{JdbcCapabilities, JdbcType}

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

  override protected def computeCapabilities: Set[Capability] = super.computeCapabilities + JdbcCapabilities.insertOrUpdate

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

    implicit val strSeqTypeMapper = new SimpleArrayJdbcType[String]("text").to(_.toSeq)

    implicit val playJsonArrayTypeMapper =
      new AdvancedArrayJdbcType[JsValue](pgjson, (s) => utils.SimpleArrayUtils.fromString[JsValue](Json.parse)(s).orNull, (v) => utils.SimpleArrayUtils.mkString[JsValue](_.toString())(v)).to(_.toList)

    implicit val userRoleTypeMapper: JdbcType[UserRole.Value] = createEnumJdbcType("role", UserRole)

    implicit val emailStatusTypeMapper: JdbcType[EmailStatus.Value] = createEnumJdbcType("status", EmailStatus)

    implicit val publicStateStatusTypeMapper: JdbcType[PublicStateStatus.Value] = createEnumJdbcType("status", PublicStateStatus)

    implicit val socialOrderTypeMapper: JdbcType[SocialOrder.Value] = createEnumJdbcType("social_order", SocialOrder)

    implicit val citizenshipEndReasonTypeMapper: JdbcType[CitizenshipEndReason.Value] = createEnumJdbcType("end_reason", CitizenshipEndReason)

    implicit val publicStateParamsTypeMapper = MappedJdbcType.base[PublicStateParams, JsValue](Json.toJson(_), _.as[PublicStateParams])

    implicit val privateStateStatusTypeMapper: JdbcType[PrivateStateStatus.Value] = createEnumJdbcType("end_reason", PrivateStateStatus)
  }
}

object AppPostgresProfile extends AppPostgresProfile
