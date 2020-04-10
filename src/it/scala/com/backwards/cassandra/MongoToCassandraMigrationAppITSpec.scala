package com.backwards.cassandra

import pureconfig.generic.auto._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import com.backwards.cassandra.Cassandra.cqlSession
import com.backwards.config.PureConfig.config
import com.backwards.mongo.Mongo.mongoClient
import com.backwards.mongo.{MongoConfig, MongoFixture}
import cats.implicits._

class MongoToCassandraMigrationAppITSpec extends AnyWordSpec with Matchers with MongoFixture {
  "No Mongo data" should {
    "be migrated to Cassandra" in {
      val program =
        MongoToCassandraMigrationApp.program(
          mongoClient(config[MongoConfig]("mongo")),
          cqlSession(config[CassandraConfig]("cassandra"))
        )


      println("===> AHA: " + program.compile.lastOrError.unsafeRunSync)


    }
  }

  "Mongo data" should {
    "be migrated to Cassandra" in {
      val program =
        MongoToCassandraMigrationApp.program(
          seed(mongoClient(config[MongoConfig]("mongo"))),
          cqlSession(config[CassandraConfig]("cassandra"))
        )


      println("===> AHA: " + program.compile.lastOrError.unsafeRunSync)


    }
  }
}