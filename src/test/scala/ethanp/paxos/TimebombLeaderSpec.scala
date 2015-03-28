package ethanp.paxos

import ethanp.system.Master.handle
import org.scalatest.WordSpec

/**
 * Ethan Petuchowski
 * 3/28/15
 */
class TimebombLeaderSpec extends WordSpec with MsgSpec {
    handle("start 5 3")
    handle("timeBombLeader 6")
    handle("sendMessage 0 all_alive")
    handle("allClear")
}
