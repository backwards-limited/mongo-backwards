package com.backwards.mongo

import cats.effect.IO
import com.mongodb.reactivestreams.client.{MongoClient, MongoClients}

object Mongo {
  def mongoClient(config: IO[MongoConfig]): IO[MongoClient] =
    IO(MongoClients.create("mongodb://localhost"))
}