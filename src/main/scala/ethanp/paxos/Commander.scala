package ethanp.paxos

import ethanp.system.Common._
import ethanp.system.{Decision, PValResponse, PValue}

import scala.collection.mutable

/**
 * Ethan Petuchowski
 * 3/25/15
 */
class Commander(val pValue: PValue, leader: Leader) {

    val needResponsesFrom = mutable.Set[PID]() ++ leader.server.serverBuffs.keys + leader.server.nodeID
    val responseThreshold: Int = (needResponsesFrom.size+1) / 2

    def receivePValResponse(pValResponse: PValResponse) {
        if (pValResponse.pValue.ballot == pValue.ballot) {
            needResponsesFrom remove pValResponse.nodeID

            /* TODO may be off by one */
            if (needResponsesFrom.size <= responseThreshold) {
                val dec = Decision(pValue.slotProp)
                leader.server.replica.receiveDecision(dec)
                leader.server.broadcastServers(dec)
            }
        }
        else {
            leader.ongoingCommanders remove pValue.slotProp
            leader preempt pValResponse.pValue.ballot
        }
    }
}
