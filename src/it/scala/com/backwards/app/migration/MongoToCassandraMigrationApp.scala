package com.backwards.app.migration

import java.util.UUID
import scala.jdk.CollectionConverters._
import cats.effect.{ContextShift, IO}
import fs2._
import pureconfig.generic.auto._
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.result.InsertOneResult
import com.datastax.oss.driver.api.core.CqlSession
import com.mongodb.reactivestreams.client.{MongoClient, MongoCollection, MongoDatabase}
import com.backwards.app.migration.MongoToCassandraMigration._
import com.backwards.cassandra.Cassandra._
import com.backwards.cassandra.Decoder.ops._
import com.backwards.cassandra.{CassandraConfig, User}
import com.backwards.config.PureConfig.config
import com.backwards.mongo.Mongo.mongoClient
import com.backwards.mongo.bson.Encoder.ops._
import com.backwards.mongo.{MongoConfig, NoOpsSubscriber}

object MongoToCassandraMigrationApp extends MongoMigrationApp(
  seed(mongoClient(config[MongoConfig]("mongo"))),
  cassandraSession(config[CassandraConfig]("cassandra")).map(process)
)

object MongoToCassandraMigration {
  def seed(mongoClient: Stream[IO, MongoClient]): Stream[IO, MongoClient] =
    mongoClient.evalTap { mongoClient =>
      IO {
        val database: MongoDatabase = mongoClient.getDatabase("mydatabase")

        val collection: MongoCollection[BsonDocument] = database.getCollection("mycollection", classOf[BsonDocument])

        val user = User(UUID.randomUUID(), "Bob", "Boo", "bob@gmail.com")

        collection.insertOne(user.asDocument).subscribe(NoOpsSubscriber[InsertOneResult])
      }
    }

  def process(cqlSession: CqlSession): User => Stream[IO, Unit] = {
    // TODO - Rethink
    implicit val cs: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.global)
    implicit val csql: CqlSession = cqlSession

    user => Stream.eval {
      for {
        _ <- execute(cql"insert into mykeyspace.user_by_id (id, email, firstname, lastname) values (?, ?, ?, ?)", user.id, user.email, user.firstName, user.lastName)
        resultSet <- execute(cql"select * from mykeyspace.user_by_id where id = ?", user.id)
      } yield
        resultSet.iterator().asScala.foreach { row =>
          scribe.info(s"In Cassandra: ${row.as[User]}")
        }
    }
  }
}