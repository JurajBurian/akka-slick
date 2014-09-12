package jubu.akka.slick

import jubu.akka.slick.db.DatabaseProfile

/**
 * @author jubu
 */
trait Schema extends DatabaseProfile {

	this: DatabaseProfile =>  //<- step 1: you must add this "self-type"
	import databaseSession.driver.simple._  //<- step 2: then import the correct Table, ... from the profile


	case class Person(givenName: String, middleName: Option[String], familyName: String, id: Long = 0)

	class Persons(tag: Tag) extends Table[Person](tag, "persons") {

		def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
		def givenName = column[String]("given_name", O.NotNull)
		def middleName = column[Option[String]]("middle_name")
		def familyName = column[String]("family_name", O.NotNull)

		def * = (givenName, middleName, familyName, id) <> (Person.tupled, Person.unapply _)

	}

	case class User(loginName: String, password: String, person: Long = 0, version:Int = 0)

	class Users(tag: Tag) extends Table[User](tag, "users") {

		def person = column[Long]("person_fk", O.PrimaryKey)
		def loginName = column[String]("login_name", O.NotNull /*, O.DBType("VARCHAR(300)")*/ )
		def password = column[String]("password", O.NotNull)
		def version = column[Int]("version", O.NotNull)

		def idx = index("person_idx", person, unique = true)

		def pfk = foreignKey("user_person_fk", person, persons)(_.id, onDelete = ForeignKeyAction.Cascade)

		def * = (loginName, password, person, version) <> (User.tupled, User.unapply _)

		def forUpdate = (password, person, version)
	}

	case class Department(name: String, id: Long = 0)

	class Departments(tag: Tag) extends Table[Department](tag, "departments") {

		def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
		def name = column[String]("NAME", O.NotNull)

		def * = (name, id) <> (Department.tupled, Department.unapply _)
	}

	val persons = TableQuery[Persons]
	val users = TableQuery[Users]
}
