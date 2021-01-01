
val ZioVersion = "1.0.3"
val ZioCatsVersion = "2.2.0.1"

scalaVersion := "2.13.3"

name := "hello-world"
organization := "ch.epfl.scala"
version := "1.0"

libraryDependencies += "dev.zio" %% "zio" % ZioVersion

val itTestLibraries = Seq(
  "dev.zio" %% "zio" % ZioVersion,
  "dev.zio" %% "zio" % ZioVersion % "it",
  "dev.zio" %% "zio-test" % ZioVersion % "it",
  "dev.zio" %% "zio-test-sbt" % ZioVersion % "it"
)

lazy val dcit = project
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .settings(libraryDependencies ++= itTestLibraries)
  .settings(testFrameworks := List(new TestFramework("zio.test.sbt.ZTestFramework")))
