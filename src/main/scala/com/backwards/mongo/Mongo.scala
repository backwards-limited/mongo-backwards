package com.backwards.mongo

import cats.effect.IO
import cats.implicits._
import fs2._
import com.mongodb.reactivestreams.client.{MongoClient, MongoClients, MongoCollection, MongoDatabase}

final case class Mongo private(client: MongoClient, config: MongoConfig) {
  def database(name: String = config.database.value): MongoDatabase =
    client.getDatabase(name)

  def collection[T](name: String, documentType: Class[T]): MongoCollection[T] =
    database().getCollection(name, documentType)
}

object Mongo {
  def mongo(config: IO[MongoConfig]): Stream[IO, Mongo] = {
    val acquire: IO[Mongo] = config map { config =>
      scribe info s"Acquiring Mongo client: $config"

      Mongo(
        MongoClients create s"mongodb://${config.host.value}",
        config
      )
    }

    val release: Mongo => IO[Unit] =
      mongo => IO {
        scribe info "Releasing Mongo client"
        mongo.client.close()
      }

    Stream.bracket(acquire)(release)
  }
}