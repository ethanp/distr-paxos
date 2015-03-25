package ethanp.node

import ethanp.system.{ClientConnection, Common}

/**
 * Ethan Petuchowski
 * 3/24/15
 */
class Client(nodeIdx: Int) extends Node(nodeIdx) {

    override def restart(): Unit = ???

    override def init(): Unit = ???

    override def offset = Common.clientOffset

    override def blockingInitAllConns(numClients: Int, numServers: Int) {
        blockingConnectToServers(numServers)
    }

    override def myConnObj = ClientConnection(nodeIdx)
}
