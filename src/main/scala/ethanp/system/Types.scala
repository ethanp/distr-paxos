package ethanp.system

import ethanp.system.Common.PID

/**
 * Ethan Petuchowski
 * 3/24/15
 */

sealed abstract class Msg extends Serializable

/* FOR GIVEN TESTS */
case object PrintLog extends Msg    // master -> client

case class ClientProp(kID: PID, propID: Int, text: String) extends Msg // client -> replicas
case class SlotProp(idx: Int, clientProp: ClientProp) extends Msg      // "proposal" replica -> leaders

/* PROPOSALS */
case class PValue(ballot: Ballot, slotProp: SlotProp) extends Msg
case class PValProp(commanderID: PID, pValue: PValue) extends Msg     // "p2a" commander -> acceptors
case class PValResponse(nodeID: PID, pValue: PValue) extends Msg      // "p2b" acceptors -> commander

case class Decision(slotProp: SlotProp) extends Msg

/* WIRING NODES TOGETHER */
sealed abstract class NodeConnection(nodeId: Int) extends Msg
case class ClientConnection(nodeId: PID) extends NodeConnection(nodeId)
case class ServerConnection(nodeId: PID) extends NodeConnection(nodeId)

/* FOR ELECTIONS */
case class Preempted(ballot: Ballot) extends Msg
case class VoteRequest(nodeID: PID, ballot: Ballot) extends Msg     // "p1a" scout -> acceptors
case class VoteResponse(nodeID: PID, ballot: Ballot, accepteds: Set[PValue], decisions: Map[Int, ClientProp]) extends Msg // "p1b" acceptors -> scout

/* OPTIMIZATION MESSAGES */
case class Heartbeat(nodeID: PID, ballot: Ballot) extends Msg

/* PAXOS OBJECTS */
case class Ballot(idx: Int, nodeID: PID) extends Ordered[Ballot] {
    override def compare(that: Ballot): Int =
        if (idx != that.idx) idx - that.idx
        else nodeID - that.nodeID
}

object Ballot {
    def turnstyle(nodeID: PID) = Ballot(-1, nodeID)
    def firstFor(nodeID: PID)  = Ballot( 0, nodeID)
}
