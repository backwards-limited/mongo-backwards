package com.backwards.mongo

import cats.effect.IO
import cats.implicits._
import fs2._
import com.mongodb.reactivestreams.client.{MongoClient, MongoClients}

object Mongo {
  def mongoClient(config: IO[MongoConfig]): Stream[IO, MongoClient] = {
    val acquire: IO[MongoClient] = config map { c =>
      scribe info s"Acquiring Mongo client: $c"

      // TODO - WIP
      MongoClients.create(s"mongodb://${c.host}")
    }

    val release: MongoClient => IO[Unit] =
      mongoClient => IO {
        scribe info "Releasing Mongo client"
        mongoClient.close()
      }

    Stream.bracket(acquire)(release)
  }
}