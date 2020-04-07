package com.backwards.cassandra

import java.util.UUID
import java.util.concurrent.TimeUnit
import scala.concurrent.Future
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success}
import com.datastax.oss.driver.api.core.cql.{PreparedStatement, ResultSet, SimpleStatement}
import com.datastax.oss.driver.api.core.{CqlIdentifier, CqlSession}
import com.backwards.cassandra.Decoder.ops._

/**
 * cqlsh localhost -u cassandra -p cassandra
 */
object Demo extends App {
  import scala.jdk.FutureConverters._ // TODO

  implicit class CqlStrings(val context: StringContext) extends AnyVal {
    def cql(args: Any*)(implicit session: CqlSession): Future[PreparedStatement] = {
      val statement = SimpleStatement.newInstance(context.raw(args: _*))
      session.prepareAsync(statement).asScala
    }
  }

  import scala.concurrent.{ExecutionContext, Future} // TODO

  // TODO IO from Future
  def execute(statement: Future[PreparedStatement], params: Any*)(
    implicit executionContext: ExecutionContext, session: CqlSession
  ): Future[ResultSet] =
    statement
      .map(_.bind(params: _*))
      .map(session.execute)

  implicit val session: CqlSession =
    CqlSession
      .builder
      //.addContactPoint(new InetSocketAddress("localhost", 9042))
      //.withLocalDatacenter("DC1")
      .withAuthCredentials("cassandra", "cassandra")
      .withKeyspace(CqlIdentifier.fromCql("mykeyspace"))
      //.addTypeCodecs(TypeCodecs.UUID)
      .build

  import scala.concurrent.ExecutionContext.Implicits.global // TODO

  val user = User(UUID.randomUUID(), "Bob", "Boo", "email@gmail.com")

  val resultSet: Future[ResultSet] = for {
    x <- execute(cql"insert into mykeyspace.user_by_id (id, email, firstname, lastname) values (?, ?, ?, ?)", user.id, user.email, user.firstName, user.lastName)
    y <- execute(cql"select * from mykeyspace.user_by_id where id = ?", user.id)
  } yield y

  // TODO
  resultSet.onComplete {
    case Failure(t) =>
      t.printStackTrace()

    case Success(resultSet) =>
      resultSet.iterator().asScala.toList.foreach { row =>
        println(row.as[User])
      }
  }

  // TODO
  TimeUnit.SECONDS.sleep(5)
}