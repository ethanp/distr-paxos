package ethanp.paxos

import ethanp.system.Master.handle
import org.scalatest.WordSpec

/**
 * Ethan Petuchowski
 * 3/27/15
 */
class CascadeLeaderSpec extends WordSpec with MsgSpec {
    handle("start 5 3")
    handle("sendMessage 0 helloWorld0")
    handle("allClear")
    handle("timeBombLeader 0")
    handle("allClear")
    handle("sendMessage 1 helloWorld1")
    handle("allClear")
    handle("timeBombLeader 0")
    handle("allClear")
    handle("sendMessage 2 helloWorld2")
    handle("allClear")
    
}
