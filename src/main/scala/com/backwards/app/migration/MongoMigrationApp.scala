package com.backwards.app.migration

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import fs2._
import fs2.interop.reactivestreams._
import org.mongodb.scala.bson.BsonDocument
import com.mongodb.reactivestreams.client.MongoClient
import com.backwards.cassandra.User
import com.backwards.mongo.bson.Decoder.ops._

class MongoMigrationApp(
  mongoClient: Stream[IO, MongoClient],
  callback: Stream[IO, User => Stream[IO, Unit]] // TODO - Rethink this
) extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    val program: Stream[IO, Unit] = for {
      callback <- callback
      mongoClient <- mongoClient
      mongoDatabase = mongoClient.getDatabase("mydatabase")
      mongoCollection = mongoDatabase.getCollection("mycollection", classOf[BsonDocument])
      (user, index) <- mongoCollection.find().toStream[IO].map(_.as[User]).zipWithIndex
      _ <- user.fold(Stream.raiseError[IO], callback)
    } yield
      scribe.info(s"$index: $user")

    program.compile.drain.as(ExitCode.Success)
  }
}
