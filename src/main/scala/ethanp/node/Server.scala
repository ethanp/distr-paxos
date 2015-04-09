package ethanp.node

import ethanp.paxos.{MsgBuff, Acceptor, Leader, Replica}
import ethanp.system.Common._
import ethanp.system._

/**
 * Ethan Petuchowski
 * 3/24/15
 */
class Server(val nodeID: Int) extends Node(nodeID) {

    var replica = new Replica(this)
    var leader = new Leader(this)
    var acceptor = new Acceptor(this)

    /**
     * 1. the Node-Thread will stop reading incoming messages, simply discarding them instead
     * 2. any outstanding Commanders and Scouts are immediately slaughtered
     * 3. any outstanding heartbeats are cancelled
     * 4. replica, leader, and acceptor are discarded
     */
    def crash() {
        printlnGen(s"server $nodeID crashing!")

        // 1
        alive = false

        // 2
        leader.currentScout = null
        leader.ongoingCommanders.clear()

        // 3
        if (leader.heartbeatThread != null) {
            leader.heartbeatThread.interrupt()
            leader.heartbeatThread = null
        }

        // 4
        leader = null
        replica = null
        acceptor = null
    }

    /**
     * 1. replica, leader, and acceptor are renewed
     * 2. stop discarding incoming messages
     * 3. resume receiving incoming NodeConnections (shouldn't matter)
     */
    def restart() {
        if (alive) {
            println(s"server $nodeID is already alive")
        }
        printlnGen(s"server $nodeID restarting")

        // 1
        replica = new Replica(this)
        leader = new Leader(this)
        acceptor = new Acceptor(this)

        // 2
        alive = true

        // 3
        serverThread.resume()

        // retrieve the collected memory of the other servers
        leader.spawnScout()

    }

    def sendClient(id: PID, msg: Msg) = clientBuffs(id) send msg
    def broadcastClients(msg: Msg) = broadcast(clientBuffs.values, msg)

    override def offset = Common.serverOffset

    override def myConnObj = ServerConnection(nodeID)

    override def blockingInitAllConns(numClients: Int, numServers: Int) {
        blockingConnectToClients(0 until numClients)
        blockingConnectToServers(0 until numServers filterNot (_ == nodeID))
    }

    override def handle(msg: Msg, senderPort: PID) {
        val s = s"server $nodeID rcvd $msg from $senderPort"
        msg match {
            case m: Heartbeat ⇒ printHeartbeat(s)
            case _ ⇒ printlnGen(s)
        }
        msg match {
            case proposal@ClientProp(_,_,_)     ⇒ replica propose proposal
            case proposal@SlotProp(_,_)         ⇒ leader propose proposal
            case heartbeat@Heartbeat(_,_)       ⇒ leader receiveHeartbeat heartbeat
            case Decision(slotProp)             ⇒ replica receiveDecisionFor slotProp

            /* p1a, p1b */
            case voteReq@VoteRequest(_,_)       ⇒ acceptor receiveVoteRequest voteReq
            case voteResp@VoteResponse(_,_,_,_) ⇒ leader receiveVoteResponse voteResp

            /* p2a, p2b */
            case pProp@PValProp(_,_)            ⇒ acceptor receivePValProp pProp
            case pResp@PValResponse(_,_)        ⇒ leader receivePValResp pResp

            case _ ⇒ throw new RuntimeException("unexpected msg: "+msg)
        }
    }

    override def broadcast(buffs: Iterable[MsgBuff], msg: Msg): Unit =
        this.synchronized {
            for (b ← buffs) {
                msg match {
                    case VoteRequest(_, _) ⇒ tickSend(b, msg)
                    case PValProp(_, _) ⇒ tickSend(b, msg)
                    case _ ⇒ b send msg
                }
            }
        }


    def tickSend(b: MsgBuff, msg: Msg) =
        if (leader != null) leader.tickSend(b, msg)
        else printlnGen(s"$nodeID can't send, already crashed")
}
