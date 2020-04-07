package com.backwards.cassandra

import java.net.InetSocketAddress
import java.util.UUID
import java.util.concurrent.{CompletionStage, TimeUnit}
import scala.concurrent.Future
import com.datastax.oss.driver.api.core.`type`.codec.TypeCodecs
import com.datastax.oss.driver.api.core.cql.{BoundStatement, PreparedStatement, ResultSet, SimpleStatement}
import com.datastax.oss.driver.api.core.session.Session
import com.datastax.oss.driver.api.core.{CqlIdentifier, CqlSession}
import com.datastax.oss.driver.internal.core.`type`.codec.UuidCodec
import com.datastax.oss.driver.internal.core.cql.DefaultBoundStatement
import com.datastax.oss.driver.shaded.guava.common.util.concurrent.ListenableFuture
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success}

object Demo extends App {
  /*val cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
  val session = cluster.connect()
  val manager = new MappingManager(session)

  val mapperClass = manager.mapper(classOf[TableObj], "test")
  val objClass = mapperClass.get(new Integer(1))
  println("Obj(1)='" + objClass + "'")*/

  import scala.jdk.FutureConverters._


  implicit class CqlStrings(val context: StringContext) extends AnyVal {
    def cql(args: Any*)(implicit session: CqlSession): Future[PreparedStatement] = {
      val statement = SimpleStatement.newInstance(context.raw(args: _*))
      session.prepareAsync(statement).asScala
    }
  }

  import scala.concurrent.{ ExecutionContext, Future, Promise }

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


  import scala.concurrent.ExecutionContext.Implicits.global

  val user = User(UUID.randomUUID(), "Bob", "Boo", "email@gmail.com")

  val resultSet: Future[ResultSet] = for {
    x <- execute(cql"insert into mykeyspace.user_by_id (id, email, firstname, lastname) values (?, ?, ?, ?)", user.id, user.email, user.firstName, user.lastName)
    y <- execute(cql"select * from mykeyspace.user_by_id where id = ?", user.id)
  } yield y



  resultSet.onComplete {
    case Failure(t) =>
      t.printStackTrace()
    case Success(resultSet) =>
      resultSet.iterator().asScala.toList.foreach { row =>
        println(row.getString("firstname"))

        println(Decoder[User].decode(row))
      }
  }


  TimeUnit.SECONDS.sleep(5)
}