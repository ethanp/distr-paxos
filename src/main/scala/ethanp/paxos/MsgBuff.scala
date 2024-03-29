package ethanp.paxos

import java.io.{ObjectInputStream, ObjectOutputStream}
import java.net.Socket
import java.util.concurrent.ConcurrentLinkedQueue

import ethanp.system.Common.{printStartup, printlnGen}
import ethanp.system.{Heartbeat, Msg, NodeConnection}

/**
 * Ethan Petuchowski
 * 3/24/15
 *
 * It took way too long to figure out how to accomplish this
 */
class MsgBuff(val socket: Socket, val localLPort: Int) extends Runnable {

    @volatile var alive = true
    var remoteLPort = -1

    def this(remotePort: Int, localLPort: Int) {
        this(new Socket("0.0.0.0", remotePort), localLPort)
        remoteLPort = remotePort
    }

    val inBuff = new ConcurrentLinkedQueue[Msg]()

    val oos = new ObjectOutputStream(socket.getOutputStream)
    val ois = new ObjectInputStream(socket.getInputStream)

    def send(msg: Msg) {
        this.synchronized {
            val s = s"$localLPort sending $msg to $remoteLPort"
            msg match {
                case x: Heartbeat ⇒ ; // ignore
                case x: NodeConnection ⇒ printStartup(s)
                case _ ⇒ printlnGen(s)
            }
            oos writeObject msg
            oos.flush()
        }
    }

    def blockTillMsgRcvd(): Msg = {
        for (i ← 1 to 100) {
            val msg = readMsgIfAvailable()
            if (msg isDefined)
                return msg.get
            Thread sleep 20
        }
        throw new RuntimeException("Msg never received (waited 2 seconds)")
    }

    def run() {
        while (alive) {
            val rcvdMsg = ois.readObject().asInstanceOf[Msg] // this is where we block
            inBuff offer rcvdMsg
            if (inBuff.size() > 5) {
                printlnGen(s"MsgBuff $remoteLPort -> $localLPort has ${inBuff.size()} msgs")
            }
        }
    }

    def kill() {
        alive = false
        socket.close()
    }

    def readMsgIfAvailable(): Option[Msg] = {
        if (inBuff isEmpty) None
        else Some(inBuff.poll())
    }
}
