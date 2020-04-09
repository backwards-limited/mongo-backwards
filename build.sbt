import scala.sys.process._
import sbt._

lazy val IntegrationTest = config("it") extend Test

lazy val itTest = taskKey[Int]("Execute integration tests in Docker")

lazy val itInspect = taskKey[Int]("Inspect integration tests in Docker without container shutdown")

lazy val root = project("mongo-backwards", file("."))
  .settings(description := "Scala Mongo by Backwards")

def project(id: String, base: File): Project =
  Project(id, base)
    .enablePlugins(JavaAppPackaging)
    .configs(IntegrationTest)
    .settings(
      resolvers ++= Seq(
        Resolver.sonatypeRepo("releases"),
        Resolver.sonatypeRepo("snapshots"),
        Resolver.bintrayRepo("cakesolutions", "maven"),
        Resolver.bintrayRepo("dmbl", "dinogroup"),
        "Artima Maven Repository" at "http://repo.artima.com/releases",
        "jitpack" at "https://jitpack.io",
        "Confluent Platform Maven" at "http://packages.confluent.io/maven/"
      ),
      scalaVersion := BuildProperties("scala.version"),
      sbtVersion := BuildProperties("sbt.version"),
      organization := "com.backwards",
      name := id,
      autoStartServer := false,
      addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full),
      addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
      libraryDependencies ++= Dependencies(),
      Defaults.itSettings,
      itTest := "docker-compose up --abort-on-container-exit --exit-code-from it-test".#&&("docker-compose down").#||("docker-compose down").!,
      itInspect := "docker-compose up".!,
      scalacOptions ++= Seq(
        "-encoding", "utf8",
        "-deprecation",
        "-unchecked",
        "-language:implicitConversions",
        "-language:higherKinds",
        "-language:existentials",
        "-language:postfixOps",
        "-Ymacro-annotations",
        "-Xfatal-warnings"
        // "-Ywarn-value-discard"
      ),
      fork := true,
      publishArtifact in Test := true
    )