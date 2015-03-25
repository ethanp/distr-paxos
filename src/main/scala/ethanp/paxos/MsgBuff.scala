package ethanp.paxos

import java.io.{ObjectInputStream, ObjectOutputStream}
import java.net.Socket

/**
 * Ethan Petuchowski
 * 3/24/15
 */
case class MsgBuff(socket: Socket) {
    def this(port: Int) {
        this(new Socket("0.0.0.0", port))
    }

    val oos = new ObjectOutputStream(socket.getOutputStream)
    val ois = new ObjectInputStream(socket.getInputStream)

    def send(msg: Msg) { oos.writeObject(msg) }

    def get(): Option[Msg] = {
        val obj = ois.readObject()
        if (obj == null) None
        else Some(obj.asInstanceOf[Msg])
    }
}
