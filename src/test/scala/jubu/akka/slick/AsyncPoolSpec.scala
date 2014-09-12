package jubu.akka.slick

import org.specs2.mutable.Specification
import akka.actor.{ActorSystem, OneForOneStrategy, Props, ActorRef}
import akka.routing.{FromConfig, RoundRobinPool}
import akka.actor.SupervisorStrategy.Restart
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

/**
 * @author jubu
 */
class AsyncPoolSpec extends Specification {


	private implicit val actorSystem = ActorSystem("test", ConfigFactory.load().getConfig("test.asyncPoolSpec"))

	private val name = "slickTestConfig"

	private def sessionFactory() =  new Function0[Int] {
		override def apply(): Int = {
			val cfg = actorSystem.settings.config.getConfig(s"akka.actor.deployment./${name}.session")
			val ret = cfg.getInt("value")
			ret
		}
	}

/*
	private val supervisor: OneForOneStrategy = OneForOneStrategy(3, Duration(2, SECONDS)) {
		case _: Throwable => Restart
	}


	private val router: ActorRef =
		actorSystem.actorOf(
			Props(classOf[Worker[Int]], sessionFactory).withRouter(
				RoundRobinPool(nrOfInstances = 100, supervisorStrategy = supervisor)), name)
*/

	private lazy val router = actorSystem.actorOf(FromConfig.props(Props(classOf[Worker[Int]], sessionFactory)), name)

	private lazy val pool = AsyncPool[Int](router, Timeout(2, SECONDS))

	"AsyncPoolSpec" should {

		"return result" in {
			val job: Future[Int] = pool { arg => arg + 1	}
			val result: Int = Await.result(job, Duration(1, SECONDS))
			result == 11
		}

		"throws exception" in {
			val job: Future[Int] = pool {
				arg => throw new RuntimeException("Error occured")
			}
			Await.result(job, Duration(1, SECONDS)) must throwA[Exception]
		}
	}
}
