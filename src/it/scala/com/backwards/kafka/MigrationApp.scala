package com.backwards.kafka

import java.util.UUID
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import io.circe.generic.auto._
import fs2._
import fs2.kafka._
import pureconfig.generic.auto._
import com.backwards.config.PureConfig.config
import com.backwards.kafka.Kafka.kafkaProducer
import com.backwards.kafka.circe.Serializer._
import com.backwards.mongo.Mongo._
import com.backwards.mongo._

/**
 * Check Kafka i.e. have events been published (brew install kafkacat):
 *
 * kafkacat -L -b localhost:9092
 *
 * kafkacat -C -b localhost:9092 -t users -o beginning
 */
object MigrationApp extends IOApp with MongoFixture {
  def run(args: List[String]): IO[ExitCode] =
    program(
      seedUsers(init(mongo(config[MongoConfig]("mongo")))),
      kafkaProducer[UUID, User](config[KafkaConfig]("kafka"))
    ).compile.drain.as(ExitCode.Success)

  def program(mongo: Stream[IO, Mongo], kafkaProducer: Stream[IO, KafkaProducer[IO, UUID, User]]): Stream[IO, ProducerResult[UUID, User, Unit]] =
    for {
      implicit0(kafkaProducer: KafkaProducer[IO, UUID, User]) <- kafkaProducer
      implicit0(mongo: Mongo) <- mongo
      user <- users.evalMap(_.fold(IO.raiseError, process))
    } yield user

  def process(user: User)(implicit kafkaProducer: KafkaProducer[IO, UUID, User]): IO[ProducerResult[UUID, User, Unit]] = {
    val record = ProducerRecord("users", user.id, user)

    kafkaProducer.produce(ProducerRecords.one(record)).flatten.map { producerResult =>
      scribe.info(producerResult.toString)
      producerResult
    }
  }
}