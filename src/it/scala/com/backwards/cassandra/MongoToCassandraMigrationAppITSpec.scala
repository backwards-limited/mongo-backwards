package com.backwards.cassandra

import pureconfig.generic.auto._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import com.backwards.cassandra.Cassandra.cqlSession
import com.backwards.config.PureConfig.config
import com.backwards.mongo.Mongo.mongoClient
import com.backwards.mongo.MongoConfig

class MongoToCassandraMigrationAppITSpec extends AnyWordSpec with Matchers {
  "Mongo data" should {
    "be migrated to Cassandra" in {
      val program =
        MongoToCassandraMigrationApp.program(
          mongoClient(config[MongoConfig]("mongo")),
          cqlSession(config[CassandraConfig]("cassandra"))
        )


      println("===> AHA: " + program.compile.last.unsafeRunSync)


    }
  }
}