package com.backwards.mongo

import cats.effect.{ConcurrentEffect, IO}
import cats.implicits._
import fs2._
import fs2.interop.reactivestreams._
import org.mongodb.scala.bson.BsonDocument
import com.backwards.mongo.bson.Decoder.ops._

object MongoMigration {
  def run[O](mongo: Stream[IO, Mongo], callback: User => Stream[IO, O])(implicit C: ConcurrentEffect[IO]): Stream[IO, O] =
    for {
      mongo <- mongo
      mongoCollection = mongo.collection("users", classOf[BsonDocument])
      (user, index) <- mongoCollection.find().toStream[IO].map(_.as[User]).zipWithIndex
      outcome <- user.fold(Stream.raiseError[IO], process(callback))
    } yield {
      scribe info s"$index: $user"
      outcome
    }

  def process[O](callback: User => Stream[IO, O]): User => Stream[IO, O] =
    callback // TODO - For the time being, simply pass on "user" to "callback"
}