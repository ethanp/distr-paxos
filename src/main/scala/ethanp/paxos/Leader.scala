package ethanp.paxos

import ethanp.system.{Proposal, Server, Ballot}

/**
 * Ethan Petuchowski
 * 3/25/15
 */
class Leader(server: Server) {
    var ballotNum = Ballot(0, server.nodeID)
    @volatile var active = false
    val proposals = Set.empty[Proposal]

    def preempt(ballot: Ballot) {
        if (ballot > ballotNum) {
            active = false
            ballotNum = Ballot(ballot.idx+1, server.nodeID)
            /* TODO register for heartbeats from ballot.nodeID */
        }
    }
}
