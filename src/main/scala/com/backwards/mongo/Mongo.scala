package com.backwards.mongo

import cats.effect.IO
import cats.implicits._
import fs2._
import com.mongodb.reactivestreams.client.{MongoClient, MongoClients}

object Mongo {
  def mongoClient(config: IO[MongoConfig]): Stream[IO, MongoClient] = {
    val acquire = IO {
      scribe info "Acquiring mongo client"
      MongoClients.create("mongodb://localhost")
    }

    val release: MongoClient => IO[Unit] =
      mongoClient => IO {
        scribe info "Releasing mongo client"
        mongoClient.close()
      }

    Stream.bracket(acquire)(release)
  }
}

/* TODO

val settings: MongoClientSettings = MongoClientSettings
  .builder()
  .applyToClusterSettings(b => b.hosts(List(new ServerAddress("localhost")).asJava))
  .build()

*/