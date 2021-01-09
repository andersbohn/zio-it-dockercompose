import LayersPostgis.Postgres
import ServiceModule.Aa
import zio._
import zio.blocking.Blocking
import zio.test.Assertion._
import zio.test._
import zio.test.environment.TestEnvironment


object PgItBTest extends DefaultRunnableSpec {

  lazy val pgLayer: ZLayer[Any, Nothing, Postgres] = Blocking.live >>> LayersPostgis.postgresLayer
  lazy val suiteLayers = zio.test.environment.testEnvironment ++ pgLayer ++ ServiceModule.Aa.fake

  def aam(i: Int): URIO[ServiceModule.Aa, String] = ZIO.accessM(_.get.helloA(i))

  override def spec = { //: ZSpec[_root_.zio.test.environment.TestEnvironment, Any] = {
    val test1:ZSpec[Aa, Throwable] = testM("testM hello1") {
      (for {
        _ <- aam(11)
        validationresponse <- Task.succeed("hello")
      } yield assert(validationresponse)(equalTo("hello")))
    }
    val test2:ZSpec[Aa, Throwable] = testM("testM hello2") {
      (for {
        validationresponse <- Task.succeed("hello")
      } yield assert(validationresponse)(equalTo("hello")))
    }
    val z: ZSpec[Aa with Postgres, Throwable] = TestAspect.sequential(MigrationAspects.migrate(suite("zio test suite with docker postgis")(
      test1,
      test2
    )))
    val bad: Spec[TestEnvironment, Any, TestSuccess] = z.provideCustomLayerShared(suiteLayers)
    val sameButFine: ZSpec[TestEnvironment, Failure] = z.provideCustomLayerShared(suiteLayers)
    sameButFine
  }
}
