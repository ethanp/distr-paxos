package ethanp.paxos

import java.time.LocalTime

import ethanp.node.Server
import ethanp.system.Common.PID
import ethanp.system._

import scala.collection.mutable

/**
 * Ethan Petuchowski
 * 3/25/15
 */
class Leader(val server: Server) {

    val LEADER_UNKNOWN = -1
    val myID = server.nodeID

    /**
     * The CURRENT leader shall crash itself after sending
     *    "numMsgs" server side Paxos-related messages.
     *
     * This excludes heartbeat messages if you use them.
     */
    @volatile var timeBomb = 0
    def setTimebombAfter(numMsgs: Int) { timeBomb = numMsgs }

    /* Fields **/
    var ballotNum = Ballot firstFor myID
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

    def spawnScout() {
        println(s"${myID} is running for office")
        currentScout = new Scout(ballotNum, this)
    }

    def preempt(ballot: Ballot) {
        if (ballot > ballotNum) {
            currentScout = null
            active = false
            ballotNum = Ballot(ballot.idx+1, myID)

            /* register for heartbeats from ballot.nodeID */
            leaderID = ballot.nodeID
            server.sendServer(leaderID, HeartbeatReq(myID))
            lastHeartbeat = LocalTime.now

            /* start waiting for heartbeats */
            heartbeatExpectorThread = new Thread(new HeartbeatExpector)
            heartbeatExpectorThread.start()
        }
    }

    def receiveVoteResponse(response: VoteResponse) {
        if (currentScout == null) {
            println(s"$myID ignoring vote response")
            return
        }

        if (response.ballot > currentScout.ballot) preempt(response.ballot)
        else if (currentScout receiveVoteResponse response) gotElected()
    }

    def gotElected() {
        active = true // this will cancel any outstanding HeartbeatExpector
        leaderID = myID
    }

    def receiveHeartbeatFrom(pid: PID) {
        if (!active && (leaderID != LEADER_UNKNOWN)) {
            lastHeartbeat = LocalTime.now
        }
    }

    class HeartbeatExpector extends Runnable {
        override def run() {
            while (!active && !Thread.interrupted()) {
                val startedWaiting = LocalTime.now
                Thread sleep Common.heartbeatTimeout
                if (lastHeartbeat isBefore startedWaiting) {
                    println(s"$myID timedOut on leader $leaderID")
                    spawnScout()
                }
            }
        }
    }
}
