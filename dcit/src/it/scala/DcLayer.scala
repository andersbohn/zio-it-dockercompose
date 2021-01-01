import zio.blocking.{Blocking, effectBlocking}
import zio.test.TestAspect.before
import zio.{Has, Task, ZIO, ZLayer, ZManaged}
import com.google.common.io.Resources
import scala.concurrent.duration._
import com.dimafeng.testcontainers.DockerComposeContainer.ComposeFile
import com.dimafeng.testcontainers.{DockerComposeContainer, ExposedService, MultipleContainers, PostgreSQLContainer}

import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import zio.blocking.{Blocking, effectBlocking}
import zio.test.TestAspect.before
import zio.{Has, Task, ZIO, ZLayer, ZManaged}

import java.io.File
import scala.concurrent.duration._
import scala.jdk.DurationConverters._

case class ClickhouseHttpConfig(chHost: String, chPort: Int, chUser: String, chPassword: Option[String])
case class ChAndPgContainer(
                             multiContainer: MultipleContainers,
                             pgContainer: PostgreSQLContainer,
                             chContainer: DockerComposeContainer,
                             chConfigLayer: ClickhouseHttpConfig
                           )

object LayersDockerComposeCh {

  type BothChAndPgContainer = Has[ChAndPgContainer]

  lazy val postgresAndChLayer: ZLayer[Blocking, Nothing, BothChAndPgContainer] =
    ZManaged.make {
      val blocked = effectBlocking {
        val resourcesDcFolder: File = {
          val r = Resources.getResource(getClass, "/chcluster/docker-compose.yml")
          new File(r.getFile)
        }

        val pgContainer = PostgreSQLContainer(
          dockerImageNameOverride =
            DockerImageName.parse(s"kartoza/postgis:11.5-2.8").asCompatibleSubstituteFor("postgres")
        )

        val dcRandomName = DockerComposeContainer.randomIdentifier

        val serviceName = s"clickhouse-01"
        val chPort = 8123
        lazy val chContainer: DockerComposeContainer =
          DockerComposeContainer(
            ComposeFile(Left(resourcesDcFolder)),
            Seq(ExposedService(serviceName, chPort, 1, Wait.forListeningPort().withStartupTimeout(30.seconds.toJava))),
            dcRandomName
          )

        val chHostname: String = chContainer.getServiceHost(serviceName, chPort)
        val chConfig = ClickhouseHttpConfig(chHostname, chPort, "default", None)

        val multiContainer: MultipleContainers = MultipleContainers(pgContainer, chContainer)
        multiContainer.start()
        // NOTE or log on tester?  DockerLoggerFactory.getLogger(pgContainer.container.getDockerImageName).info(s"⚡ ${pgContainer.jdbcUrl}")
        ChAndPgContainer(multiContainer, pgContainer, chContainer, chConfig)
      }
      blocked.tapCause { cause =>
        Task.succeed(println(s"blocked.tapCause ${cause.prettyPrint}"))
      }.tapErrorTrace { err =>
        Task.succeed(println(s"blocked.tapErrorTrace ${err._1} ->\n${err._2.map(_.prettyPrint)}"))
      }.orDie
    }(chAndPgContainer => effectBlocking(chAndPgContainer.multiContainer.stop()).orDie).toLayer
}

object ChDockerLayersAspects {

  def migration: ZIO[Has[ChAndPgContainer], Throwable, Unit] =
    for {
      pg <- ZIO.service[ChAndPgContainer]
      _  <- FlywayMigrator.initDb(LayersPostgis.configFor(pg.pgContainer))
    } yield ()

  def migrate =
    before(migration.orDie)
}
