import zio._

object Main extends App {

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    ZIO.effect(println("Hello, World!")).exitCode

}