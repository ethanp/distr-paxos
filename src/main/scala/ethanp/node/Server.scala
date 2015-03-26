package ethanp.node

import ethanp.paxos.{Acceptor, Leader, Replica}
import ethanp.system._

/**
 * Ethan Petuchowski
 * 3/24/15
 */
class Server(val nodeID: Int) extends Node(nodeID) {

    val replica = new Replica(this)
    val leader = new Leader(this)
    val acceptor = new Acceptor(this)

    override def restart(): Unit = ???

    override def init(): Unit = ???

    override def offset = Common.serverOffset

    override def myConnObj = ServerConnection(nodeID)

    override def blockingInitAllConns(numClients: Int, numServers: Int) {
        blockingConnectToClients(0 until numClients)
        blockingConnectToServers(0 until numClients filterNot (_ == nodeID))
    }

    override def handle(msg: Msg) {
        msg match {
            case Crash ⇒ alive = false
            case LeaderTimeBomb(numMsgs) ⇒ if (leader active) leader setTimeBomb numMsgs
            case p@ClientProposal(_,_,_) ⇒ replica propose p
            case p@SlotProposal(_,_,_,_) ⇒ leader propose p
            case Preempted(ballot) ⇒ leader preempt ballot
            case _ ⇒ throw new RuntimeException("unexpected msg: "+msg)
        }
    }
}
