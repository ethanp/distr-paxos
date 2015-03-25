package ethanp.node

import java.net.ServerSocket

import ethanp.paxos.MsgBuff
import ethanp.system.Common.PID
import ethanp.system._

import scala.collection.concurrent.TrieMap

/**
 * Ethan Petuchowski
 * 3/24/15
 */
abstract class Node(nodeIdx: Int) extends Runnable {

    @volatile var alive = false
    val listenPort = nodeIdx + offset
    val clientBuffs = TrieMap[PID, MsgBuff]()
    val serverBuffs = TrieMap[PID, MsgBuff]()
    val server = new NodeServer(listenPort)
    var serverThread : Thread = null

    class NodeServer(listenPort:Int) extends Runnable {
        val serverSocket = new ServerSocket(listenPort)

        override def run() {
            while (alive) {
                val socket = serverSocket.accept()
                val msgBuff = MsgBuff(socket)
                val nc = msgBuff.blockingReadMsg().asInstanceOf[NodeConnection]
                nc match {
                    case ClientConnection(nodeId) => clientBuffs.put(nodeId, msgBuff)
                    case ServerConnection(nodeId) => serverBuffs.put(nodeId, msgBuff)
                }
            }
        }
    }

    def startListening() {
        alive = true
        serverThread = new Thread(server)
        serverThread.start()
    }

    def blockingInitAllConns(numClients: Int, numServers: Int)

    def blockingConnectTo(num: Int, buffs: TrieMap[PID, MsgBuff], portFromPid: PID ⇒ Int) {
        for (i ← 1 to num) {
            if (!buffs.contains(i)) {
                val buff = new MsgBuff(portFromPid(i))
                buff.send(myConnObj)
                buffs.put(i, buff)
            }
        }
    }

    def blockingConnectToClients(numClients: Int) {
        blockingConnectTo(numClients, clientBuffs, Common.clientPortFromPid)
    }
    def blockingConnectToServers(numServers: Int) {
        blockingConnectTo(numServers, serverBuffs, Common.serverPortFromPid)
    }

    def myConnObj: NodeConnection

    def kill() {
        alive = false
    }

    def offset: Int

    def restart() {
        kill()
        startListening()
    }

    override def run() {
        while (alive) {
            Thread.sleep(300)
            getMsg match {
                case Some(x) ⇒ handle(x)
                case None ⇒ // Do nothing
            }
        }
    }

    /** get first waiting msg over all `msgBuffs` */
    def getMsg: Option[Msg] = {
        for (msgBuff ← clientBuffs.values ++ serverBuffs.values) {
            msgBuff.blockingReadMsg() match {
                case y@Some(_) ⇒ return y
                case None ⇒ ; // I'm hoping this means "do nothing"
            }
        }
        None // nothing was found
    }

    def init()

    def handle(msg: Msg) {
        msg match {
            case Preempted =>
            case PrintLog =>
            case AllClear =>
            case Crash =>
            case CrashAfter(numMsgs) =>
            case nc: NodeConnection =>
        }
    }
}
