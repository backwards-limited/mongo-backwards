package com.backwards.kafka

import cats.effect.{ConcurrentEffect, ContextShift, IO}
import fs2.Stream
import fs2.kafka.{KafkaProducer, ProducerSettings, RecordSerializer, producerStream}

object Kafka {
  def kafkaProducer[K, V](
    config: IO[KafkaConfig]
  )(implicit K: RecordSerializer[IO, K], V: RecordSerializer[IO, V], C: ConcurrentEffect[IO], CS: ContextShift[IO]): Stream[IO, KafkaProducer[IO, K, V]] = {
    // TODO - Configure
    val producerSettings =
      ProducerSettings[IO, K, V]
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