package ethanp.system

import ethanp.node.Node
import ethanp.paxos.{Leader, Replica}

/**
 * Ethan Petuchowski
 * 3/24/15
 */
class Server(val nodeID: Int) extends Node(nodeID) {

    val replica = new Replica(this)
    val leader = new Leader(this)

    var crashAfter = -1

    override def restart(): Unit = ???

    override def init(): Unit = ???

    override def offset = Common.serverOffset

    override def myConnObj = ServerConnection(nodeID)

    override def blockingInitAllConns(numClients: Int, numServers: Int) {
        blockingConnectToClients(1 to numClients)
        blockingConnectToServers(1 to numClients filterNot (_ == nodeID))
    }

    override def handle(msg: Msg) {
        msg match {
            case Preempted(ballot) ⇒ leader preempt ballot

            case p@SlotProposal() ⇒ leader propose p
            case PrintLog ⇒ replica.printLog()
            case AllClear ⇒ ???
            case Crash ⇒ alive = false
            case CrashAfter(numMsgs) ⇒ crashAfter = numMsgs
            case p@ClientProposal() ⇒ replica propose p
            case _ ⇒ throw new RuntimeException("unexpected msg: "+msg)
        }
    }
}
