package ethanp.paxos

import ethanp.system.Common.PID
import ethanp.system.VoteRequest

import scala.collection.mutable

/**
 * Ethan Petuchowski
 * 3/25/15
 */
class Scout(leader: Leader) {
    val needResponsesFrom = mutable.Set[PID]() ++ leader.server.serverBuffs.keys
    val responsesReqd: Int = (needResponsesFrom.size+1)/2

    /* broadcast when scout is created */
    leader.server.broadcastServers(VoteRequest(leader.server.nodeID, leader.ballotNum))
}
