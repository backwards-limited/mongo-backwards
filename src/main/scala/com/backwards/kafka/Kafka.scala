package com.backwards.kafka

import cats.effect.{ConcurrentEffect, ContextShift, IO}
import fs2.Stream
import fs2.kafka.{KafkaProducer, ProducerSettings, producerStream}

object Kafka {
  // TODO - Key and value serialization
  def kafkaProducer[K, V](config: IO[KafkaConfig])(implicit C: ConcurrentEffect[IO], CS: ContextShift[IO]): Stream[IO, KafkaProducer[IO, String, String]] = {
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
}