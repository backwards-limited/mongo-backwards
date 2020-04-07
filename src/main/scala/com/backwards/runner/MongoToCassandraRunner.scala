package com.backwards.runner

import cats.effect.{ExitCode, IO, IOApp}
import org.mongodb.scala._
import scala.jdk.CollectionConverters._
import pureconfig.ConfigSource
import pureconfig.module.fs2._
import org.mongodb.scala.bson.BsonDocument
import com.mongodb.reactivestreams.client.{MongoClient, MongoClients, MongoCollection, MongoDatabase}
import com.backwards.config.PureConfig
import com.backwards.mongo.MongoConfig
//import pureconfig.module.catseffect.syntax._
import fs2._
import fs2.interop.reactivestreams._

/*
object MongoToCassandraRunner extends IOApp with PureConfig {
  def run(args: List[String]): IO[ExitCode] = {

    val program: Stream[IO, ExitCode] = for {
      mongoConfig <- Stream.eval(IO(ConfigSource.default.at("mongo").loadOrThrow[MongoConfig]))
      mongoClient = MongoClients.create("mongodb://localhost")
      mongoDatabase = mongoClient.getDatabase("test_db")
      mongoCollection = mongoDatabase.getCollection("test_collection", classOf[BsonDocument])
      //document <- mongoCollection.find().toStream[IO]
    } yield {
      //println(s"===> $document")
      ExitCode(0)
    }

    /*val settings: MongoClientSettings = MongoClientSettings.builder()
      .applyToClusterSettings(b => b.hosts(List(new ServerAddress("localhost")).asJava))
      .build()*/


    program.compile.lastOrError
  }
}*/

class MongoToCassandraRunner(mongoClient: IO[MongoClient]) extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {

    val program: Stream[IO, ExitCode] = for {
      mongoClient <- Stream.eval(mongoClient)
      mongoDatabase = mongoClient.getDatabase("test_db")
      mongoCollection = mongoDatabase.getCollection("test_collection", classOf[BsonDocument])
      //document <- mongoCollection.find().toStream[IO]
    } yield {
      //println(s"===> $document")
      ExitCode(0)
    }

    /*val settings: MongoClientSettings = MongoClientSettings.builder()
      .applyToClusterSettings(b => b.hosts(List(new ServerAddress("localhost")).asJava))
      .build()*/


    program.compile.lastOrError
  }
}
