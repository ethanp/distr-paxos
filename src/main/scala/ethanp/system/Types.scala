package ethanp.system

import ethanp.system.Common.PID

/**
 * Ethan Petuchowski
 * 3/24/15
 */

sealed abstract class Msg extends Serializable

/* FOR GIVEN TESTS */
case object PrintLog extends Msg
case object AllClear extends Msg
case object Crash extends Msg
case class LeaderTimeBomb(numMsgs: Int) extends Msg

/* PROPOSALS */
case class ClientProposal(kID: PID, propID: Int, text: String) extends Msg
case class SlotProposal(kID: PID, propID: Int, idx: Int, text: String) extends Msg
case class PValue(ballot: Ballot, slotProposal: SlotProposal) extends Msg

/* WIRING NODES TOGETHER */
sealed abstract class NodeConnection(nodeId: Int) extends Msg
case class ClientConnection(nodeId: PID) extends NodeConnection(nodeId)
case class ServerConnection(nodeId: PID) extends NodeConnection(nodeId)

/* OTHER PAXOS MESSAGES */
case class Preempted(ballot: Ballot) extends Msg
case class VoteRequest(nodeID: PID, ballot: Ballot) extends Msg
case class VoteResponse(acceptorID: PID, ballot: Ballot, accepteds: Set[PValue]) extends Msg

/* OPTIMIZATION MESSAGES */
case class Heartbeat(nodeID: PID) extends Msg
case class HeartbeatReq(serverID: PID) extends Msg

/* PAXOS OBJECTS */
case class Ballot(idx: Int, nodeID: PID) extends Ordered[Ballot] {
    override def compare(that: Ballot): Int =
        if (idx != that.idx) idx - that.idx
        else nodeID - that.nodeID
}

/* UTIL */
object SlotProposal {
    def apply(idx: Int, p: ClientProposal): SlotProposal =
        SlotProposal(p.kID, p.propID, idx, p.text)
}
