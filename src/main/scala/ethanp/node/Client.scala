package ethanp.node

import ethanp.system.{Msg, ClientConnection, Common, Proposal}

/**
 * Ethan Petuchowski
 * 3/24/15
 */
class Client(nodeIdx: Int) extends Node(nodeIdx) {

    override def restart(): Unit = ???

    override def init() {}

    override def offset = Common.clientOffset

    override def blockingInitAllConns(numClients: Int, numServers: Int) {
        blockingConnectToServers(numServers)
    }

    override def myConnObj = ClientConnection(nodeIdx)

    /**
     * "The stub routine sends the command to ALL replicas
     * (at least one of which is assumed to not crash)
     * and returns only the first response to the command." (PMMC, 2)
     */
    def sendMessage(idx: Int, txt: String) {
        broadcast(serverBuffs.values, Proposal(idx, txt))
    }

    override def handle(msg: Msg): Unit = ???
}
