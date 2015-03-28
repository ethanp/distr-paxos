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

    val waitFor = mutable.Set[PID](0 until numServers:_*)
    val responseThreshold: Int = waitFor.size / 2
    val pvalues = mutable.Set.empty[PValue]
    val acceptor = leader.server.acceptor

    /* broadcast when scout is created */

    // for my local acceptor, simply query it
    if (acceptor.ballotNum > ballot) {
        leader preempt acceptor.ballotNum
    }
    else {
        // local acceptor should "vote" for this scout
        acceptor.ballotNum = ballot
        receiveVoteResponse(VoteResponse(leader.myID, ballot, acceptor.accepted.toSet))

        // local accepted, so ask everyone else
        leader.server.broadcastServers(VoteRequest(leader.server.nodeID, leader.ballotNum))
    }

    /**
     * @return true iff this response made us elected
     *
     * NB: Preemption if need-be happens in the Leader
     *     before this gets called
     */
    def receiveVoteResponse(response: VoteResponse): Boolean = {
        waitFor remove response.nodeID
        pvalues ++= response.accepteds
        waitFor.size <= responseThreshold
    }
}
