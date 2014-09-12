package jubu.akka.slick

import org.specs2.mutable.Specification
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import jubu.akka.slick.db.DatabaseSession
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.slick.lifted


/**
 * @author jubu
 */
class SlickPoolSpec extends Specification with Schema {

	implicit lazy val actorSystem = {
		val ret = ActorSystem("test", ConfigFactory.load().getConfig("test.slickPoolSpec"))
		ret
	}

	lazy val pool = SlickPool("slickTestConfig")

	lazy val databaseSession = pool.databaseSession

	import databaseSession.driver.simple._

	private val name = "slickTestConfig"

	implicit def asOption[T](t:T) = Option(t)

	"SlickPoolFactory" should {

		"accept ddl" in {
			val job = pool.run() {
				implicit session =>
					(users.ddl ++ persons.ddl).create
					true
			}
			val result = Await.result(job, Duration(5, SECONDS))
			result
		}

		"accept inserts" in {
			val job = pool.run() {
				implicit session =>
					session.withTransaction {
						persons.insert(Person("Janko", None, "Hrasko"))
						persons.insert(Person("Ferko", "Velky", "Mrkvicka"))
						true
					}
			}
			val result = Await.result(job, Duration(5, SECONDS))
			result
		}

		"has working selects" in {
			// test
			val job = pool.run() {
				implicit session => {
					val q = for (p <- persons if(p.id > 0L)) yield(p.id)
					val ret = q.run
					ret
				}
			}
			val result = Await.result(job, Duration(5, SECONDS))
			result == Vector(1L, 2L)
		}


/*
		"ddl - drop tables" in {
			val job = pool.run() {
				implicit session =>
					(users.ddl ++ persons.ddl).drop
					true
			}
			val result = Await.result(job, Duration(5, SECONDS))
			result
		}
*/
	}
}