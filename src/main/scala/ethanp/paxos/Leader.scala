package ethanp.paxos

import java.time.LocalTime

import ethanp.node.Server
import ethanp.system.{Ballot, SlotProposal}

import scala.collection.mutable

/**
 * Ethan Petuchowski
 * 3/25/15
 */
class Leader(server: Server) {

    /**
     * The CURRENT leader shall crash itself after sending
     *    "numMsgs" server side Paxos-related messages.
     *
     * This excludes heartbeat messages if you use them.
     */
    @volatile var timeBomb = 0
    def setTimeBomb(numMsgs: Int) { timeBomb = numMsgs }

    /* Fields **/
    var ballotNum = Ballot(0, server.nodeID)
    @volatile var active = false
    val proposals = mutable.Set.empty[SlotProposal]
    var lastHeartbeat = LocalTime.now()

    def propose(proposal: SlotProposal) {
        if (proposals.add(proposal) && active) {
            new Thread(new Commander(this)).start()
        }
    }

    def preempt(ballot: Ballot) {
        if (ballot > ballotNum) {
            active = false
            ballotNum = Ballot(ballot.idx+1, server.nodeID)
            /* TODO register for heartbeats from ballot.nodeID */
            lastHeartbeat = LocalTime.now()
        }
    }
}
