package ethanp.paxos

import java.net.ServerSocket

import scala.collection.mutable

/**
 * Ethan Petuchowski
 * 3/24/15
 */
abstract class Node(nodeIdx: Int) extends Runnable {

    var alive = false
    val listenPort = nodeIdx + offset
    val serverSocket = new ServerSocket(listenPort)
    val msgBuffs = mutable.Queue[MsgBuff]()
    start()

    def start() {
        alive = true
        init()
        while (alive) {
            val socket = serverSocket.accept()
            val msgBuff = MsgBuff(socket)
            msgBuffs.enqueue(msgBuff)
        }
    }

    def kill() {
        alive = false
    }

    def offset: Int

    def restart() {
        kill()
        start()
    }

    override def run() {
        while (alive) {
            Thread.sleep(300)
            getMsg match {
                case Some(x) ⇒ handle(x)
                case None ⇒ _ // Do nothing
            }
        }
    }

    /** get first waiting msg over all `msgBuffs` */
    def getMsg: Option[Msg] = {
        for (msgBuff ← msgBuffs) {
            msgBuff.get() match {
                case y: Some ⇒ return y
                case None ⇒ _ // I'm hoping this means "do nothing"
            }
        }
        None
    }

    def init()

    def handle(msg: Msg) {
        msg match {
            case Preempted =>
            case PrintLog =>
            case AllClear =>
            case Crash =>
            case CrashAfter(numMsgs) =>
            case NodeConnection(nodeId) =>
        }
    }
}
