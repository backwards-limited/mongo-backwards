package com.backwards.mongo

import cats.effect.IO
import cats.implicits._
import fs2._
import com.mongodb.reactivestreams.client.{MongoClient, MongoClients}

final case class Mongo(client: MongoClient, config: MongoConfig)

object Mongo {
  def mongoClient(config: IO[MongoConfig]): Stream[IO, MongoClient] = {
    val acquire: IO[MongoClient] = config map { c =>
      scribe info s"Acquiring Mongo client: $c"

      MongoClients.create(s"mongodb://${c.host.value}")
    }

    val release: MongoClient => IO[Unit] =
      mongoClient => IO {
        scribe info "Releasing Mongo client"
        mongoClient.close()
      }

    Stream.bracket(acquire)(release)
  }
}