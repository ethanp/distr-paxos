package ethanp.node

import java.net.ServerSocket

import ethanp.paxos.MsgBuff
import ethanp.system.Common.{printStartup, PID}
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
    startListening()

    class NodeServer(listenPort:Int) extends Runnable {
        val serverSocket = new ServerSocket(listenPort)

        override def run() {
            while (alive) {
                val socket = serverSocket.accept()
                val msgBuff = new MsgBuff(socket, listenPort)
                new Thread(msgBuff).start()
                val nc = msgBuff.blockTillMsgRcvd().asInstanceOf[NodeConnection]
                nc match {
                    case ClientConnection(nodeId) =>
                        clientBuffs.put(nodeId, msgBuff)
                        printStartup(s"$listenPort rcvd conn from client $nodeId")
                        msgBuff.remoteLPort = Common.clientPortFromPid(nodeId)
                    case ServerConnection(nodeId) =>
                        serverBuffs.putIfAbsent(nodeId, msgBuff)
                        printStartup(s"$listenPort rcvd conn from server $nodeId")
                        msgBuff.remoteLPort = Common.serverPortFromPid(nodeId)
                }
            }
        }
    }

    def startListening() {
        alive = true
        serverThread = new Thread(server)
        printStartup("node listening at "+server.serverSocket.getLocalPort)
        serverThread.start()
    }

    def blockingInitAllConns(numClients: Int, numServers: Int)

    def blockingConnectTo(pids: Iterable[PID], buffs: TrieMap[PID, MsgBuff], portFromPid: PID ⇒ Int) {
        for (i ← pids) {
            if (!buffs.contains(i)) {
                val buff = new MsgBuff(portFromPid(i), listenPort)
                new Thread(buff).start()
                buff send myConnObj
                buffs.put(i, buff)
            }
        }
    }

    def blockingConnectToClients(clientIds: Iterable[PID]) {
        blockingConnectTo(clientIds, clientBuffs, Common.clientPortFromPid)
    }
    def blockingConnectToServers(serverIds: Iterable[PID]) {
        blockingConnectTo(serverIds, serverBuffs, Common.serverPortFromPid)
    }

    def sendServer(id: PID, msg: Msg) = serverBuffs(id) send msg

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
            Thread.sleep(10)
            getMsg match {
                case Some((x,y)) ⇒ handle(x, y)
                case None ⇒ // Do nothing
            }
        }
    }

    /** get first waiting msg over all `msgBuffs` */
    def getMsg: Option[(Msg, Int)] = {
        for (msgBuff ← clientBuffs.values ++ serverBuffs.values) {
            msgBuff.readMsgIfAvailable() match {
                case Some(y) ⇒ return Some(y, msgBuff.remoteLPort)
                case None ⇒ ; // I'm hoping this means "do nothing"
            }
        }
        None // nothing was found
    }

    def init()

    def handle(msg: Msg, senderPort: PID): Unit

    def broadcast(buffs: Iterable[MsgBuff], msg: Msg) = buffs foreach (_ send msg)
    def broadcastServers(msg: Msg) = broadcast(serverBuffs.values, msg)
}
