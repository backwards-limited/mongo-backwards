package com.backwards.cassandra

import scala.jdk.CollectionConverters._
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import fs2._
import pureconfig.generic.auto._
import com.datastax.oss.driver.api.core.CqlSession
import com.mongodb.reactivestreams.client.MongoClient
import com.backwards.cassandra.Cassandra.{cqlSession, _}
import com.backwards.cassandra.Decoder.ops._
import com.backwards.config.PureConfig.config
import com.backwards.mongo.Mongo.mongoClient
import com.backwards.mongo.{MongoConfig, MongoFixture, MongoMigration, User}

object MongoToCassandraMigrationApp extends IOApp with MongoFixture {
  def run(args: List[String]): IO[ExitCode] =
    program(
      seed(mongoClient(config[MongoConfig]("mongo"))),
      cqlSession(config[CassandraConfig]("cassandra"))
    ).compile.drain.as(ExitCode.Success)

  def program(mongoClient: Stream[IO, MongoClient], cqlSession: Stream[IO, CqlSession]): Stream[IO, Iterator[Throwable Either User]] =
    cqlSession flatMap { cqlSession =>
      MongoMigration.run(mongoClient, process(cqlSession))
    }

  def process(implicit cqlSession: CqlSession): User => Stream[IO, Iterator[Throwable Either User]] =
    user => Stream.eval {
      for {
        _ <- execute(cql"insert into mykeyspace.user_by_id (id, email, firstname, lastname) values (?, ?, ?, ?)", user.id, user.email, user.firstName, user.lastName)
        resultSet <- execute(cql"select * from mykeyspace.user_by_id where id = ?", user.id)
      } yield
        resultSet.iterator().asScala.map { row =>
          val user = row.as[User]
          scribe info s"In Cassandra: $user"
          user
        }
    }
}