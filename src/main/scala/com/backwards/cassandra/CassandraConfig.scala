package com.backwards.cassandra

final case class CassandraConfig(host: Host, port: Port, dataCentre: DataCentre, keyspace: Keyspace, credentials: Credentials)

final case class Host(value: String) extends AnyVal

final case class Port(value: Int) extends AnyVal

final case class DataCentre(value: String) extends AnyVal

final case class Keyspace(value: String) extends AnyVal

final case class Credentials(userName: UserName, password: Password)

final case class UserName(value: String) extends AnyVal

final case class Password(value: String) extends AnyVal