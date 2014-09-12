package jubu.akka.slick.db

import akka.actor.ActorSystem

/**
 *
 * @author jubu
 */
trait DatabaseSession {

	import scala.language.implicitConversions
	import scala.slick.driver.JdbcDriver

	type Session = profile.backend.Session
	type Driver = JdbcDriver

	val driver: Driver
	val session: Session
	protected lazy val profile: driver.profile.type = driver.profile

}


object DatabaseSession {

	import scala.slick.driver._
	import java.util.Properties

	case class Parameters(val driverName: String, val url: String,   val user: String, val password: String)

	private def driverByName: String => Option[JdbcDriver] = Map(
		"org.apache.derby.jdbc.EmbeddedDriver" -> DerbyDriver,
		"org.h2.Driver" -> H2Driver,
		"org.hsqldb.jdbcDriver" -> HsqldbDriver,
		"com.mysql.jdbc.Driver" -> MySQLDriver,
		"org.postgresql.Driver" -> PostgresDriver,
		"org.sqlite.JDBC" -> SQLiteDriver).get(_)

	def apply(driverName:String, url:String, user:String, password:String):DatabaseSession = apply (Parameters(driverName, url, user, password))

	def apply(p:Parameters, driverMapping:String => Option[JdbcDriver] = driverByName,  properties: Properties = null): DatabaseSession = {
		new DatabaseSession {

			val driver = driverMapping(p.driverName).getOrElse {
				throw new RuntimeException(s"Slick error : Unknown jdbc driver found in application.conf: [${p.driverName}]")
			}

			override lazy val session = {
				database.createSession()
			}

			protected lazy val database = {
				val database = profile.backend.Database
				database.forURL(p.url, p.user, p.password, properties, p.driverName)
			}
		}
	}
}