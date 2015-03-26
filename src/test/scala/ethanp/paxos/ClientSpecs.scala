package ethanp.paxos

import ethanp.node.StoredProposal
import ethanp.system.{SlotProposal, Master}
import org.scalatest.WordSpec

/**
 * Ethan Petuchowski
 * 3/25/15
 */
class ClientSpec_M1 extends WordSpec {

    Master.handle("start 3 2")
    Master.handle("sendMessage 0 helloWorld")

    "A Client" when {
        "sent message" should {
            "store the proposal" in {
                assert(Master.clients(0).proposals.get(1).get == StoredProposal("helloWorld"))
            }
            "send proposal to all servers" in {
                Thread.sleep(300)
                assert(Master.servers.values.forall
                        (_.leader.proposals.contains(SlotProposal(0, 1, 1, "helloWorld"))))
            }
            "all clear" should {
//                Master.handle("allClear")
//                "only return when all the dust has settled" ignore {
//                }

            }
        }
    }
}

class ClientSpec_M2 extends WordSpec {

    Master.handle("start 5 5")
    Master.handle("sendMessage 3 hello_3")
    Master.handle("sendMessage 4 hello_4")

    "both those clients" should {
        "store the proposal" in {
            assert((3 to 4).forall(i ⇒
                Master.clients(i).proposals.get(1).get == StoredProposal(s"hello_$i")))
        }
    }
    "everyone else" should {
        "not have stored proposals" in {
            assert((0 to 2).forall(Master.clients(_).proposals.isEmpty))
        }
    }
}
