package com.backwards.app.migration

import java.util.UUID
import scala.jdk.CollectionConverters._
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import fs2._
import fs2.interop.reactivestreams._
import pureconfig.generic.auto._
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.result.InsertOneResult
import com.datastax.oss.driver.api.core.CqlSession
import com.mongodb.reactivestreams.client.{MongoClient, MongoCollection, MongoDatabase}
import com.backwards.cassandra.Cassandra._
import com.backwards.cassandra.{CassandraConfig, User}
import com.backwards.config.PureConfig.config
import com.backwards.mongo.Mongo.mongoClient
import com.backwards.mongo.bson.Decoder.ops._
import com.backwards.mongo.bson.Encoder.ops._
import com.backwards.mongo.{MongoConfig, NoOpsSubscriber}

object MongoToCassandraMigrationApp extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    val program: Stream[IO, Unit] =
      for {
        cqlSession <- cqlSession(config[CassandraConfig]("cassandra"))
        mongoClient <- seed(mongoClient(config[MongoConfig]("mongo")))
        mongoDatabase = mongoClient.getDatabase("mydatabase")
        mongoCollection = mongoDatabase.getCollection("mycollection", classOf[BsonDocument])
        (user, index) <- mongoCollection.find().toStream[IO].map(_.as[User]).zipWithIndex
        _ <- user.fold(Stream.raiseError[IO], process(cqlSession))
      } yield
        scribe.info(s"$index: $user")

      program.compile.drain.as(ExitCode.Success)
    }

  def seed(mongoClient: Stream[IO, MongoClient]): Stream[IO, MongoClient] =
    mongoClient.evalTap { mongoClient =>
      IO {
        val database: MongoDatabase = mongoClient.getDatabase("mydatabase")

        val collection: MongoCollection[BsonDocument] = database.getCollection("mycollection", classOf[BsonDocument])

        val user = User(UUID.randomUUID(), "Bob", "Boo", "bob@gmail.com")

        collection.insertOne(user.asDocument).subscribe(NoOpsSubscriber[InsertOneResult])
      }
    }

  def process(implicit cqlSession: CqlSession): User => Stream[IO, Unit] = {
    import com.backwards.cassandra.Decoder.ops._

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