package ethanp.paxos

import ethanp.node.{Client, Server}
import ethanp.system.Master.handle
import ethanp.system._
import org.scalatest.WordSpec

/**
 * Ethan Petuchowski
 * 3/24/15
 */
trait MsgSpec {
  val servers = Master.servers.values
  val clients = Master.clients.values

  def getActiveLeaderID = servers.head.leader.activeLeaderID
  def getActiveLeader = Master.servers(getActiveLeaderID).leader

  def allClients(f: Client ⇒ Boolean) = assert(clients forall f)
  def allServers(f: Server ⇒ Boolean) = assert(servers forall f)
}

class SimpleTestSpec extends WordSpec with MsgSpec {

  val t = "helloWorld"
  val clientProp = ClientProp(0, 1, t)
  val slotProp = SlotProp(1, clientProp)
  val ballot = Ballot(0, 2)
  val pVal = PValue(ballot, slotProp)

  handle("start 3 3")
  handle("sendMessage 0 helloWorld")
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

class ExtendedTestSpec extends WordSpec with MsgSpec {
  val ballot = Ballot(0, 2)

  val t1 = "hello_3"
  val t2 = "hello_4"
  val clientProp1 = ClientProp(3, 1, t1)
  val slotProp1 = SlotProp(1, clientProp1)
  val pVal1 = PValue(ballot, slotProp1)

  val clientProp2 = ClientProp(4, 1, t2)
  val slotProp2 = SlotProp(2, clientProp2)
  val pVal2 = PValue(ballot, slotProp2)

  val slotProps = Set(slotProp1, slotProp2)

  handle("start 5 5")
  handle("sendMessage 3 hello_3")
  handle("allClear")
  handle("sendMessage 4 hello_4")
  handle("allClear")

  "have servers" which {
      "the replicas have received the SlotProps" in {
        allServers(_.replica.proposals(1) == clientProp1)
        allServers(_.replica.proposals(2) == clientProp2)
      }
      "all leaders have received both proposals" in {
        allServers(_.leader.proposals == slotProps)
      }
      "no one has commanders [anymore]" in { // assumes everything has already fully propagated
        allServers(_.leader.ongoingCommanders.isEmpty)
      }
      "acceptors have accepted the PValProps" in {
        allServers(_.acceptor.accepted.map(_.slotProp) == slotProps)
      }
    }
    "have clients" which {
      "clients have responded messages to propose" in {
        assert(Master.clients(3).proposals.size == 1)
        assert(Master.clients(3).proposals(1).text == t1)

        assert(Master.clients(4).proposals.size == 1)
        assert(Master.clients(4).proposals(1).text == t2)

        allClients(_.proposals.values.forall(_.responded))
      }
      "0,1,2 don't have any messages to propose" in {
        assert((0 to 2).forall(Master.clients(_).proposals isEmpty))
      }
      "have all received the decision" in {
        allClients(_.chatLog.size == 2)
        allClients(_.chatLog(1).text == t1)
        allClients(_.chatLog(2).text == t2)
      }
    }
}
