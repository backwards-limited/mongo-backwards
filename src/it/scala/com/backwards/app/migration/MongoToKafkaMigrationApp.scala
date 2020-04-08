package com.backwards.app.migration

import java.util.UUID
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import fs2._
import fs2.interop.reactivestreams._
import fs2.kafka._
import pureconfig.generic.auto._
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.result.InsertOneResult
import com.mongodb.reactivestreams.client.{MongoClient, MongoCollection, MongoDatabase}
import com.backwards.cassandra.User
import com.backwards.config.PureConfig.config
import com.backwards.mongo.Mongo.mongoClient
import com.backwards.mongo.bson.Decoder.ops._
import com.backwards.mongo.bson.Encoder.ops._
import com.backwards.mongo.{MongoConfig, NoOpsSubscriber}

/**
 * Check Kafka i.e. have events been published:
 *
 * kafkacat -L -b localhost:9092
 *
 * kafkacat -C -b localhost:9092 -t users -o beginning
 */
object MongoToKafkaMigrationApp extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    val program: Stream[IO, Unit] =
      for {
        kafkaProducer <- kafkaProducer
        mongoClient <- seed(mongoClient(config[MongoConfig]("mongo")))
        mongoDatabase = mongoClient.getDatabase("mydatabase")
        mongoCollection = mongoDatabase.getCollection("mycollection", classOf[BsonDocument])
        (user, index) <- mongoCollection.find().toStream[IO].map(_.as[User]).zipWithIndex
        _ <- user.fold(Stream.raiseError[IO], process(kafkaProducer))
      } yield
        scribe.info(s"$index: $user")

    program.compile.drain.as(ExitCode.Success)
  }

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
    // TODO - Configure
    val producerSettings =
      ProducerSettings[IO, String, String]
        .withBootstrapServers("localhost:9092")

    producerStream[IO]
      .using(producerSettings)
      .evalTap(_ => IO(scribe info "Acquiring Kafka producer"))
      .onComplete {
        scribe info "Releasing Kafka producer"
        Stream.empty
      }
  }

  def process(kafkaProducer: KafkaProducer[IO, String, String]): User => Stream[IO, Unit] =
    user => Stream.eval {
      val record = ProducerRecord("users", user.id.toString, user.toString) // TODO

      kafkaProducer.produce(ProducerRecords.one(record)).flatten.map { producerResult =>
        scribe.info(producerResult.toString)
        ()
      }
    }
}