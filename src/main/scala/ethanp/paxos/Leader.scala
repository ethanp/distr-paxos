package ethanp.paxos

import ethanp.system.{Ballot, Server, SlotProposal}

import scala.collection.mutable

/**
 * Ethan Petuchowski
 * 3/25/15
 */
class Leader(server: Server) {
    def propose(proposal: SlotProposal) {
        if (proposals.add(proposal) && active) {
            /* TODO spawn a commander to try to replicate the proposal*/
            ???
        }
    }

    var ballotNum = Ballot(0, server.nodeID)
    @volatile var active = false
    val proposals = mutable.Set.empty[SlotProposal]

    def preempt(ballot: Ballot) {
        if (ballot > ballotNum) {
            active = false
            ballotNum = Ballot(ballot.idx+1, server.nodeID)
            /* TODO register for heartbeats from ballot.nodeID */
        }
    }
}
