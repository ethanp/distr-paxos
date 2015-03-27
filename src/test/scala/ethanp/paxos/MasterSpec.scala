package ethanp.paxos

import java.lang.Thread.sleep

import ethanp.system.Master
import ethanp.system.Master.handle
import org.scalatest.WordSpec

/**
 * Ethan Petuchowski
 * 3/24/15
 */
class MasterSpec extends WordSpec {
  val servers = Master.servers.values
  val clients = Master.clients.values
  def getALeaderID = servers.head.leader.leaderID
  def getLeader = Master.servers(getALeaderID).leader
  handle("start 3 3")
  handle("sendMessage 0 helloWorld")

  "The Master" when {
    "receiving start 3 3" should {
      "create all nodes" in {
        assert(Stream(clients, servers) forall (_.size == 3))
      }
      "have clients" which {
        "are connected to the servers" in {
          assert(clients.forall(_.serverBuffs.size == 3))
        }
        "are not connected to each other" in {
          assert(clients.forall(_.clientBuffs.isEmpty))
        }
      }
      "have servers" which {
        "are connected to each other" in {
          assert(servers.forall(_.serverBuffs.size == 2))
        }
        "are connected to the clients" in {
          assert(servers.forall(_.clientBuffs.size == 3))
        }
        "start a bunch of Scouts" in {
          assert(servers.forall(_.leader.currentScout != null))
        }
        "have a single active leader" in {
          assert(servers.count(_.leader.active) == 1)
        }
        "agree whom the leader is" in {
          assert(servers.forall(_.leader.leaderID == getALeaderID))
        }
      }
    }
    "receiving sendMessage 0 helloWorld" should {
      val t = "helloWorld"
      "have clients" which {
        "0 has only the message to propose" in {
          assert(Master.clients(0).proposals.size == 1)
          assert(Master.clients(0).proposals(1).text == t)
        }
        "1 and 2 don't have the message to propose" in {
          assert((1 to 2).forall(Master.clients(_).proposals.isEmpty))
        }
        "each have received the decision" in {
          assert(clients forall (_.chatLog.size == 1))
          assert(clients forall (_.chatLog.values.head.text == t))
        }
      }
      "have servers" which {
        "the leader has received the proposal" in {
          getLeader.proposals.head.clientProp.text == "helloWorld"
        }
        "the leader has a commander for the proposal" in {
          assert(getLeader.ongoingCommanders.size == 1)
          assert(getLeader.ongoingCommanders.values.head.pValue.slotProp.clientProp.text == t)
        }
        "the replicas have received the (correct) pValue" in {
          servers.forall(_.replica.proposals(1).text == t)
        }
        "the non leaders have no commanders" in {
          assert(servers.filterNot(_.leader.active).forall(_.leader.ongoingCommanders.isEmpty))
        }
      }
    }
  }
}

class JustThePrints extends WordSpec {
  handle("start 3 3")
  handle("sendMessage 0 helloWorld")
  sleep(3000)
}
