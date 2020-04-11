package com.backwards.cassandra

import java.util.UUID
import scala.jdk.CollectionConverters._
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import fs2._
import pureconfig.generic.auto._
import com.backwards.cassandra.Cassandra.{cassandra, _}
import com.backwards.cassandra.Decoder.ops._
import com.backwards.config.PureConfig.config
import com.backwards.mongo._

/**
 * cqlsh localhost -u cassandra -p cassandra
 */
object CassandraExample extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    val user = User(UUID.randomUUID(), "Bob", "Boo", "email@gmail.com")

    val program = for {
      implicit0(cassandra: Cassandra) <- cassandra(config[CassandraConfig]("cassandra"))
      _ <- Stream.eval(execute(cql"insert into user_by_id (id, email, firstname, lastname) values (?, ?, ?, ?)", user.id, user.email, user.firstName, user.lastName))
      resultSet <- Stream.eval(execute(cql"select * from user_by_id where id = ?", user.id))
    } yield
      resultSet.iterator().asScala.foreach { row =>
        scribe info s"${row.as[User]}"
      }

    program.compile.drain.as(ExitCode.Success)
  }
}