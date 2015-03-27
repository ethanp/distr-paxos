package ethanp.paxos

import ethanp.system.Master.handle
import ethanp.system.{Master, SlotProp, ClientProp}
import org.scalatest.WordSpec

/**
 * Ethan Petuchowski
 * 3/27/15
 */
class ExtendedTestSpec extends WordSpec with MsgSpec {

  val t1 = "hello_3"
  val t2 = "hello_4"
  val clientProp1 = ClientProp(3, 1, t1)
  val slotProp1 = SlotProp(1, clientProp1)

  val clientProp2 = ClientProp(4, 1, t2)
  val slotProp2 = SlotProp(2, clientProp2)

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
        allServers(_.leader.ongoingCommanders isEmpty)
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
