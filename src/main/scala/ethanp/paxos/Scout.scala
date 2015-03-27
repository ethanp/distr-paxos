package ethanp.paxos

import ethanp.system.Common.PID
import ethanp.system.Master.numServers
import ethanp.system._

import scala.collection.mutable

/**
 * Ethan Petuchowski
 * 3/25/15
 */
class Scout(var ballot: Ballot, leader: Leader) {

    val needResponsesFrom = mutable.Set[PID](0 until numServers:_*)
    val responseThreshold: Int = needResponsesFrom.size / 2

    val pvalues = mutable.Set.empty[PValue]
    def myAcceptorsBallot = leader.server.acceptor.ballotNum

    /* broadcast when scout is created */
    if (myAcceptorsBallot > ballot) {
        leader.preempt(myAcceptorsBallot)
    }
    else {
        leader.server.acceptor.ballotNum = ballot
        leader.server.broadcastServers(VoteRequest(leader.server.nodeID, leader.ballotNum))
    }

    /**
     * @return true iff this response made us elected
     */
    def receiveVoteResponse(response: VoteResponse): Boolean = {
        needResponsesFrom remove response.nodeID
        pvalues ++= response.accepteds
        needResponsesFrom.size <= responseThreshold
    }
}
