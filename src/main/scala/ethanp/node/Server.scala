package ethanp.node

import ethanp.paxos.{Acceptor, Leader, Replica}
import ethanp.system.Common._
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

    def sendClient(id: PID, msg: Msg) = clientBuffs(id) send msg
    def broadcastClients(msg: Msg) = broadcast(clientBuffs.values, msg)

    override def init(): Unit = ???

    override def offset = Common.serverOffset

    override def myConnObj = ServerConnection(nodeID)

    override def blockingInitAllConns(numClients: Int, numServers: Int) {
        blockingConnectToClients(0 until numClients)
        blockingConnectToServers(0 until numClients filterNot (_ == nodeID))
    }

    override def handle(msg: Msg, senderPort: PID) {
        println(s"server $nodeID rcvd $msg from $senderPort")
        msg match {
            case proposal@ClientProp(_,_,_)     ⇒ replica propose proposal
            case proposal@SlotProp(_,_)         ⇒ leader propose proposal
            case Heartbeat(serverID)            ⇒ leader receiveHeartbeatFrom serverID
            case voteReq@VoteRequest(_,_)       ⇒ acceptor receiveVoteRequest voteReq
            case voteResp@VoteResponse(_,_,_)   ⇒ leader receiveVoteResponse voteResp

            /* unimplemented */
            case LeaderTimeBomb(numMsgs) ⇒ if (leader active) leader setTimebombAfter numMsgs
            case Crash ⇒ ???

            case _ ⇒ throw new RuntimeException("unexpected msg: "+msg)
        }
    }
}
