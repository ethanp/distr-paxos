package ethanp.paxos

import ethanp.node.Server
import ethanp.system._

import scala.collection.mutable

/**
 * Ethan Petuchowski
 * 3/26/15
 */
class Acceptor(server: Server) {

    val accepted = mutable.Set.empty[PValue]
    var ballotNum = Ballot firstFor server.nodeID

    /* P1A */
    def receiveVoteRequest(request: VoteRequest) {

        if (request.ballot > ballotNum) ballotNum = request.ballot

        /* preempt the leader if req'd */
        if (server.leader.active && request.ballot > server.leader.ballotNum)
            server.leader preempt request.ballot

        server.sendServer(request.nodeID, VoteResponse(server.nodeID, ballotNum, accepted.toSet))
    }

    /* P2A */
    def receivePValProp(prop: PValProp) {
        if (prop.pValue.ballot >= ballotNum) {
            ballotNum = prop.pValue.ballot
            accepted += prop.pValue
        }

        val pValResponse = PValResponse(server.nodeID, PValue(ballotNum, prop.pValue.slotProp))

        // local
        if (prop.commanderID == server.nodeID) {

            // reply to the local commander
            server.leader.ongoingCommanders.get(prop.pValue.slotProp) match {
                case Some(commander) => commander receivePValResponse pValResponse
                case None => throw new RuntimeException("wtf mate")
            }
        }

        // remote
        else {
            server.sendServer(prop.commanderID, pValResponse)
        }
    }
}
