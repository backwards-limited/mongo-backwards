package com.backwards.mongo

import cats.effect.{ConcurrentEffect, IO}
import cats.implicits._
import fs2._
import fs2.interop.reactivestreams._
import org.mongodb.scala.bson.BsonDocument
import com.mongodb.reactivestreams.client.MongoClient
import com.backwards.mongo.bson.Decoder.ops._

object MongoMigration {
  def run(mongoClient: Stream[IO, MongoClient], callback: User => Stream[IO, Unit])(implicit C: ConcurrentEffect[IO]): Stream[IO, Unit] =
    for {
      mongoClient <- mongoClient
      mongoDatabase = mongoClient.getDatabase("mydatabase")
      mongoCollection = mongoDatabase.getCollection("mycollection", classOf[BsonDocument])
      (user, index) <- mongoCollection.find().toStream[IO].map(_.as[User]).zipWithIndex
      _ <- user.fold(Stream.raiseError[IO], callback)
    } yield
      scribe.info(s"$index: $user")
}