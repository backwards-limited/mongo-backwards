package com.backwards.kafka

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import fs2._
import fs2.kafka._
import pureconfig.generic.auto._
import com.backwards.config.PureConfig.config
import com.backwards.kafka.Kafka.kafkaProducer
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
    program.compile.drain.as(ExitCode.Success)

  def program: Stream[IO, ProducerResult[String, String, Unit]] =
    kafkaProducer(config[KafkaConfig]("kafka")) flatMap { kafkaProducer =>
      MongoMigration.run(
        seed(mongoClient(config[MongoConfig]("mongo"))),
        process(kafkaProducer)
      )
    }

  def process(kafkaProducer: KafkaProducer[IO, String, String]): User => Stream[IO, ProducerResult[String, String, Unit]] =
    user => Stream.eval {
      // TODO - Serialization
      val record = ProducerRecord("users", user.id.toString, user.toString)

      kafkaProducer.produce(ProducerRecords.one(record)).flatten.map { producerResult =>
        scribe.info(producerResult.toString)
        producerResult
      }
    }
}