package ethanp.paxos

import ethanp.system.Common.PID
import ethanp.system.{Ballot, VoteResponse, VoteRequest}

import scala.collection.mutable

/**
 * Ethan Petuchowski
 * 3/25/15
 */
class Scout(var ballot: Ballot, leader: Leader) {

    val needResponsesFrom = mutable.Set[PID]() ++ leader.server.serverBuffs.keys
    val responseThreshold: Int = needResponsesFrom.size / 2

    /* broadcast when scout is created */
    leader.server.broadcastServers(VoteRequest(leader.server.nodeID, leader.ballotNum))

    /**
     * @return true iff this response made us elected
     */
    def receiveVoteResponse(response: VoteResponse): Boolean = {
        needResponsesFrom remove response.acceptorID

        needResponsesFrom.size <= responseThreshold
    }
}
