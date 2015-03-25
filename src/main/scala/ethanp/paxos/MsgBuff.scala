package ethanp.paxos

import java.io.{ObjectInputStream, ObjectOutputStream}
import java.net.Socket

import scala.collection.mutable

/**
 * Ethan Petuchowski
 * 3/24/15
 */
case class MsgBuff(socket: Socket) extends Runnable {

    val oos = new ObjectOutputStream(socket.getOutputStream)
    val ois = new ObjectInputStream(socket.getInputStream)

    val msgs = mutable.Queue[Msg]()

    def get(): Option[Msg] = if (msgs.isEmpty) None else Some(msgs.dequeue())
    def isEmpty: Boolean = msgs.isEmpty

    override def run() {
        ???
    }
}
