package com.backwards.kafka

import cats.effect.{ConcurrentEffect, ContextShift, IO}
import fs2.Stream
import fs2.kafka._

object Kafka {
  implicit def serializer[A: KafkaSerializer]: Serializer[IO, A] =
    Serializer.delegate[IO, A](implicitly[KafkaSerializer[A]])

  def kafkaProducer[K, V](
    config: IO[KafkaConfig]
  )(implicit K: KafkaSerializer[K], V: KafkaSerializer[V], C: ConcurrentEffect[IO], CS: ContextShift[IO]): Stream[IO, KafkaProducer[IO, K, V]] = {
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