package base

import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Application
import play.api.db.evolutions.Evolutions
import play.api.db.{DBApi, Database}
import play.api.test.Injecting
import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres
import ru.yandex.qatools.embed.postgresql.distribution.Version

abstract class WithDatabase extends PlaySpec with GuiceOneAppPerTest with Injecting with BeforeAndAfterAll with ScalaFutures {

  private val postgres = new EmbeddedPostgres(Version.V10_3)

  override protected def beforeAll(): Unit = {
    postgres.start("localhost", 15432, "postgres", "postgres", "postgres")
    super.beforeAll()
  }

  override protected def afterAll(): Unit = {
    postgres.stop()
    super.afterAll()
  }

  final def withDatabase[T](block: => T)(implicit app: Application = app): Unit = {
    val dbApi: DBApi       = app.injector.instanceOf[DBApi]
    val database: Database = dbApi.database("default")

    Evolutions.applyEvolutions(database)
    block
    Evolutions.cleanupEvolutions(database)
  }

}
