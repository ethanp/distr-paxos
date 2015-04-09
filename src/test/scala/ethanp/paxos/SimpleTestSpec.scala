package ethanp.paxos

import ethanp.system.Master.handle
import ethanp.system._
import org.scalatest.WordSpec

class SimpleTestSpec extends WordSpec with MsgSpec {

  val t = "helloWorld"
  val clientProp = ClientProp(0, 1, t)
  val slotProp = SlotProp(1, clientProp)
  val ballot = Ballot(0, 2)
  val pVal = PValue(ballot, slotProp)

  handle("start 3 3")
  handle("allClear")

  "receiving start 3 3" should {
    "create all nodes" in {
      assert(Stream(clients, servers) forall (_.size == 3))
    }
    "have clients" which {
      "are connected to the servers" in {
        allClients(_.serverBuffs.size == 3)
      }
      "are not connected to each other" in {
       allClients(_.clientBuffs.isEmpty)
      }
    }
    "have servers" which {
      "are connected to each other" in {
        allServers(_.serverBuffs.size == 2)
      }
      "are connected to the clients" in {
        allServers(_.clientBuffs.size == 3)
      }
      "have a single active leader" in {
        assert(servers.count(_.leader.active) == 1)
      }
      "agree whom the leader is" in {
        allServers(_.leader.activeLeaderID == getActiveLeaderID)
      }
    }
  }

  handle("sendMessage 0 helloWorld")
  handle("allClear")
  "receiving sendMessage 0 helloWorld" should {
    "have servers" which {
      "the replicas have received the (correct) SlotProp" in {
        allServers(_.replica.proposals(1) == clientProp)
      }
      "all leaders have received the proposal" in {
        allServers(_.leader.proposals.head.clientProp == clientProp)
      }
      "no one has commanders [anymore]" in { // assumes everything has already fully propagated
        allServers(_.leader.ongoingCommanders.isEmpty)
      }
      "acceptors have accepted the PValProp" in {
        allServers(_.acceptor.accepted.size == 1)
        allServers(_.acceptor.accepted contains pVal)
      }
    }
    "have clients" which {
      "0 has only the message to propose" in {
        assert(Master.clients(0).proposals.size == 1)
        assert(Master.clients(0).proposals(1).text == t)
      }
      "1 and 2 don't have any messages to propose" in {
        assert((1 to 2).forall(Master.clients(_).proposals.isEmpty))
      }
      "have all received the decision" in {
        allClients(_.chatLog.size == 1)
        allClients(_.chatLog.values.head.text == t)
        assert(Master.clients(0).proposals(1).responded)
      }
    }
  }
}

class SimpleTest2Spec extends WordSpec with MsgSpec {

  val t = "helloWorld"
  val clientProp = ClientProp(0, 1, t)
  val slotProp = SlotProp(1, clientProp)

  /* we can't assume that a particular ballot is going to be used */
//  val ballot = Ballot(0, 2)
//  val pVal = PValue(ballot, slotProp)

  handle("start 5 2")
  handle("sendMessage 0 helloWorld")
  handle("allClear")

  "receiving start 5 2" should {
    "create all nodes" in {
      assert(clients.size == 2)
      assert(servers.size == 5)
    }
    "have clients" which {
      "are connected to the servers" in {
        allClients(_.serverBuffs.size == 5)
      }
      "are not connected to each other" in {
       allClients(_.clientBuffs.isEmpty)
      }
    }
    "have servers" which {
      "are connected to each other" in {
        allServers(_.serverBuffs.size == 4)
      }
      "are connected to the clients" in {
        allServers(_.clientBuffs.size == 2)
      }
      "have a single active leader" in {
        assert(servers.count(_.leader.active) == 1)
      }
      "agree whom the leader is" in {
        allServers(_.leader.activeLeaderID == getActiveLeaderID)
      }
    }
  }
  "receiving sendMessage 0 helloWorld" should {
    "have servers" which {
      "the replicas have received the (correct) SlotProp" in {
        allServers(_.replica.proposals(1) == clientProp)
      }
      "all leaders have received the proposal" in {
        allServers(_.leader.proposals.size == 1)
        allServers(_.leader.proposals.head.clientProp == clientProp)
      }
      "no one has commanders [anymore]" in { // assumes everything has already fully propagated
        allServers(_.leader.ongoingCommanders.isEmpty)
      }
      "acceptors have accepted the PValProp" in {
        allServers(_.acceptor.accepted.size == 1)
        allServers(_.acceptor.accepted.head.slotProp == slotProp)
      }
    }
    "have clients" which {
      "0 has only the message to propose" in {
        assert(Master.clients(0).proposals.size == 1)
        assert(Master.clients(0).proposals(1).text == t)
      }
      "1 doesn't have any messages to propose" in {
        assert(Master.clients(1).proposals.isEmpty)
      }
      "have all received the decision" in {
        allClients(_.chatLog.size == 1)
        allClients(_.chatLog.values.head.text == t)
        assert(Master.clients(0).proposals(1).responded)
      }
    }
  }
}


