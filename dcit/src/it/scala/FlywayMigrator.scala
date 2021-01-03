import org.flywaydb.core.Flyway
import org.flywaydb.core.api.MigrationVersion
import zio.{Has, RIO, Task, ZIO}

object FlywayMigrator {

  /**
   * The prod version will NOT run on a non-empty schema where no migration table is present.
   * For testcontainers, we simply run in 'postgres', which is not empty, so we need to claim that is version '0',
   * then flyway will start by executing from V1 (default would be to start with V2..)
   */
  def initDb(cfg: DoobieConfig): Task[Unit] =
    ZIO.effect {
/*
      val fw =
        Flyway
          .configure()
          .dataSource(cfg.url, cfg.user, cfg.password)
          .baselineVersion(MigrationVersion.fromVersion("0"))
          .baselineOnMigrate(true)
          .load()
      fw.migrate()
*/
    }.unit

  def initDbAll: RIO[Has[DoobieConfig], Unit] = ZIO.access(x => initDb(x.get))

}
