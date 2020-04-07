package com.backwards.runner

import scala.reflect.ClassTag
import cats.effect.IO
import pureconfig.ConfigSource
import com.mongodb.reactivestreams.client.{MongoClient, MongoClients}
import com.backwards.config.PureConfig
import com.backwards.mongo.MongoConfig
import com.backwards.mongo.Mongo.mongoClient
import com.backwards.config.PureConfig.config
import pureconfig.generic.auto._
import Foo._

object Blah extends MongoToCassandraRunner(before(mongoClient(config[MongoConfig]("mongo"))))

object Foo {
  def before(mongoClient: IO[MongoClient]): IO[MongoClient] =
    mongoClient.map { mongoClient =>
      println(s"===> Before mongo")
      mongoClient
    }
}

