package com.backwards.app.migration

import scala.jdk.CollectionConverters._
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import fs2._
import pureconfig.generic.auto._
import com.datastax.oss.driver.api.core.CqlSession
import com.backwards.cassandra.Cassandra._
import com.backwards.cassandra.Decoder.ops._
import com.backwards.cassandra.{CassandraConfig, User}
import com.backwards.config.PureConfig.config
import com.backwards.mongo.Mongo.mongoClient
import com.backwards.mongo.{MongoConfig, MongoFixture}

object MongoToCassandraMigrationApp extends IOApp with MongoFixture {
  def run(args: List[String]): IO[ExitCode] = {
    val program: Stream[IO, Unit] =
      cqlSession(config[CassandraConfig]("cassandra")).flatMap { cqlSession =>
        MongoMigration.run(
          seed(mongoClient(config[MongoConfig]("mongo"))),
          process(cqlSession)
        )
      }

    program.compile.drain.as(ExitCode.Success)
  }

  def process(implicit cqlSession: CqlSession): User => Stream[IO, Unit] =
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