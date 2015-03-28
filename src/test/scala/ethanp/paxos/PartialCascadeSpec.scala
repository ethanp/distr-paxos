package ethanp.paxos

import ethanp.system.Master.handle
import org.scalatest.WordSpec

/**
 * Ethan Petuchowski
 * 3/27/15
 */
class PartialCascadeSpec extends WordSpec with MsgSpec {
    handle("start 5 2")
    handle("sendMessage 0 all_alive")
    handle("allClear")
    handle("crashServer 0")
    handle("allClear")
    handle("sendMessage 1 0_is_dead")
    handle("allClear")
    handle("crashServer 1")
    handle("restartServer 0")
    handle("allClear")
    handle("sendMessage 0 1_is_dead")
    handle("allClear")
    handle("crashServer 2")
    handle("restartServer 1")
    handle("allClear")
    handle("sendMessage 1 2_is_dead")
    handle("allClear")
    handle("crashServer 3")
    handle("restartServer 2")
    handle("allClear")
    handle("sendMessage 0 3_is_dead")
    handle("allClear")
    handle("printChatLog 0")
    handle("printChatLog 1")
}
