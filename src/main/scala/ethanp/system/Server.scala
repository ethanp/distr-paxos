package ethanp.system

import ethanp.node.Node
import ethanp.paxos.{Leader, Replica}

/**
 * Ethan Petuchowski
 * 3/24/15
 */
class Server(val nodeID: Int) extends Node(nodeID) {

    val replica = new Replica()
    val leader = new Leader()

    var crashAfter = -1

    override def restart(): Unit = ???

    override def run(): Unit = ???

    override def init(): Unit = ???

    override def offset = Common.serverOffset

    override def myConnObj = ServerConnection(nodeID)

    override def blockingInitAllConns(numClients: Int, numServers: Int) {
        blockingConnectToClients(numClients)
        blockingConnectToServers(numServers)
    }

    override def handle(msg: Msg) {
        msg match {
            case Preempted(ballot) => leader preempt ballot
            case PrintLog => replica.printLog()
            case AllClear => ???
            case Crash => ???
            case CrashAfter(numMsgs) => crashAfter = numMsgs
            case p@Proposal(_,_) => replica propose p
            case _ => throw new RuntimeException("unexpected msg: "+msg)
        }
    }
}
