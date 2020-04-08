package com.backwards.app.migration

import java.util.UUID
import cats.effect.{ContextShift, IO}
import cats.implicits._
import fs2._
import fs2.kafka._
import pureconfig.generic.auto._
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.result.InsertOneResult
import com.mongodb.reactivestreams.client.{MongoClient, MongoCollection, MongoDatabase}
import com.backwards.app.migration.MongoToKafkaMigration._
import com.backwards.cassandra.User
import com.backwards.config.PureConfig.config
import com.backwards.mongo.Mongo.mongoClient
import com.backwards.mongo.bson.Encoder.ops._
import com.backwards.mongo.{MongoConfig, NoOpsSubscriber}

object MongoToKafkaMigrationApp extends MongoMigrationApp(
  seed(mongoClient(config[MongoConfig]("mongo"))),
  kafkaProducer.map(process) // TODO - Change to something like: kafkaProducer(config[KafkaProducerConfig]("kafka")).map(process)
)

object MongoToKafkaMigration {
  def seed(mongoClient: Stream[IO, MongoClient]): Stream[IO, MongoClient] =
    mongoClient.evalTap { mongoClient =>
      IO {
        val database: MongoDatabase = mongoClient.getDatabase("mydatabase")

        val collection: MongoCollection[BsonDocument] = database.getCollection("mycollection", classOf[BsonDocument])

        val user = User(UUID.randomUUID(), "Bob", "Boo", "bob@gmail.com")

        collection.insertOne(user.asDocument).subscribe(NoOpsSubscriber[InsertOneResult])
      }
    }

  def kafkaProducer: Stream[IO, KafkaProducer[IO, String, String]] = { // TODO - Parameterise key/value as maybe UUID -> User
    // TODO - Rethink
    implicit val cs: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.global)

    // TODO - Configure
    val producerSettings =
      ProducerSettings[IO, String, String]
        .withBootstrapServers("localhost:9092")

    producerStream[IO].using(producerSettings)
  }

  def process(kafkaProducer: KafkaProducer[IO, String, String]): User => Stream[IO, Unit] =
    user => Stream.eval {
      val record = ProducerRecord("user-topic", user.id.toString, user.toString) // TODO

      kafkaProducer.produce(ProducerRecords.one(record)).flatten.map { producerResult =>
        scribe.info(producerResult.toString)
        ()
      }
    }
}