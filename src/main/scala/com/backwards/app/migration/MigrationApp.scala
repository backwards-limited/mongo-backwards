package com.backwards.app.migration

import cats.effect.{ContextShift, ExitCode, IO, IOApp}
import org.mongodb.scala.bson.BsonDocument
import com.mongodb.reactivestreams.client.MongoClient
import fs2._
import fs2.interop.reactivestreams._
import cats.implicits._
import com.backwards.mongo.bson.Decoder.ops._
import com.backwards.cassandra.User

class MigrationApp[O](
  mongoClient: Stream[IO, MongoClient],
  outcome: Stream[IO, O],
  callback: ContextShift[IO] => O => User => Stream[IO, Unit] // TODO - Rethink this
) extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    val program: Stream[IO, Unit] = for {
      outcome <- outcome
      mongoClient <- mongoClient
      mongoDatabase = mongoClient.getDatabase("mydatabase")
      mongoCollection = mongoDatabase.getCollection("mycollection", classOf[BsonDocument])
      (user, index) <- mongoCollection.find().toStream[IO].map(_.as[User]).zipWithIndex
      _ <- user.fold(Stream.raiseError[IO], callback(contextShift)(outcome))
    } yield
      scribe.info(s"$index: $user")

    program.compile.drain.as(ExitCode.Success)
  }
}
