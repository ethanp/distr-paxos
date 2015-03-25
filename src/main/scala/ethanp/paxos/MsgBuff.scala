package ethanp.paxos

import java.io.{ObjectInputStream, ObjectOutputStream}
import java.net.Socket

import ethanp.system.Msg

/**
 * Ethan Petuchowski
 * 3/24/15
 */
class MsgBuff(socket: Socket) {
    def this(port: Int) {
        this(new Socket("0.0.0.0", port))
    }

    val oos = new ObjectOutputStream(socket.getOutputStream)
    val ois = new ObjectInputStream(socket.getInputStream)

    def send(msg: Msg) {
        oos.writeObject(msg)
        oos.flush()
    }

    def blockTillMsgRcvd(): Msg = ois.readObject().asInstanceOf[Msg]

    def readMsgIfAvailable(): Option[Msg] = {

        /* don't wait if there's nothing to wait for*/
        if (ois.available() == 0) return None

        /* if we've received at least part of an object,
           wait for the whole thing to arrive */
        val obj = ois.readObject()

        if (obj == null) None
        else Some(obj.asInstanceOf[Msg])
    }
}
