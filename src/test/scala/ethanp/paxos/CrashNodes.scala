package ethanp.paxos

import ethanp.system.Master._
import org.scalatest.WordSpec

/**
 * Ethan Petuchowski
 * 4/8/15
 */
class CrashNodes extends WordSpec with MsgSpec {
    handle("start 5 3")
    handle("sendMessage 0 helloWorld0")
    handle("allClear")
    handle("crashServer 0")
    handle("allClear")
    handle("sendMessage 1 helloWorld1")
    handle("allClear")
    handle("crashServer 1")
    handle("allClear")
    handle("sendMessage 2 helloWorld2")
    handle("allClear")
    handle("sendMessage 0 helloWorld0")
    handle("allClear")
    handle("crashServer 2")
    handle("allClear")
    handle("restartServer 1")
    handle("allClear")
    handle("sendMessage 1 helloWorld1")
    handle("allClear")
    handle("crashServer 3")
    handle("allClear")
    handle("restartServer 0")
    handle("allClear")
    handle("sendMessage 2 helloWorld2")
    handle("allClear")
}
