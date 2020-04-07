package com.backwards.app.migration

import cats.effect.{ExitCode, IO, IOApp}
import org.mongodb.scala.bson.BsonDocument
import com.mongodb.reactivestreams.client.MongoClient
import fs2._
import fs2.interop.reactivestreams._
import cats.implicits._

class MigrationApp(mongoClient: Stream[IO, MongoClient]) extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    val program: Stream[IO, Unit] = for {
      mongoClient <- mongoClient
      mongoDatabase = mongoClient.getDatabase("mydatabase")
      mongoCollection = mongoDatabase.getCollection("mycollection", classOf[BsonDocument])
      (document, index) <- mongoCollection.find().toStream[IO].zipWithIndex
    } yield
      scribe.info(s"$index: ${document.toJson}")

    program.compile.drain.as(ExitCode.Success)
  }
}
