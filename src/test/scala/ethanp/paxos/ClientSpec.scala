package ethanp.paxos

import ethanp.node.StoredProposal
import ethanp.system.{SlotProposal, Master}
import org.scalatest.WordSpec

/**
 * Ethan Petuchowski
 * 3/25/15
 */
class ClientSpec extends WordSpec {
    Master.handle("start 3 2")
    "A Client" when {
        "sent message" should {
            Master.handle("sendMessage 0 helloWorld")
            "store the proposal" in {
                assert(Master.clients(0).proposals.get(1).get == StoredProposal("helloWorld", false))
            }
            "send proposal to all servers" in {
                Thread.sleep(300)
                assert(Master.servers.values.forall
                        (_.leader.proposals.contains(SlotProposal(0, 1, 1, "helloWorld"))))
            }
        }
    }
}
