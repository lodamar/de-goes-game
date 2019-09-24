val Http4sVersion     = "0.21.0-M5"
val CirceVersion      = "0.12.1"
val Specs2Version     = "4.1.0"
val LogbackVersion    = "1.2.3"
val ZIOVersion        = "1.0.0-RC13"
val ZIOInteropVersion = "2.0.0.0-RC3"
val PureConfigVersion = "0.12.0"
val ScalaTestVersion  = "3.0.8"

lazy val root = (project in file("."))
  .settings(
    organization := "com.lodamar",
    name := "de-goes-game",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.1",
    libraryDependencies ++= Seq(
      "org.http4s"            %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s"            %% "http4s-circe"        % Http4sVersion,
      "org.http4s"            %% "http4s-dsl"          % Http4sVersion,
      "io.circe"              %% "circe-generic"       % CirceVersion,
      "ch.qos.logback"        % "logback-classic"      % LogbackVersion,
      "com.github.pureconfig" %% "pureconfig"          % PureConfigVersion,
      "dev.zio"               %% "zio-interop-cats"    % ZIOInteropVersion,
      "dev.zio"               %% "zio"                 % ZIOVersion,

      "org.scalatest"         %% "scalatest"           % ScalaTestVersion % Test
    ),
  )

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-Xfatal-warnings",
)
