package jubu.akka.slick

import jubu.akka.slick.db.DatabaseSession
import akka.actor.{ActorRef, Props, ActorSystem}

import akka.routing.FromConfig
import java.util.concurrent.TimeUnit._
import akka.util.Timeout
import scala.util.Try


/**
 * @author jubu
 */
case class SlickPool(name: String)(implicit actorSystem: ActorSystem) {

	private lazy val router: ActorRef =	actorSystem.actorOf(
		FromConfig.props(Props(classOf[Worker[DatabaseSession#Session]],(()=> {databaseSession.session}))), name)


	/*

		private val supervisor: OneForOneStrategy = OneForOneStrategy(-1, Duration.Inf, true) {
			case _: Throwable => Restart
		}

		private val router: ActorRef =
			actorSystem.actorOf(
				Props(classOf[Worker[DatabaseSession#Session]], sessionFactory).withRouter(
					RoundRobinPool(nrOfInstances = 100, supervisorStrategy = supervisor)), "slickDatabaseRouter")
	*/

	def run(implicit timeout:Timeout = Timeout(60, SECONDS)): AsyncPool[DatabaseSession#Session] = {
		AsyncPool(router, timeout)
	}

	lazy val databaseSession: DatabaseSession = {
		val cfg = actorSystem.settings.config.getConfig(s"akka.actor.deployment./${name}.session")
		val p = DatabaseSession.Parameters(
			cfg.getString("driver-name"),
			cfg.getString("url"),
			cfg.getString("user"), cfg.getString("password"))
		val mp = Try(cfg.getAnyRef("mapping"))
		mp.getOrElse()
		val ret= DatabaseSession(p)
		ret
	}
}

object ConfiguredSlickPool {

}