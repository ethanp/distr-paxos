package ethanp.system

import ethanp.system.Common.PID

/**
 * Ethan Petuchowski
 * 3/24/15
 */

sealed abstract class Msg extends Serializable
case class Preempted(ballot: Ballot) extends Msg
case object PrintLog extends Msg
case object AllClear extends Msg
case object Crash extends Msg
case class CrashAfter(numMsgs: Int) extends Msg
case class ClientProposal(senderID: PID, propID: Int, text: String) extends Msg
case class SlotProposal(senderID: PID, propID: Int, idx: Int, text: String) extends Msg

object SlotProposal {
    def apply(idx: Int, p: ClientProposal) = SlotProposal(p.senderID, p.propID, idx, p.text)
    type SlotProposal = SlotProposal
}


sealed abstract class NodeConnection(nodeId: Int) extends Msg
case class ClientConnection(nodeId: PID) extends NodeConnection(nodeId)
case class ServerConnection(nodeId: PID) extends NodeConnection(nodeId)

case class Ballot(idx: Int, nodeID: PID) extends Ordered[Ballot] {
    override def compare(that: Ballot): PID =
        if (idx != that.idx) idx - that.idx
        else nodeID - that.nodeID
}
