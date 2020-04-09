package com.backwards.mongo

import org.scalacheck.Gen

trait UserFixture {
  lazy val genUser: Gen[User] = for {
    uuid <- Gen.uuid
    firstName <- Gen.oneOf("Bob", "Sue", "Bill", "Ed", "Sam", "Beth", "Ann")
    lastName <- Gen.oneOf("MacDonald", "Burns", "Craig", "Bruce", "Ferguson", "Sinclair")
    email = s"$firstName@$lastName.com"
  } yield User(uuid, firstName, lastName, email)
}