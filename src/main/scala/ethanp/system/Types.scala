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
case class LeaderTimeBomb(numMsgs: Int) extends Msg

case class ClientProposal(kID: PID, propID: Int, text: String) extends Msg
case class SlotProposal(kID: PID, propID: Int, idx: Int, text: String) extends Msg
case class PValue(ballot: Ballot, slotProposal: SlotProposal) extends Msg

object SlotProposal {
    def apply(idx: Int, p: ClientProposal): SlotProposal =
        SlotProposal(p.kID, p.propID, idx, p.text)
}


sealed abstract class NodeConnection(nodeId: Int) extends Msg
case class ClientConnection(nodeId: PID) extends NodeConnection(nodeId)
case class ServerConnection(nodeId: PID) extends NodeConnection(nodeId)

case class Ballot(idx: Int, nodeID: PID) extends Ordered[Ballot] {
    override def compare(that: Ballot): Int =
        if (idx != that.idx) idx - that.idx
        else nodeID - that.nodeID
}