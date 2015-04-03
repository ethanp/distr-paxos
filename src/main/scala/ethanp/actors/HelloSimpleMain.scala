package ethanp.actors

import akka.actor.{ActorRef, ActorLogging, Props, Actor}

/**
 * Ethan Petuchowski
 * 4/3/15
 *
 * If I end up using this for my Paxos
 *
 * First of all
 *
 *   * By default, do they run as Threads?
 *
 *   * Is there a way I can make them run as Processes?
 *
 *   * How do ensure that regardless of where they are running,
 *     they way they communicate is over `TCP`?
 *
 * Some things to look into
 *
 *   * class DeadlineFailureDetector:
 *      - Implementation of failure detector using an absolute timeout
 *        of missing heartbeats to trigger unavailability.
 *
 *   * trait Pool extends RouterConfig
 *      - RouterConfig for router actor that creates "routees" as child
 *        actors and removes them from the router if they terminate.
 *
 *   * case class Broadcast(message: Any) extends RouterEnvelope with Product with Serializable
 *      - Used to broadcast a message to all "routees" in a router;
 *        only the contained message will be forwarded,
 *        i.e. the Broadcast(...) envelope will be stripped off.
 *
 */

class MasterActor extends Actor {

  /**
   * Called when an Actor is started.
   * Actors are automatically started asynchronously when created.
   * Empty default implementation.
   */
  override def preStart(): Unit = {
    // create the greeter actor (actors are automatically started asynchronously when created)

    // TODO perhaps this should be a "pool" of ClientActors?
    val greeter: ActorRef = context.actorOf(Props[ClientActor], "client")

    // Send it the 'Greet' message
    greeter ! GreeterMessages.Greet
  }

  def receive = {
    // When we receive the 'Done' message, stop this actor
    // (which if this is still the initialActor will trigger the deathwatch and stop the entire ActorSystem)
    case GreeterMessages.Done => {
      context.stop(self)
    }
  }
}
object HelloSimpleMain {

  def main(args: Array[String]): Unit = {
    val initialActor: String = classOf[MasterActor].getName

    /* You pass this thing the class of the "top level application supervisor actor",
     * and it starts an ActorSystem by starting up an actor of that class.
     * It will shut the system down when that main actor terminates. */
    akka.Main.main(Array(initialActor))
  }
}

object GreeterMessages {
  case object Greet
  case object Done
}

/* mixing-in ActorLogging makes it so this Actor has its
 * own access to the Akka system's logger (or something) */
class ClientActor extends Actor with ActorLogging {

  def receive = {
    case GreeterMessages.Greet =>
      val greetMsg = "Hello World!"

      println(greetMsg)
      log.info(greetMsg)

      sender() ! GreeterMessages.Done // Send the 'Done' message back to the sender
  }

}
