package ethanp.paxos

import java.time.LocalTime

import ethanp.node.Server
import ethanp.system.Common.printlnGen
import ethanp.system._

import scala.collection.mutable

/**
 * Ethan Petuchowski
 * 3/25/15
 */
class Leader(val server: Server) {
    def tickSend(buff: MsgBuff, msg: Msg) = {
        bombTick()
        buff send msg
        if (timeBomb == 0)
            server.crash()
    }

    def bombTick() {
        if (timeBomb > 0) {
            timeBomb -= 1 // for the local "send"
            printlnGen(s"$myID ticked, now at $timeBomb")
        }
    }


    import Leader._
    val myID = server.nodeID

    /**
     * The CURRENT leader shall crash itself after sending
     *    "numMsgs" server side Paxos-related messages.
     *
     * This excludes heartbeat messages if you use them.
     */
    @volatile var timeBomb = NO_TIMEBOMB
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

        val unproposed = proposals.forall(_.clientProp != proposal.clientProp)

        if (unproposed) {
            proposals add proposal
            if (active) {
                printlnGen(s"$myID issuing commander")
                val pValue = PValue(ballotNum, proposal)
                ongoingCommanders += (proposal → new Commander(pValue, this))
                ongoingCommanders(proposal).broadcastProposal()
            }
        }
        else if (activeLeaderID == LEADER_UNKNOWN) spawnScout()
    }

    def spawnScout() {
        printlnGen(s"$myID is running for office")
        currentScout = new Scout(ballotNum, this)
    }

    def preempt(ballot: Ballot) {
        if (ballot > ballotNum) {
            printlnGen(s"$myID preempted by ${ballot.nodeID}, capitulating")

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
            printlnGen(s"$myID can't capitulate, ballot too low. Is this an ERROR? spawning scout")
            if (active) spawnScout()
        }
    }

    def receiveVoteResponse(response: VoteResponse) {
        server.replica.decisions ++= response.decisions

        if (currentScout == null) {
            printlnGen(s"$myID ignoring vote response")
            return
        }

        if (response.ballot > currentScout.ballot) preempt(response.ballot)
        else if (currentScout.receiveVoteResponse(response) && activeLeaderID != myID) gotElected()
    }

    def receivePValResp(response: PValResponse) {
        ongoingCommanders.get(response.pValue.slotProp) match {
            case Some(commander) => commander receivePValResponse response
            case None => printlnGen(s"$myID ignoring response for non-existent commander")
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
        printlnGen(s"$myID got elected")
        val rcvdSet = currentScout.pvalues.toSet
        bigPlus(proposals, rcvdSet)
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

    /**
     * Warning: I may have missed some cases in here
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
                        printlnGen(s"$myID heartbeater interrupted, stopping heartbeats.")
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
                        printlnGen(s"$myID's heartbeat expector interrupted")
                        return
                }
                if (lastHeartbeat isBefore startedWaiting) {
                    printlnGen(s"$myID timedOut on leader $activeLeaderID")
                    Thread.sleep((Master.numServers - myID) * 100)
                    spawnScout()
                    return
                }
            }
        }
    }
}

object Leader {
    val LEADER_UNKNOWN = -1
    val NO_TIMEBOMB = -1
}
