package com.backwards.mongo

import java.util.UUID
import cats.effect.IO
import cats.implicits._
import fs2.Stream
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.result.InsertOneResult
import com.mongodb.reactivestreams.client.{MongoClient, MongoCollection, MongoDatabase}
import com.backwards.mongo.bson.Encoder.ops._

object MongoFixture extends MongoFixture

trait MongoFixture {
  def seed(mongoClient: Stream[IO, MongoClient]): Stream[IO, MongoClient] =
    mongoClient evalTap { mongoClient =>
      IO {
        val database: MongoDatabase = mongoClient.getDatabase("mydatabase")

        val collection: MongoCollection[BsonDocument] = database.getCollection("mycollection", classOf[BsonDocument])

        val user = User(UUID.randomUUID(), "Bob", "Boo", "bob@gmail.com")

        collection.insertOne(user.asDocument).subscribe(NoOpsSubscriber[InsertOneResult])
      }
    }
}