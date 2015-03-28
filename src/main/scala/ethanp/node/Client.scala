package ethanp.node

import ethanp.system._

import scala.collection.mutable

/**
 * Ethan Petuchowski
 * 3/24/15
 */
class Client(nodeID: Int) extends Node(nodeID) {

    @volatile var curPropID = 0

    case class ChatLogItem(senderID: Int, text: String)
    val chatLog = mutable.Map[Int, ChatLogItem]()

    val proposals = mutable.Map[Int, StoredProposal]()

    def printLog() = chatLog foreach { case (k, v) ⇒ println(s"$k ${v.senderID} ${v.text}") }

    override def offset = Common.clientOffset

    override def blockingInitAllConns(numClients: Int, numServers: Int) {
        blockingConnectToServers(0 until numClients)
    }

    override def myConnObj = ClientConnection(nodeID)

    /**
     * "The stub routine sends the command to ALL replicas
     * (at least one of which is assumed to not crash)
     * and returns only the first response to the command." (PMMC, 2)
     */
    def propose(txt: String) {
        curPropID += 1
        proposals.put(curPropID, StoredProposal(txt))
        broadcast(serverBuffs.values, ClientProp(nodeID, curPropID, txt))
    }

    override def handle(msg: Msg, senderPort: Int) {
        println(s"client $nodeID rcvd $msg from $senderPort")
        msg match {
            case PrintLog => printLog()
            case SlotProp(idx, clientProp) ⇒
                if (clientProp.kID == nodeID) {
                    proposals.get(clientProp.propID).get.responded = true
                }
                chatLog.put(idx, ChatLogItem(clientProp.kID, clientProp.text))

            /* UNIMPLEMENTED */
            case AllClear => ???

            case _ ⇒ throw new RuntimeException("Unhandled msg: "+msg)
        }
    }
}

case class StoredProposal(text: String, var responded: Boolean = false)
