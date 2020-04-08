import sbt._

object Dependencies {
  def apply(): Seq[ModuleID] = Seq(
    scalatest, scalacheck, scalacheckShapeless,
    pureConfig, scribe,
    cats, mouse, simulacrum, refined, monocle, shapeless,
    fs2, fs2Kafka, mongo, datastax
  ).flatten
  
  lazy val scalatest: Seq[ModuleID] = Seq(
    "org.scalatest" %% "scalatest" % "3.1.0" % "test,it" withSources() withJavadoc()
  )

  lazy val scalacheck: Seq[ModuleID] = Seq(
    "org.scalacheck" %% "scalacheck" % "1.14.3" % "test,it" withSources() withJavadoc()
  )

  lazy val scalacheckShapeless: Seq[ModuleID] = Seq(
    "com.github.alexarchambault" %% "scalacheck-shapeless_1.14" % "1.2.5" % "test,it" withSources() withJavadoc()
  )
  
  lazy val pureConfig: Seq[ModuleID] = {
    val group = "com.github.pureconfig"
    val version = "0.12.3"

    Seq(
      "pureconfig-core", "pureconfig-generic", "pureconfig-cats", "pureconfig-cats-effect", "pureconfig-fs2"
    ).map(group %% _ % version withSources() withJavadoc())
  }

  lazy val scribe: Seq[ModuleID] = Seq(
    "com.outr" %% "scribe" % "2.7.12" withSources() withJavadoc()
  )

  lazy val cats: Seq[ModuleID] = {
    val group = "org.typelevel"
    val version = "2.1.1"

    Seq(
      "cats-core", "cats-effect"
    ).map(group %% _ % version withSources() withJavadoc()) ++ Seq(
      "cats-laws", "cats-testkit"
    ).map(group %% _ % version % Test withSources() withJavadoc())
  }

  lazy val mouse: Seq[ModuleID] = Seq(
    "org.typelevel" %% "mouse" % "0.24" withSources() withJavadoc()
  )

  lazy val simulacrum: Seq[ModuleID] = Seq(
    "org.typelevel" %% "simulacrum" % "1.0.0" withSources() withJavadoc()
  )
  
  lazy val refined: Seq[ModuleID] = {
    val group = "eu.timepit"
    val version = "0.9.13"

    Seq(
      "refined", "refined-pureconfig", "refined-cats"
    ).map(group %% _ % version withSources() withJavadoc())
  }

  lazy val monocle: Seq[ModuleID] = {
    val group = "com.github.julien-truffaut"
    val version = "2.0.4"

    Seq(
      "monocle-core", "monocle-macro", "monocle-generic"
    ).map(group %% _ % version withSources() withJavadoc()) ++ Seq(
      "monocle-law"
    ).map(group %% _ % version % Test withSources() withJavadoc())
  }

  lazy val shapeless: Seq[ModuleID] = Seq(
    "com.chuusai" %% "shapeless" % "2.3.3" force() withSources() withJavadoc()
  )

  lazy val fs2: Seq[ModuleID] = {
    val group = "co.fs2"
    val version = "2.2.2"

    Seq(
      "fs2-core", "fs2-io", "fs2-reactive-streams"
    ).map(group %% _ % version withSources() withJavadoc())
  }

  lazy val fs2Kafka: Seq[ModuleID] = Seq(
    "com.github.fd4s" %% "fs2-kafka" % "1.0.0"
  )

  lazy val mongo: Seq[ModuleID] = Seq(
    "org.mongodb.scala" %% "mongo-scala-driver" % "4.0.1" withSources() withJavadoc()
  )

  lazy val datastax: Seq[ModuleID] = {
    val group = "com.datastax.oss"
    val version = "4.5.1"

    Seq(
      "java-driver-core", "java-driver-query-builder", "java-driver-mapper-processor", "java-driver-mapper-runtime"
    ).map(group % _ % version withSources() withJavadoc())
  }
}