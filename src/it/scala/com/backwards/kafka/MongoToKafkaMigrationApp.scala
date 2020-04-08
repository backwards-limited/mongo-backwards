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
 * Check Kafka i.e. have events been published:
 *
 * kafkacat -L -b localhost:9092
 *
 * kafkacat -C -b localhost:9092 -t users -o beginning
 */
object MongoToKafkaMigrationApp extends IOApp with MongoFixture {
  def run(args: List[String]): IO[ExitCode] = {
    val program: Stream[IO, Unit] =
      kafkaProducer(config[KafkaConfig]("kafka")) flatMap { kafkaProducer =>
        MongoMigration.run(
          seed(mongoClient(config[MongoConfig]("mongo"))),
          process(kafkaProducer)
        )
      }

    program.compile.drain.as(ExitCode.Success)
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