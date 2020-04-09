package com.backwards.mongo

import cats.effect.{ConcurrentEffect, IO}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import fs2._
import org.mongodb.scala.bson.BsonDocument
import org.scalamock.scalatest.MockFactory
import com.mongodb.reactivestreams.client.{MongoClient, MongoCollection, MongoDatabase}
import com.backwards.io.IOFixture

// TODO - Remove in favour of IT testing against Mongo
class MongoMigrationSpec extends AnyWordSpec with Matchers with MockFactory with IOFixture {
  "Mongo migration" should {
    "run with no data to process" in {
      /*
      val mongoClient = Stream.eval(IO {
        val mongoClient = mock[MongoClient]
        val mongoDatabase = mock[MongoDatabase]
        val mongoCollection = mock[MongoCollection[BsonDocument]]

        (mongoClient.getDatabase _).expects(*).returns(mongoDatabase).once()

        (mongoDatabase.getCollection(_: String, _: Class[BsonDocument])).expects(*, *).returns(mongoCollection).once()

        mongoClient
      })

      val callback: User => Stream[IO, Unit] =
        user => Stream.empty

      val program: Stream[IO, Unit] = MongoMigration.run(mongoClient, callback)

      program.compile.lastOrError.unsafeRunSync
      */
    }
  }
}