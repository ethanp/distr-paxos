package ethanp.node

import ethanp.paxos.MsgBuff
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

    def printLog() =
        chatLog.toSeq.sortBy(_._1).foreach(i ⇒
            println(s"${i._1-1} ${i._2.senderID}: ${i._2.text}"))

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
        Common.printlnGen(s"client $nodeID rcvd $msg from $senderPort")
        msg match {
            case PrintLog => printLog()
            case SlotProp(idx, clientProp) ⇒
                if (clientProp.kID == nodeID) {
                    proposals.get(clientProp.propID).get.responded = true
                }
                val item = ChatLogItem(clientProp.kID, clientProp.text)
                if (chatLog get idx map (_ == item) getOrElse true) {
                    chatLog.put(idx, item)
                } else {
                    throw new RuntimeException(
                      s"c $nodeID rcvd $msg but already has ${chatLog(idx)} there")
                }

            case _ ⇒ throw new RuntimeException("Unhandled msg: "+msg)
        }
    }

    override def broadcast(buffs: Iterable[MsgBuff], msg: Msg): Unit = buffs foreach (_ send msg)
}

case class StoredProposal(text: String, var responded: Boolean = false)
