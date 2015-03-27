package ethanp.paxos

import java.time.LocalTime

import ethanp.node.Server
import ethanp.system.Common.PID
import ethanp.system._

import scala.collection.mutable
import scala.util.Random

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
    val proposals = mutable.Set.empty[SlotProp]

    var lastHeartbeat = LocalTime.now
    var heartbeatThread : Thread = null
    var leaderID: Int = LEADER_UNKNOWN

    var currentScout: Scout = null
    val ongoingCommanders = mutable.Map.empty[PValue, Commander]

    def asyncRandomDelayThenSpawnScout() {
        /*ooh shiny!*/
        new Thread {
            Thread.sleep(Random.nextInt(10) * 100)
            if (leaderID == LEADER_UNKNOWN) spawnScout()
        }
    }

    def propose(proposal: SlotProp) {
        if (proposals.add(proposal) && active) {
            val pValue = PValue(ballotNum, proposal)
            ongoingCommanders += (pValue → new Commander(pValue, this))
        }
    }

    def spawnScout() {
        println(s"$myID is running for office")
        currentScout = new Scout(ballotNum, this)
    }

    def preempt(ballot: Ballot) {
        if (ballot > ballotNum) {
            println(s"$myID preempted by ${ballot.nodeID}, capitulating")
            currentScout = null
            active = false
            ballotNum = Ballot(ballot.idx+1, myID)

            /* register for heartbeats from ballot.nodeID */
            leaderID = ballot.nodeID
//            server.sendServer(leaderID, HeartbeatReq(myID))
            lastHeartbeat = LocalTime.now

            /* start waiting for heartbeats */
            heartbeatThread = new Thread(new HeartbeatExpector)
            heartbeatThread.start()
        }
    }

    def receiveVoteResponse(response: VoteResponse) {
        if (currentScout == null) {
            println(s"$myID ignoring vote response")
            return
        }

        if (response.ballot > currentScout.ballot) preempt(response.ballot)
        else if (currentScout.receiveVoteResponse(response) && leaderID != myID) gotElected()
    }

    /* TODO this should be implicit method on mutable.Set[SlotProp] called "⊕" */
    def bigPlus(proposals: mutable.Set[SlotProp], values: Set[PValue]) {
        proposals ++= values.groupBy(_.slotProp).map(_._2.maxBy(_.ballot).slotProp).toSet
    }

    /**
     * in PMMC this would be reception of the "adopted" message
     */
    def gotElected() {
        if (leaderID != myID && heartbeatThread != null) heartbeatThread.interrupt()
        println(s"$myID got elected")
        bigPlus(proposals, currentScout.pvalues.toSet)
        active = true // this will cancel any outstanding HeartbeatExpector
        leaderID = myID

        /* start heartbeating */
        heartbeatThread = new Thread(new Heartbeater)
        heartbeatThread.start()
    }

    def receiveHeartbeatFrom(pid: PID) {
        if (!active && (leaderID != LEADER_UNKNOWN)) {
            lastHeartbeat = LocalTime.now
        }
    }

    class Heartbeater extends Runnable {
        override def run() {
            while (active && !Thread.interrupted()) {
                Thread sleep Common.heartbeatTimeout * 2/3
                server broadcastServers Heartbeat(myID)
            }
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

