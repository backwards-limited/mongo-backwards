package com.backwards.cassandra

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import fs2._
import pureconfig.generic.auto._
import com.backwards.cassandra.Cassandra.{cassandra, _}
import com.backwards.cassandra.Decoder.ops._
import com.backwards.config.PureConfig.config
import com.backwards.mongo.Mongo.mongo
import com.backwards.mongo._

object MongoToCassandraMigrationApp extends IOApp with MongoFixture {
  def run(args: List[String]): IO[ExitCode] =
    program(
      seedUsers(init(mongo(config[MongoConfig]("mongo")))),
      cassandra(config[CassandraConfig]("cassandra"))
    ).compile.drain.as(ExitCode.Success)

  def program(mongo: Stream[IO, Mongo], cassandra: Stream[IO, Cassandra]): Stream[IO, Throwable Either User] =
    cassandra flatMap { cassandra =>
      MongoMigration.run(mongo, process(cassandra))
    }

  def process(implicit cassandra: Cassandra): User => Stream[IO, Throwable Either User] =
    user => Stream.eval {
      for {
        _ <- execute(cql"insert into user_by_id (id, email, firstname, lastname) values (?, ?, ?, ?)", user.id, user.email, user.firstName, user.lastName)
        resultSet <- execute(cql"select * from user_by_id where id = ?", user.id)
      } yield {
        val user = resultSet.one.as[User]
        scribe info s"In Cassandra: $user"
        user
      }
    }
}