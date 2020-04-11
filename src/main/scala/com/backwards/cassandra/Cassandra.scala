package com.backwards.cassandra

import java.net.InetSocketAddress
import scala.jdk.FutureConverters._
import cats.effect.{ContextShift, IO}
import cats.implicits._
import fs2._
import com.datastax.oss.driver.api.core.cql.{PreparedStatement, ResultSet, SimpleStatement}
import com.datastax.oss.driver.api.core.{CqlIdentifier, CqlSession}

final case class Cassandra private(cqlSession: CqlSession, config: CassandraConfig) {
  def keyspace(name: String = config.keyspace.value): String =
    cqlSession.getKeyspace.orElse(CqlIdentifier.fromCql(name)).asInternal()
}

object Cassandra {
  implicit class CqlStrings(val context: StringContext) extends AnyVal {
    def cql(args: Any*)(implicit cassandra: Cassandra, cs: ContextShift[IO]): IO[PreparedStatement] = {
      val statement = SimpleStatement.newInstance(context.raw(args: _*))

      IO fromFuture IO {
        cassandra.cqlSession.prepareAsync(statement).asScala
      }
    }
  }

  def cassandra(config: IO[CassandraConfig]): Stream[IO, Cassandra] = {
    val acquire: IO[Cassandra] = config map { config =>
      scribe info s"Acquiring Cassandra session: $config"

      Cassandra(cqlSession(config), config)
    }

    val release: Cassandra => IO[Unit] =
      cassandra => IO {
        scribe info "Releasing Cassandra session"
        cassandra.cqlSession.close()
      }

    Stream.bracket(acquire)(release)
  }

  def cqlSession(config: CassandraConfig): CqlSession =
    CqlSession
      .builder
      .addContactPoint(new InetSocketAddress(config.host.value, config.port.value))
      .withLocalDatacenter(config.dataCentre.value)
      .withAuthCredentials(config.credentials.userName.value, config.credentials.password.value)
      .withKeyspace(CqlIdentifier.fromCql(config.keyspace.value))
      .build

  def execute(statement: IO[PreparedStatement], params: Any*)(implicit cassandra: Cassandra): IO[ResultSet] =
    statement
      .map(_.bind(params: _*))
      .map(cassandra.cqlSession.execute)
}