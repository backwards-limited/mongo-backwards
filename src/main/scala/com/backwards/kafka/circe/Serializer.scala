package com.backwards.kafka.circe

import fs2.kafka.KafkaSerializer
import io.circe.Encoder

object Serializer {
  implicit def circeKafkaSerializer[A: Encoder]: KafkaSerializer[A] =
    (_: String, data: A) => Encoder[A].apply(data).noSpaces.getBytes
}