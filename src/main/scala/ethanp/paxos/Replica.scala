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
    val decisions = mutable.Map.empty[Int, ClientProp] // this *also* happens to be the "state"
    var slotNum = 1

    def propose(proposal: ClientProp) {
        if (!proposals.values.toSet.contains(proposal)) {
            val usedIndices = proposals.keySet ++ decisions.keySet
            val firstIdx = (Stream from 1 dropWhile usedIndices.contains).head
            proposals.put(firstIdx, proposal)

            val slotProp = SlotProp(firstIdx, proposal)
            server.leader propose slotProp      // pass it to local leader
            server broadcastServers slotProp    // send it to remote servers
        }
    }

    /** this assumes that the decisions map IS the state */
    def perform(prop: ClientProp) {
        decisions collectFirst {
            case (k, v) if k < slotNum && v == prop ⇒ SlotProp(k, v)
        } match {
            case Some(x) ⇒ ; // nothing to do (client should have already rcvd SlotProp)
            case None ⇒ server.broadcastClients(SlotProp(slotNum, prop))
        }
        slotNum += 1
    }

    def receiveDecisionFor(slotProp: SlotProp) {
        decisions.put(slotProp.idx, slotProp.clientProp)
        while (decisions contains slotNum) {
            val p = decisions get slotNum
            val pPrime = proposals get slotNum
            if (pPrime.isDefined && p.get != pPrime.get) {
                propose(pPrime.get)
            }
            perform(p.get)
        }
    }
}
