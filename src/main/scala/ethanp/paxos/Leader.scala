package ethanp.paxos

import java.time.LocalTime

import ethanp.node.Server
import ethanp.system.Common.PID
import ethanp.system.{Common, HeartbeatReq, Ballot, SlotProposal}

import scala.collection.mutable

/**
 * Ethan Petuchowski
 * 3/25/15
 */
class Leader(val server: Server) {

    val LEADER_UNKNOWN = -1

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

    var lastHeartbeat = LocalTime.now
    var heartbeatExpectorThread: Thread = null
    var leaderID: Int = LEADER_UNKNOWN

    var currentScout: Scout = null

    def propose(proposal: SlotProposal) {
        if (proposals.add(proposal) && active) {
            new Thread(new Commander(this)).start()
        }
    }

    def spawnScout {
        currentScout = new Scout(this)
    }

    def preempt(ballot: Ballot) {
        if (ballot > ballotNum) {
            active = false
            ballotNum = Ballot(ballot.idx+1, server.nodeID)

            /* register for heartbeats from ballot.nodeID */
            leaderID = ballot.nodeID
            server.sendServer(leaderID, HeartbeatReq(server.nodeID))
            lastHeartbeat = LocalTime.now

            /* start waiting for heartbeats */
            heartbeatExpectorThread = new Thread(new HeartbeatExpector)
            heartbeatExpectorThread.start()
        }
    }

    def receiveHeartbeatFrom(pid: PID) {
        if (!active && (leaderID != LEADER_UNKNOWN)) {
            lastHeartbeat = LocalTime.now
        }
    }

    class HeartbeatExpector extends Runnable {
        override def run() {
            while (!active) {
                val startedWaiting = LocalTime.now
                Thread.sleep(Common.heartbeatTimeout)
                if (lastHeartbeat isBefore startedWaiting) {
                    println(s"${server.nodeID} timedOut on leader $leaderID")
                    spawnScout
                }
            }
        }
    }
}
