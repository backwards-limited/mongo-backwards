package com.backwards.cassandra

import scala.jdk.FutureConverters._
import cats.effect.{ContextShift, IO}
import cats.implicits._
import fs2._
import com.datastax.oss.driver.api.core.cql.{PreparedStatement, ResultSet, SimpleStatement}
import com.datastax.oss.driver.api.core.{CqlIdentifier, CqlSession}

object Cassandra {
  implicit class CqlStrings(val context: StringContext) extends AnyVal {
    def cql(args: Any*)(implicit session: CqlSession, cs: ContextShift[IO]): IO[PreparedStatement] = {
      val statement = SimpleStatement.newInstance(context.raw(args: _*))

      IO fromFuture IO {
        session.prepareAsync(statement).asScala
      }
    }
  }

  def cassandraSession(config: IO[CassandraConfig]): Stream[IO, CqlSession] = {
    val acquire = IO {
      scribe info "Acquiring Cassandra session"

      // TODO - Configuration
      CqlSession
        .builder
        //.addContactPoint(new InetSocketAddress("localhost", 9042))
        //.withLocalDatacenter("DC1")
        .withAuthCredentials("cassandra", "cassandra")
        .withKeyspace(CqlIdentifier.fromCql("mykeyspace"))
        //.addTypeCodecs(TypeCodecs.UUID)
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