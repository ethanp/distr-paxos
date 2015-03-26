package ethanp.paxos

import ethanp.system.Master
import org.scalatest.WordSpec

/**
 * Ethan Petuchowski
 * 3/24/15
 */
class MasterSpec extends WordSpec {
  "The Master" when {
    "receiving start 3 3" should {
      Master.handle("start 3 3")
      "create all nodes" in {
        assert {
          List(Master.clients, Master.servers) forall (_.size == 3)
        }
      }
      "have nodes" which {
        "are connected to each other" in {
          assert {
            Master.clients.values.forall { n ⇒
              n.serverBuffs.size == 3 && n.clientBuffs.isEmpty
            } &&
            Master.servers.values.forall { n ⇒
              n.serverBuffs.size == 2 && n.clientBuffs.size == 3
            }
          }
        }
      }
    }
  }
}
