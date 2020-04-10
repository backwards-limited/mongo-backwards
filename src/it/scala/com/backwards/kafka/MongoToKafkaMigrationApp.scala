package com.backwards.kafka

import java.util.UUID
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import io.circe.generic.auto._
import fs2._
import fs2.kafka._
import pureconfig.generic.auto._
import com.mongodb.reactivestreams.client.MongoClient
import com.backwards.config.PureConfig.config
import com.backwards.kafka.Kafka.kafkaProducer
import com.backwards.kafka.circe.Serializer._
import com.backwards.mongo.Mongo.mongoClient
import com.backwards.mongo.{MongoConfig, MongoFixture, MongoMigration, User}

/**
 * Check Kafka i.e. have events been published (brew install kafkacat):
 *
 * kafkacat -L -b localhost:9092
 *
 * kafkacat -C -b localhost:9092 -t users -o beginning
 */
object MongoToKafkaMigrationApp extends IOApp with MongoFixture {
  def run(args: List[String]): IO[ExitCode] =
    program(
      seed(mongoClient(config[MongoConfig]("mongo"))),
      kafkaProducer[UUID, User](config[KafkaConfig]("kafka"))
    ).compile.drain.as(ExitCode.Success)

  def program(mongoClient: Stream[IO, MongoClient], kafkaProducer: Stream[IO, KafkaProducer[IO, UUID, User]]): Stream[IO, ProducerResult[UUID, User, Unit]] =
    kafkaProducer flatMap { kafka =>
      MongoMigration.run(mongoClient, process(kafka))
    }

  def process(kafkaProducer: KafkaProducer[IO, UUID, User]): User => Stream[IO, ProducerResult[UUID, User, Unit]] =
    user => Stream.eval {
      val record = ProducerRecord("users", user.id, user)

      kafkaProducer.produce(ProducerRecords.one(record)).flatten.map { producerResult =>
        scribe.info(producerResult.toString)
        producerResult
      }
    }
}