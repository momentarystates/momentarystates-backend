import com.killbirds.sbt.sbtrelease.gitflow.Steps._
import ReleaseTransformations._

name := """dgdg-backend"""
organization := "net.weltuebergang"
scalaVersion := "2.13.2"
maintainer := "markus@toto.io"

lazy val root = (project in file(".")).enablePlugins(PlayScala, SwaggerPlugin)

libraryDependencies += guice
libraryDependencies += ws
libraryDependencies += "com.typesafe.play"       %% "play-slick"                 % "5.0.0"
libraryDependencies += "com.typesafe.play"       %% "play-slick-evolutions"      % "5.0.0"
libraryDependencies += "org.postgresql"          % "postgresql"                  % "42.2.14"
libraryDependencies += "org.scalaz"              %% "scalaz-core"                % "7.2.30"
libraryDependencies += "com.github.tminglei"     %% "slick-pg"                   % "0.19.0"
libraryDependencies += "com.github.tminglei"     %% "slick-pg_play-json"         % "0.19.0"
libraryDependencies += "org.webjars"             % "swagger-ui"                  % "3.26.1"
libraryDependencies += "com.typesafe.play"       %% "play-mailer"                % "8.0.1"
libraryDependencies += "com.typesafe.play"       %% "play-mailer-guice"          % "8.0.1"
libraryDependencies += "com.lightbend.akka"      %% "akka-stream-alpakka-s3"     % "2.0.0"
libraryDependencies += "org.apache.tika"         % "tika-parsers"                % "1.24.1"
libraryDependencies += "org.apache.pdfbox"       % "jbig2-imageio"               % "3.0.3"
libraryDependencies += "ru.yandex.qatools.embed" % "postgresql-embedded"         % "2.10" % Test exclude ("de.flapdoodle.embed", "de.flapdoodle.embed.process")
libraryDependencies += "de.flapdoodle.embed"     % "de.flapdoodle.embed.process" % "2.1.2"
libraryDependencies += "org.scalatestplus.play"  %% "scalatestplus-play"         % "5.1.0" % Test

javaOptions in Test ++= Seq("-Dconfig.resource=test.conf")
fork in Test := true
parallelExecution in Test := false

swaggerDomainNameSpaces := Seq("controllers.api", "persistence.model", "commons")

/**
 * customize release process
 */
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies, // : ReleaseStep
  checkGitFlowExists, // : ReleaseStep
  inquireVersions, // : ReleaseStep
  runClean, // : ReleaseStep
  runTest, // : ReleaseStep
  gitFlowReleaseStart, // : ReleaseStep
  setReleaseVersion, // : ReleaseStep
  commitReleaseVersion, // : ReleaseStep, performs the initial git checks
  gitFlowReleaseFinish, // : ReleaseStep
  setNextVersion, // : ReleaseStep
  commitNextVersion, // : ReleaseStep
  pushChanges, // : ReleaseStep, also checks that an upstream branch is properly configured
  pushMaster // : ReleaseStep
)

scalacOptions in Compile ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-feature" // Emit warning and location for usages of features that should be imported explicitly.
)
