package ethanp.paxos

import ethanp.node.Server
import ethanp.system.{PValue, VoteResponse, VoteRequest, Ballot}

import scala.collection.mutable

/**
 * Ethan Petuchowski
 * 3/26/15
 */
class Acceptor(server: Server) {

    val accepted = mutable.Set.empty[PValue]
    var ballotNum = Ballot turnstyle server.nodeID

    def receiveVoteRequest(request: VoteRequest) {
        if (request.ballot > ballotNum) ballotNum = request.ballot
        server.sendServer(request.nodeID, VoteResponse(server.nodeID, ballotNum, accepted.toSet))
    }
}
