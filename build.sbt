import com.typesafe.sbt.packager.docker.{Cmd, ExecCmd}
import sbtrelease.ReleaseStateTransformations._

name := """registration-service"""
organization := "com.devsprint"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.6"

libraryDependencies += guice
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test
libraryDependencies += "com.github.alexarchambault" %% "scalacheck-shapeless_1.14" % "1.2.0" % Test
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
libraryDependencies += "com.googlecode.libphonenumber" % "libphonenumber" % "4.3"
libraryDependencies += "org.typelevel" %% "cats-core" % "1.4.0"


defaultLinuxInstallLocation in Docker := "/opt/microservice"
dockerExposedPorts in Docker := Seq(9000)
dockerCommands := Seq(
  Cmd("FROM", "anapsix/alpine-java:8_server-jre"),
  Cmd("ADD", "opt /opt"),
  Cmd("WORKDIR", "/opt/microservice"),
  Cmd("ENV", "JAVA_LIBRARY_PATH /opt/microservice/native"),
  ExecCmd("CMD", "/opt/microservice/bin/registration-service")
)
version in Docker := version.value

scalafmtOnCompile := true


releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies, // : ReleaseStep
  inquireVersions, // : ReleaseStep
  runClean,
  runTest, // : ReleaseStep
  setReleaseVersion, // : ReleaseStep
  commitReleaseVersion, // : ReleaseStep, performs the initial git checks
  tagRelease, // : ReleaseStep
  //publishArtifacts, // : ReleaseStep, checks whether `publishTo` is properly set up
  ReleaseStep(releaseStepTask(publishLocal in Docker)),
  setNextVersion, // : ReleaseStep
  commitNextVersion, // : ReleaseStep
  pushChanges // : ReleaseStep, also checks that an upstream branch is properly configured
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.devsprint.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.devsprint.binders._"
