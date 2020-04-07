package com.backwards.cassandra

import java.util.UUID
import scala.jdk.CollectionConverters._
import cats.effect.{ExitCode, IO, IOApp}
import com.datastax.oss.driver.api.core.{CqlIdentifier, CqlSession}
import com.backwards.cassandra.Cassandra._
import com.backwards.cassandra.Decoder.ops._

/**
 * cqlsh localhost -u cassandra -p cassandra
 */
object Demo extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    implicit val session: CqlSession =
      CqlSession
        .builder
        //.addContactPoint(new InetSocketAddress("localhost", 9042))
        //.withLocalDatacenter("DC1")
        .withAuthCredentials("cassandra", "cassandra")
        .withKeyspace(CqlIdentifier.fromCql("mykeyspace"))
        //.addTypeCodecs(TypeCodecs.UUID)
        .build

    val user = User(UUID.randomUUID(), "Bob", "Boo", "email@gmail.com")

    for {
      _ <- execute(cql"insert into mykeyspace.user_by_id (id, email, firstname, lastname) values (?, ?, ?, ?)", user.id, user.email, user.firstName, user.lastName)
      resultSet <- execute(cql"select * from mykeyspace.user_by_id where id = ?", user.id)
    } yield {
      resultSet.iterator().asScala.foreach { row =>
        scribe.info(s"${row.as[User]}")
      }

      ExitCode.Success
    }
  }
}