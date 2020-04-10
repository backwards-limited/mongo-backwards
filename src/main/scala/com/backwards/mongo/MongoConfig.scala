package com.backwards.mongo

// TODO - WIP
final case class MongoConfig(host: Host, database: Database, collection: Collection)

final case class Host(value: String) extends AnyVal

final case class Database(value: String) extends AnyVal

final case class Collection(value: String) extends AnyVal