name := """dgdg-backend"""
organization := "net.weltuebergang"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, SwaggerPlugin)

scalaVersion := "2.13.2"

libraryDependencies += guice
libraryDependencies += ws
libraryDependencies += "com.typesafe.play"      %% "play-slick"            % "5.0.0"
libraryDependencies += "com.typesafe.play"      %% "play-slick-evolutions" % "5.0.0"
libraryDependencies += "org.postgresql"         % "postgresql"             % "42.2.12"
libraryDependencies += "org.scalaz"             %% "scalaz-core"           % "7.2.30"
libraryDependencies += "com.github.tminglei"    %% "slick-pg"              % "0.19.0"
libraryDependencies += "com.github.tminglei"    %% "slick-pg_play-json"    % "0.19.0"
libraryDependencies += "org.webjars"            % "swagger-ui"             % "2.2.0"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play"    % "5.0.0" % Test
