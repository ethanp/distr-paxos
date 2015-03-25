package ethanp.paxos

import java.net.ServerSocket

import scala.collection.mutable

/**
 * Ethan Petuchowski
 * 3/24/15
 */
abstract class Node(nodeIdx: Int) extends Runnable {

    var alive = false
    val listenPort = 3000 + nodeIdx
    val serverSocket = new ServerSocket(listenPort)
    val msgBuffs = mutable.Queue[MsgBuff]()

    def start() {
        alive = true
        init()
        while (alive) {
            val socket = serverSocket.accept()
            val msgBuff = MsgBuff(socket)
            msgBuffs.enqueue(msgBuff)
            new Thread(msgBuff).start()
        }
    }

    def kill() {
        alive = false
    }

    def restart() {
        kill()
        start()
    }

    override def run() {
        while (true) {
            Thread.sleep(300)
            getMsg() match {
                case Some(x) ⇒ handle(x)
                case None ⇒ _ // Do nothing
            }
        }
    }

    /** gets all waiting msgs over all `msgBuffs` */
    def getMsg(): Option[Msg] = {
        for (msgBuff ← msgBuffs) {
            msgBuff.get() match {
                case Some(x) ⇒ return Some(x)
                case None ⇒ _ // I'm hoping this means "do nothing"
            }
        }
        None
    }

    def init()
    def handle(msg: Msg)
}



class Client(nodeIdx: Int) extends Node(nodeIdx) {

    override def restart(): Unit = ???

    override def handle(msg: Msg) {
        msg match {
            case Preempted ⇒
            case PrintLog ⇒
            case AllClear ⇒
            case Crash ⇒
            case CrashAfter(numMsgs) ⇒
        }
    }

    override def init(): Unit = ???
}

class Server(nodeIdx: Int) extends Node(nodeIdx) {

    override def restart(): Unit = ???

    override def run(): Unit = ???

    override def handle(msg: Msg) {
        msg match {
            case Preempted ⇒
            case PrintLog ⇒
            case AllClear ⇒
            case Crash ⇒
            case CrashAfter(numMsgs) ⇒
        }
    }

    override def init(): Unit = ???
}
