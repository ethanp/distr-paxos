package ethanp.paxos

import ethanp.system.Common._
import ethanp.system.Master.numServers
import ethanp.system._

import scala.collection.mutable

/**
 * Ethan Petuchowski
 * 3/25/15
 */
class Commander(val pValue: PValue, leader: Leader) {

    val needResponsesFrom = mutable.Set[PID](0 until numServers:_*)
    val responseThreshold: Int = needResponsesFrom.size / 2

    def broadcastProposal() {
        val prop = PValProp(leader.myID, pValue)
        leader.server.acceptor receivePValProp prop // locally
        leader.server broadcastServers prop // remotely
    }

    def receivePValResponse(pValResponse: PValResponse) {
        if (pValResponse.pValue.ballot == pValue.ballot) {
            needResponsesFrom remove pValResponse.nodeID
            if (needResponsesFrom.size <= responseThreshold) {
                val dec = Decision(pValue.slotProp)
                leader.server.replica receiveDecision dec
                leader.server broadcastServers dec
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
