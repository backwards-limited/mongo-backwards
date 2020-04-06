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

object Demo extends App {
  /*lazy val cluster: Cluster = {
    val builder = Cluster.builder()
    for (cp <- cassandraHosts) builder.addContactPoint(cp)
    builder.withPort(cassandraPort)
    builder.withPoolingOptions(poolingOptions)

    builder.build()
  }

  lazy implicit val session: Session = cluster.connect()
  */


  /*val cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
  val session = cluster.connect()
  val manager = new MappingManager(session)

  val mapperClass = manager.mapper(classOf[TableObj], "test")
  val objClass = mapperClass.get(new Integer(1))
  println("Obj(1)='" + objClass + "'")*/

  import scala.jdk.FutureConverters._

  // val javaFuture = java.util.concurrent.CompletableFuture.completedFuture(12)
 // val scalaFuture = javaFuture.asScala

  implicit class CqlStrings(val context: StringContext) extends AnyVal {
    def cql(args: Any*)(implicit session: CqlSession): Future[PreparedStatement] = {
      val statement = SimpleStatement.newInstance(context.raw(args: _*))
      session.prepareAsync(statement).asScala
    }
  }

  import scala.concurrent.{ ExecutionContext, Future, Promise }


  def execute(statement: Future[PreparedStatement], params: Any*)(
    implicit executionContext: ExecutionContext, session: CqlSession
  ): Future[ResultSet] =
    statement
      //.map(_.bind(params.map(_.asInstanceOf[Object])))
      .map { x =>
        println(x)
        val y = x.bind(params: _*)
        println(y)
        y
      }
      .map { blah =>
        println(blah)
        session.execute(blah)
      }

  implicit val session: CqlSession =
    CqlSession
      .builder
      //.addContactPoint(new InetSocketAddress("localhost", 9042))
      //.withLocalDatacenter("DC1")
      .withAuthCredentials("cassandra", "cassandra")
      .withKeyspace(CqlIdentifier.fromCql("mykeyspace"))
      //.addTypeCodecs(TypeCodecs.UUID)
      .build

  val myKey = UUID.randomUUID()

  import scala.concurrent.ExecutionContext.Implicits.global

  /*import com.datastax.oss.driver.api.core.co
  import com.datastax.oss.driver.api.core.type.codec*/

  val resultSet: Future[ResultSet] = execute(
    cql"SELECT * FROM mykeyspace.user_by_id WHERE id = ?",
    myKey
  )


  resultSet.onComplete { rs =>
    println(s"===> RS = $rs")
    println(rs.get.getColumnDefinitions.iterator().asScala.toList.mkString(", "))
  }


  TimeUnit.SECONDS.sleep(5)


  /*
  ResultSet rs = session.execute("select release_version from system.local");              // (2)
  Row row = rs.one();
  System.out.println(row.getString("release_version"));
   */
}