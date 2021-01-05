import LayersPostgis.Postgres
import zio._
import zio.blocking.Blocking
import zio.test.Assertion._
import zio.test._


object PgItTest extends DefaultRunnableSpec {

  type TestEnv = zio.test.environment.TestEnvironment

  lazy val pgLayer: ZLayer[Any, Nothing, Postgres] = Blocking.live >>> LayersPostgis.postgresLayer
  lazy val suiteLayers = zio.test.environment.testEnvironment ++ pgLayer

  def aam(i:Int): URIO[ServiceModule.Aa,String] = ZIO.accessM(_.get.helloA(i))

  override def spec =
    (suite("zio test suite with docker postgis")(
      testM("testM hello1") {
        (for {
          _ <- aam(11)
          validationresponse <- Task.succeed("hello")
        } yield assert(validationresponse)(equalTo("hello")))
      }.provideLayer(ServiceModule.Aa.fake),
      testM("testM hello2") {
        (for {
          validationresponse <- Task.succeed("hello")
        } yield assert(validationresponse)(equalTo("hello")))
      }
    ) @@ TestAspect.sequential  @@ MigrationAspects.migrate).provideCustomLayer(suiteLayers)
}
