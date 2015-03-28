package ethanp.paxos

import java.time.LocalTime

import ethanp.node.Server
import ethanp.system._

import scala.collection.mutable

/**
 * Ethan Petuchowski
 * 3/25/15
 */
class Leader(val server: Server) {
    import Leader._
    val myID = server.nodeID

    /**
     * The CURRENT leader shall crash itself after sending
     *    "numMsgs" server side Paxos-related messages.
     *
     * This excludes heartbeat messages if you use them.
     */
    @volatile var timeBomb = -1
    def setTimebombAfter(numMsgs: Int) {
        if (numMsgs == 0) server.crash()
        else timeBomb = numMsgs
    }

    /* Fields **/
    var ballotNum = Ballot firstFor myID
    @volatile var active = false
    val proposals = mutable.Set.empty[SlotProp]

    var lastHeartbeat = LocalTime.now
    var heartbeatThread : Thread = null
    var activeLeaderID: Int = LEADER_UNKNOWN

    var currentScout: Scout = null
    val ongoingCommanders = mutable.Map.empty[SlotProp, Commander]

    def asyncDelayThenSpawnScout() {
        new Thread {
            Thread.sleep((Master.numServers - myID) * 100)
            if (activeLeaderID == LEADER_UNKNOWN) spawnScout()
        }
    }

    def propose(proposal:  SlotProp) {
        if ((proposals add proposal) && active) {
            println(s"$myID issuing commander")
            val pValue = PValue(ballotNum, proposal)
            ongoingCommanders += (proposal → new Commander(pValue, this))
            ongoingCommanders(proposal).broadcastProposal()
        }
        else if (activeLeaderID == LEADER_UNKNOWN) spawnScout()
    }

    def spawnScout() {
        println(s"$myID is running for office")
        currentScout = new Scout(ballotNum, this)
    }

    def preempt(ballot: Ballot) {
        if (ballot > ballotNum) {
            println(s"$myID preempted by ${ballot.nodeID}, capitulating")

            // cancel outstanding heartbeater
            if (heartbeatThread != null && heartbeatThread.isAlive)
                heartbeatThread.interrupt()

            currentScout = null
            active = false
            ballotNum = Ballot(ballot.idx+1, myID)
            activeLeaderID = ballot.nodeID
            lastHeartbeat = LocalTime.now

            /* start waiting for heartbeats */
            heartbeatThread = new Thread(new HeartbeatExpector)
            heartbeatThread.start()
        }
        else {
            println(s"$myID can't capitulate, ballot too low. Is this an ERROR? spawning scout")
            if (active) spawnScout()
        }
    }

    def receiveVoteResponse(response: VoteResponse) {
        if (currentScout == null) {
            println(s"$myID ignoring vote response")
            return
        }

        if (response.ballot > currentScout.ballot) preempt(response.ballot)
        else if (currentScout.receiveVoteResponse(response) && activeLeaderID != myID) gotElected()
    }

    def receivePValResp(response: PValResponse) {
        ongoingCommanders.get(response.pValue.slotProp) match {
            case Some(commander) => commander receivePValResponse response
            case None => println(s"$myID ignoring response for non-existent commander")
        }
    }

    /* TODO this should be implicit method on mutable.Set[SlotProp] called "⊕" */
    def bigPlus(proposals: mutable.Set[SlotProp], values: Set[PValue]) {
        proposals ++= values.groupBy(_.slotProp).map(_._2.maxBy(_.ballot).slotProp).toSet
    }

    /**
     * in PMMC this would be reception of the "adopted" message
     */
    def gotElected() {
        if (activeLeaderID != myID && heartbeatThread != null) heartbeatThread.interrupt()
        println(s"$myID got elected")
        bigPlus(proposals, currentScout.pvalues.toSet)
        activeLeaderID = myID

        /* start heartbeating */
        heartbeatThread = new Thread(new Heartbeater)
        heartbeatThread.start()

        // The paper has this AFTER spawning the commanders, but I says that makes nonsense
        active = true // this will cancel any outstanding HeartbeatExpector

        /* send out all existing proposals
         * (seems mighty inefficient, as some may have been decided already) */
        for (p ← proposals) {
            val pValue = PValue(ballotNum, p)
            ongoingCommanders += (p → new Commander(pValue, this))
            ongoingCommanders(p).broadcastProposal()
        }
    }

    /** TODO this surely makes no sense
      *
      * There's gotta be a bunch of cases in here!
      *
      * 1. For example what if someone times out on my heartbeats, gets elected, and starts
      *    sending me heartbeats?
      *
      *      * Well surely, I should capitulate (and don't call me Shirley).
      *
      *
      * 2. There's probably a bunch more cases like this too that I'll need to handle
      *
      *      * I guess I'll just get to that when I get to that; right now I'm implementing
      *        PAXOS, not some heart-bleeding bull-crap.
      *
      *      * My guess is that I'll have to include a ballot number in the heartbeat.
      *      * Worst comes to worst, I'll take a gander at the ol' Raft paper.
      */
    def receiveHeartbeat(heartbeat: Heartbeat) {
        if (heartbeat.ballot > ballotNum) {
            preempt(heartbeat.ballot)
        }
        else if (!active) {
            if (activeLeaderID == heartbeat.nodeID) {
                lastHeartbeat = LocalTime.now
            }
        }
    }

    class Heartbeater extends Runnable {
        override def run() {
            while (active && !Thread.interrupted()) {
                try Thread sleep Common.heartbeatTimeout * 2/3
                catch {
                    case e: InterruptedException ⇒
                        println(s"$myID heartbeater interrupted, stopping heartbeats.")
                        return
                }
                server broadcastServers Heartbeat(myID, ballotNum)
            }
        }
    }

    class HeartbeatExpector extends Runnable {
        override def run() {
            while (!active && !Thread.interrupted()) {
                val startedWaiting = LocalTime.now
                try Thread sleep Common.heartbeatTimeout
                catch {
                    case e: InterruptedException ⇒
                        println(s"$myID's heartbeat expector interrupted")
                        return
                }
                if (lastHeartbeat isBefore startedWaiting) {
                    println(s"$myID timedOut on leader $activeLeaderID")
                    spawnScout()
                    return
                }
            }
        }
    }
}

object Leader {
    val LEADER_UNKNOWN = -1
}
