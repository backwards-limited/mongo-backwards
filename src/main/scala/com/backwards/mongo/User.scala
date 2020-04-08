package com.backwards.mongo

import java.util.UUID

final case class User(id: UUID, firstName: String, lastName: String, email: String) // TODO - Value classes or tags