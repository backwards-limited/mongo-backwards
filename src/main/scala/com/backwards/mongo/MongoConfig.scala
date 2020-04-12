package com.backwards.mongo

// TODO - WIP
final case class MongoConfig(host: Host, database: Database)

final case class Host(value: String) extends AnyVal

final case class Database(value: String) extends AnyVal