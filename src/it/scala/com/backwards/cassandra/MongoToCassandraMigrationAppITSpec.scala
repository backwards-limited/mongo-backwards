package com.backwards.cassandra

import cats.implicits._
import pureconfig.generic.auto._
import org.scalatest.Inspectors
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import com.backwards.cassandra.Cassandra.cassandra
import com.backwards.config.PureConfig.config
import com.backwards.io.IOFixture
import com.backwards.mongo.Mongo.mongo
import com.backwards.mongo.{MongoConfig, MongoFixture}

class MongoToCassandraMigrationAppITSpec extends AnyWordSpec with Matchers with Inspectors with IOFixture with MongoFixture {
  // TODO - Uncomment - First need code to "init" Cassandra to "truncate" all collections within keyspace
  /*"No Mongo data" should {
    "be migrated to Cassandra" in {
      val program =
        MongoToCassandraMigrationApp.program(
          init(mongo(config[MongoConfig]("mongo"))),
          cassandra(config[CassandraConfig]("cassandra"))
        )

      program.compile.toList.unsafeRunSync mustBe Nil
    }
  }*/

  "Mongo data" should {
    "be migrated to Cassandra" in {
      val program =
        MongoToCassandraMigrationApp.program(
          seedUsers(init(mongo(config[MongoConfig]("mongo")))),
          cassandra(config[CassandraConfig]("cassandra"))
        )

      program.compile.toList.unsafeRunSync match {
        case users @ List(_, _ @ _*) => forAll(users)(_ mustBe a[Right[_, _]])
      }
    }
  }
}