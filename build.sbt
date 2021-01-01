
val ZioVersion = "1.0.3"
val ZioCatsVersion = "2.2.0.1"
val FlywayVersion              = "5.2.4"
val TestContainersScalaVersion = "0.38.8"

scalaVersion := "2.13.3"

name := "hello-world"
organization := "ch.epfl.scala"
version := "1.0"

libraryDependencies += "dev.zio" %% "zio" % ZioVersion

val itTestLibraries = Seq(
  "dev.zio" %% "zio" % ZioVersion,
  "dev.zio" %% "zio" % ZioVersion % "it",
  "dev.zio" %% "zio-test" % ZioVersion % "it",
  "dev.zio" %% "zio-test-sbt" % ZioVersion % "it",
  "com.dimafeng"  %% "testcontainers-scala"            % TestContainersScalaVersion % "it",
  "com.dimafeng"  %% "testcontainers-scala-postgresql" % TestContainersScalaVersion % "it",
  "org.flywaydb"       % "flyway-core"          % FlywayVersion,
  "com.google.guava" % "guava-io" % "r03"

)

lazy val dcit = project
  .configs(IntegrationTest)
  .settings(scalaVersion := "2.13.3")
  .settings(Defaults.itSettings)
  .settings(libraryDependencies ++= itTestLibraries)
  .settings(testFrameworks := List(new TestFramework("zio.test.sbt.ZTestFramework")))
