package ethanp.node

import ethanp.system._

import scala.collection.mutable

/**
 * Ethan Petuchowski
 * 3/24/15
 */
class Client(nodeIdx: Int) extends Node(nodeIdx) {

    @volatile var curPropID = 0

    case class ChatLogItem(senderID: Int, text: String)
    val chatLog = mutable.Map[Int, ChatLogItem]()

    val proposals = mutable.Map[Int, StoredProposal]()

    override def restart(): Unit = ???

    override def init() {}

    override def offset = Common.clientOffset

    override def blockingInitAllConns(numClients: Int, numServers: Int) {
        blockingConnectToServers(0 until numClients)
    }

    override def myConnObj = ClientConnection(nodeIdx)

    /**
     * "The stub routine sends the command to ALL replicas
     * (at least one of which is assumed to not crash)
     * and returns only the first response to the command." (PMMC, 2)
     */
    def propose(txt: String) {
        curPropID += 1
        proposals.put(curPropID, StoredProposal(txt))
        broadcast(serverBuffs.values, ClientProposal(nodeIdx, curPropID, txt))
    }

    override def handle(msg: Msg) {
        msg match {
            case PrintLog => chatLog foreach {
                case (k, v) ⇒ println(s"$k ${v.senderID} ${v.text}")
            }
            case AllClear => ???
            case SlotProposal(senderID, propID, idx, text) ⇒
                if (senderID == nodeIdx) {
                    proposals.get(propID).get.responded = true
                }
                chatLog.put(idx, ChatLogItem(senderID, text))
            case _ ⇒ throw new RuntimeException("Unhandled msg: "+msg)
        }
    }
}

case class StoredProposal(text: String, var responded: Boolean = false)
