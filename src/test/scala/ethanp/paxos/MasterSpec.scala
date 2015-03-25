package ethanp.paxos

import ethanp.system.Master
import org.scalatest.WordSpec

/**
 * Ethan Petuchowski
 * 3/24/15
 */
class MasterSpec extends WordSpec {
  "Master" when {
    "starts up" should {
      "create and connect all nodes" in {
        Master.startAllNodes(3, 3)
        assert {
          List(Master.clients, Master.servers) forall (_.size == 3)
        }
      }
    }
  }
}
