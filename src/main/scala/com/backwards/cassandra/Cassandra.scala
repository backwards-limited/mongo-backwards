package com.backwards.cassandra

import java.net.InetSocketAddress
import scala.jdk.FutureConverters._
import cats.effect.{ContextShift, IO}
import cats.implicits._
import fs2._
import com.datastax.oss.driver.api.core.cql.{PreparedStatement, ResultSet, SimpleStatement}
import com.datastax.oss.driver.api.core.{CqlIdentifier, CqlSession}

object Cassandra {
  implicit class CqlStrings(val context: StringContext) extends AnyVal {
    def cql(args: Any*)(implicit cqlSession: CqlSession, cs: ContextShift[IO]): IO[PreparedStatement] = {
      val statement = SimpleStatement.newInstance(context.raw(args: _*))

      IO fromFuture IO {
        cqlSession.prepareAsync(statement).asScala
      }
    }
  }

  def cqlSession(config: IO[CassandraConfig]): Stream[IO, CqlSession] = {
    val acquire = config map { c =>
      scribe info s"Acquiring Cassandra session: $c"

      CqlSession
        .builder
        .addContactPoint(new InetSocketAddress(c.host.value, c.port.value))
        .withLocalDatacenter(c.dataCentre.value)
        .withAuthCredentials(c.credentials.userName.value, c.credentials.password.value)
        .withKeyspace(CqlIdentifier.fromCql(c.keyspace.value))
        .build
    }

    val release: CqlSession => IO[Unit] =
      cqlSession => IO {
        scribe info "Releasing Cassandra session"
        cqlSession.close()
      }

    Stream.bracket(acquire)(release)
  }

  def execute(statement: IO[PreparedStatement], params: Any*)(implicit session: CqlSession): IO[ResultSet] =
    statement
      .map(_.bind(params: _*))
      .map(session.execute)
}