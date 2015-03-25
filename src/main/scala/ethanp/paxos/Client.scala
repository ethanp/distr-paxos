package ethanp.paxos

/**
 * Ethan Petuchowski
 * 3/24/15
 */
class Client(nodeIdx: Int) extends Node(nodeIdx) {

    override def restart(): Unit = ???

    override def init(): Unit = ???

    override def offset = 3000
}
