package ethanp.system

import ethanp.node.Node

/**
 * Ethan Petuchowski
 * 3/24/15
 */
class Server(nodeIdx: Int) extends Node(nodeIdx) {

    override def restart(): Unit = ???

    override def run(): Unit = ???

    override def init(): Unit = ???

    override def offset = Common.serverOffset

    override def myConnObj = ServerConnection(nodeIdx)

    override def blockingInitAllConns(numClients: Int, numServers: Int) {
        blockingConnectToClients(numClients)
        blockingConnectToServers(numServers)
    }
}
