import zio._
import zio.blocking.Blocking
import zio.test.Assertion._
import zio.test._


object PgItTest extends DefaultRunnableSpec {

  type TestEnv = zio.test.environment.TestEnvironment

  val suiteLayers = zio.test.environment.testEnvironment ++ (Blocking.live >>> LayersPostgis.postgresLayer)

  override def spec: ZSpec[TestEnv, Throwable] =
    suite("zio test suite with docker postgis")(
      testM("testM hello") {
        (for {
          validationresponse <- Task.succeed("hello")
        } yield assert(validationresponse)(equalTo("hello")))
      }
    ).provideCustomLayer(suiteLayers)
}
