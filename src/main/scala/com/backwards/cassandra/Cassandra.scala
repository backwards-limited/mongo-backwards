package com.backwards.cassandra

import scala.jdk.FutureConverters._
import cats.effect.{ContextShift, IO}
import cats.implicits._
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.{PreparedStatement, ResultSet, SimpleStatement}

object Cassandra {
  implicit class CqlStrings(val context: StringContext) extends AnyVal {
    def cql(args: Any*)(implicit session: CqlSession, cs: ContextShift[IO]): IO[PreparedStatement] = {
      val statement = SimpleStatement.newInstance(context.raw(args: _*))

      IO fromFuture IO {
        session.prepareAsync(statement).asScala
      }
    }
  }

  def execute(statement: IO[PreparedStatement], params: Any*)(implicit session: CqlSession): IO[ResultSet] =
    statement
      .map(_.bind(params: _*))
      .map(session.execute)
}