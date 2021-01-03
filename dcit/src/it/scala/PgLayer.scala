import LayersPostgis.Postgres
import com.dimafeng.testcontainers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import zio.blocking.{Blocking, effectBlocking}
import zio.test.TestAspect
import zio.test.TestAspect.before
import zio.{Has, ZIO, ZLayer, ZManaged}

final case class DoobieConfig(url: String, driver: String, user: String, password: String) {
  def show: String = s"db url $url - '$user'/${Option(password).map(_.length)}  - $driver"
}


object LayersPostgis {

  type Postgres = Has[PostgreSQLContainer]

  def configFor(pgContainer: PostgreSQLContainer): DoobieConfig =
    DoobieConfig(pgContainer.jdbcUrl, pgContainer.driverClassName, pgContainer.username, pgContainer.password)

  lazy val container = PostgreSQLContainer(
    dockerImageNameOverride =
      DockerImageName.parse(s"kartoza/postgis:11.5-2.8").asCompatibleSubstituteFor("postgres")
  )

  val postgresLayer: ZLayer[Blocking, Nothing, Postgres] =
    ZManaged.make {
      effectBlocking {
        println(s"start postgis")
        container.start()
        container
      }.orDie
    }(container => effectBlocking{
      println(s"stopit postgis")
      container.stop()
    }.orDie).toLayer
}

object MigrationAspects {
  def migrationDirect: ZIO[Postgres, Throwable, Unit] =
    for {
      pg <- ZIO.service[PostgreSQLContainer]
      _ <- ZIO.succeed(println(s"migrationA"))
      _  <- FlywayMigrator.initDb(LayersPostgis.configFor(pg))
    } yield ()
  def migration: ZIO[Postgres, Throwable, Unit] =
    for {
      pg <- ZIO.service[PostgreSQLContainer]
      _ <- ZIO.succeed(println(s"migrationB"))
      _  <- FlywayMigrator.initDb(LayersPostgis.configFor(pg))
    } yield ()

  def migrate =
    before(migration.orDie)
}

