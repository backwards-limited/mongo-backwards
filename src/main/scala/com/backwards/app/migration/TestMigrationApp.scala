package com.backwards.app.migration

import java.util.UUID
import scala.jdk.CollectionConverters._
import cats.effect.{ContextShift, IO}
import fs2._
import pureconfig.generic.auto._
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.result.InsertOneResult
import org.reactivestreams.{Subscriber, Subscription}
import com.datastax.oss.driver.api.core.CqlSession
import com.mongodb.reactivestreams.client.{MongoClient, MongoCollection, MongoDatabase}
import com.backwards.app.migration.TestMigration._
import com.backwards.cassandra.Cassandra._
import com.backwards.cassandra.Decoder.ops._
import com.backwards.cassandra.{CassandraConfig, User}
import com.backwards.config.PureConfig.config
import com.backwards.mongo.Mongo.mongoClient
import com.backwards.mongo.MongoConfig
import com.backwards.mongo.bson.Encoder

object TestMigrationApp extends MigrationApp(
  seed(mongoClient(config[MongoConfig]("mongo"))),
  cassandraSession(config[CassandraConfig]("cassandra")),
  process
)

object TestMigration {
  def seed(mongoClient: Stream[IO, MongoClient]): Stream[IO, MongoClient] =
    mongoClient.evalTap { mongoClient =>
      IO {
        val database: MongoDatabase = mongoClient.getDatabase("mydatabase")

        val collection: MongoCollection[BsonDocument] = database.getCollection("mycollection", classOf[BsonDocument])

        val user = User(UUID.randomUUID(), "Bob", "Boo", "bob@gmail.com")

        val doc = Encoder[User].encode(user).asDocument()

        collection.insertOne(doc).subscribe(new Subscriber[InsertOneResult] {
          def onSubscribe(s: Subscription): Unit = {
            scribe.info()
            s.request(1)
          }

          def onNext(t: InsertOneResult): Unit = scribe.info()

          def onError(t: Throwable): Unit = scribe.info()

          def onComplete(): Unit = scribe.info()
        })
      }
    }

  def process(cs: ContextShift[IO])(cqlSession: CqlSession)(user: User): Stream[IO, Unit] = Stream.eval {
    // TODO - Rethink
    implicit val c = cs
    implicit val csql = cqlSession

    for {
      _ <- execute(cql"insert into mykeyspace.user_by_id (id, email, firstname, lastname) values (?, ?, ?, ?)", user.id, user.email, user.firstName, user.lastName)
      resultSet <- execute(cql"select * from mykeyspace.user_by_id where id = ?", user.id)
    } yield {
      resultSet.iterator().asScala.foreach { row =>
        scribe.info(s"In Cassandra: ${row.as[User]}")
      }
    }
  }
}