package com.backwards.app.migration

import java.util.UUID
import cats.effect.IO
import pureconfig.generic.auto._
import com.mongodb.reactivestreams.client.{MongoClient, MongoCollection, MongoDatabase}
import com.backwards.app.migration.TestMigration._
import com.backwards.config.PureConfig.config
import com.backwards.mongo.Mongo.mongoClient
import com.backwards.mongo.MongoConfig
import fs2._
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.result.InsertOneResult
import org.reactivestreams.{Subscriber, Subscription}
import com.backwards.cassandra.User
import com.backwards.mongo.bson.Encoder
import org.mongodb.scala._

object TestMigrationApp extends MigrationApp(
  seed(mongoClient(config[MongoConfig]("mongo")))
)

object TestMigration {
  def seed(mongoClient: Stream[IO, MongoClient]): Stream[IO, MongoClient] =
    mongoClient.evalTap { mongoClient =>
      IO {
        val database: MongoDatabase = mongoClient.getDatabase("mydatabase")

        val collection: MongoCollection[BsonDocument] = database.getCollection("mycollection", classOf[BsonDocument])

        val user = User(UUID.randomUUID(), "Bob", "Boo", "bob@gmail.com")

        val doc = Encoder[User].encode(user).asDocument()

        collection.insertOne(doc).subscribe(new Subscriber[InsertOneResult] {
          def onSubscribe(s: Subscription): Unit = {
            println("onSubscribe")
            s.request(1)
          }

          def onNext(t: InsertOneResult): Unit = println("onNext")

          def onError(t: Throwable): Unit = println("onError")

          def onComplete(): Unit = println("onComplete")
        })
      }
    }
}
