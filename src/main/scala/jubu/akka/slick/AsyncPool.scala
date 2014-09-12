package jubu.akka.slick

import scala.concurrent.{Promise, Future}
import akka.pattern.ask
import akka.actor.{Actor, ActorRef}
import akka.actor.Status.{Failure, Success}
import scala.util.Try
import akka.util.Timeout
import scala.concurrent.duration._
import scala.reflect.ClassTag


/**
 *
 * @author jubu
 */

case class AsyncPool[T](router: ActorRef, implicit val timeout:Timeout) {

	import scala.concurrent.ExecutionContext.Implicits.global

	def apply[U: Manifest](f: (T) => U): Future[U] = {

		ask(router, Task(f)).map {
			case th:Throwable => throw th
			case ret => ret.asInstanceOf[U]
		}
	}
}


case class Worker[T](factory: Function0[T]) extends Actor {

	// create it only one time
	private val v = factory()

	def receive = {
		case t: Task[T, _] => try {	sender ! t(v)	} catch {	case e:Throwable => sender ! e	}
	}
}


private case class Task[T, U](val f: T => U) extends Serializable {
	def apply(t: T): U = {
		f(t)
	}
}

/*
case class AsyncPool[T](router: ActorRef, implicit val timeout:Timeout) {

	import scala.concurrent.ExecutionContext.Implicits.global

	def apply[U: Manifest](f: (T) => U): Future[U] = {

		val p = Promise[U]
		router!Task(f, p)
		p.future
	}
}

case class Worker[T](factory: Function0[T]) extends Actor {
	private val v = factory()

	def receive = {
		case t: Task[T, _] => t(v)
	}
}

private case class Task[T, U](val f: T => U, val p:Promise[U]) extends Serializable {
	def apply(t: T) = {
		try {
			p.success(f(t))
		} catch {
			case th:Throwable => p.failure(th)
		}
	}
}
*/
