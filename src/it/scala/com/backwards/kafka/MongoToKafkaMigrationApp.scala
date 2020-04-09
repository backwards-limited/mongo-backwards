package com.backwards.kafka

import java.util.UUID
import cats.Applicative
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import io.circe.Encoder
import io.circe.generic.auto._
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



  implicit def circeKafkaSerializer[A: Encoder]: KafkaSerializer[A] =
    (topic: String, data: A) => Encoder[A].apply(data).noSpaces.getBytes

  implicit def serializer[A: KafkaSerializer]: Serializer[IO, A] = Serializer.delegate[IO, A](implicitly[KafkaSerializer[A]])


  implicit def recordSerializer[A](
                                    implicit serializer: Serializer[IO, A]
                                  ): RecordSerializer[IO, A] = RecordSerializer.lift[IO, A]

  def program: Stream[IO, ProducerResult[UUID, User, Unit]] =
    kafkaProducer[UUID, User](config[KafkaConfig]("kafka")) flatMap { kafkaProducer =>
      MongoMigration.run(
        seed(mongoClient(config[MongoConfig]("mongo"))),
        process(kafkaProducer)
      )
    }

  def process(kafkaProducer: KafkaProducer[IO, UUID, User]): User => Stream[IO, ProducerResult[UUID, User, Unit]] =
    user => Stream.eval {
      // TODO - Serialization
      val record = ProducerRecord("users", user.id, user)

      kafkaProducer.produce(ProducerRecords.one(record)).flatten.map { producerResult =>
        scribe.info(producerResult.toString)
        producerResult
      }
    }
}