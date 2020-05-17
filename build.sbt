name := """dgdg-backend"""
organization := "net.weltuebergang"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, SwaggerPlugin)

scalaVersion := "2.13.2"

libraryDependencies += guice
libraryDependencies += ws
libraryDependencies += "com.typesafe.play"       %% "play-slick"                 % "5.0.0"
libraryDependencies += "com.typesafe.play"       %% "play-slick-evolutions"      % "5.0.0"
libraryDependencies += "org.postgresql"          % "postgresql"                  % "42.2.12"
libraryDependencies += "org.scalaz"              %% "scalaz-core"                % "7.2.30"
libraryDependencies += "com.github.tminglei"     %% "slick-pg"                   % "0.19.0"
libraryDependencies += "com.github.tminglei"     %% "slick-pg_play-json"         % "0.19.0"
libraryDependencies += "org.webjars"             % "swagger-ui"                  % "2.2.0"
libraryDependencies += "com.typesafe.play"       %% "play-mailer"                % "8.0.0"
libraryDependencies += "com.typesafe.play"       %% "play-mailer-guice"          % "8.0.0"
libraryDependencies += "ru.yandex.qatools.embed" % "postgresql-embedded"         % "2.9" % Test exclude ("de.flapdoodle.embed", "de.flapdoodle.embed.process")
libraryDependencies += "de.flapdoodle.embed"     % "de.flapdoodle.embed.process" % "2.0.5"
libraryDependencies += "org.scalatestplus.play"  %% "scalatestplus-play"         % "5.0.0" % Test

javaOptions in Test ++= Seq("-Dconfig.resource=test.conf")
fork in Test := true
parallelExecution in Test := false

scalacOptions in Compile ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-Ywarn-unused:imports" // Warn if an import selector is not referenced.
)
