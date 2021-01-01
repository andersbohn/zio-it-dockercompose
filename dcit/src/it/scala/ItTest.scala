import zio._
import zio.blocking.Blocking
import zio.test.Assertion._
import zio.test._


object ItTest extends DefaultRunnableSpec {

  type TestEnv = zio.test.environment.TestEnvironment

  override def spec: ZSpec[TestEnv, Throwable] =
    suite("zio test suite")(
      testM("testM hello") {
        (for {
          validationresponse <- Task.succeed("hello")
        } yield assert(validationresponse)(equalTo("hello")))
      }
    )
}
