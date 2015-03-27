package ethanp.paxos

import ethanp.node.Server
import ethanp.system._

import scala.collection.mutable
import scala.language.postfixOps

/**
 * Ethan Petuchowski
 * 3/25/15
 */
class Replica(server: Server) {

    val proposals = mutable.Map.empty[Int, ClientProp]
    val decisions = mutable.Map.empty[Int, ClientProp]

    def propose(proposal: ClientProp) {
        if (!proposals.values.toSet.contains(proposal)) {
            val usedIndices = proposals.keySet ++ decisions.keySet
            val firstIdx = (Stream from 1 dropWhile usedIndices.contains).head
            proposals.put(firstIdx, proposal)
            server.serverBuffs.values.foreach(_.send(SlotProp(firstIdx, proposal)))
        }
    }

    def receiveDecision(decision: Decision) {
        decisions.put(decision.slotProp.idx, decision.slotProp.clientProp)
        /* TODO not finished */
    }
}
