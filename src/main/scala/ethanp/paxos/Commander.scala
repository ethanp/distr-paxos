package ethanp.paxos

import ethanp.system.Common._
import ethanp.system.{PValProp, Decision, PValResponse, PValue}

import scala.collection.mutable

/**
 * Ethan Petuchowski
 * 3/25/15
 */
class Commander(val pValue: PValue, leader: Leader) {

    val needResponsesFrom = mutable.Set[PID]() ++ leader.server.serverBuffs.keys + leader.myID
    val responseThreshold: Int = (needResponsesFrom.size+1) / 2

    def broadcastProposal() {
        val prop = PValProp(leader.myID, pValue)
        leader.server.acceptor receivePValProp prop // locally
        leader.server broadcastServers prop // remotely
    }

    def receivePValResponse(pValResponse: PValResponse) {
        if (pValResponse.pValue.ballot == pValue.ballot) {
            needResponsesFrom remove pValResponse.nodeID

            /* TODO may be off by one */
            if (needResponsesFrom.size <= responseThreshold) {
                val dec = Decision(pValue.slotProp)
                leader.server.replica.receiveDecision(dec)
                leader.server.broadcastServers(dec)
                exit()
            }
        }
        else {
            exit()
            leader preempt pValResponse.pValue.ballot
        }
    }

    def exit() = leader.ongoingCommanders remove pValue.slotProp
}
