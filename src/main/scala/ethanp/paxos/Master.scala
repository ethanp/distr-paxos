package ethanp.paxos

import java.util.Scanner

/**
 * Ethan Petuchowski 3/24/15
 */
object Master {

    val clients = Vector[Client]()
    val servers = Vector[Server]()

    def main(args: Array[String]) {
        val scan: Scanner = new Scanner(System.in)
        while (scan.hasNextLine) {
            val inputLine: Array[String] = scan.nextLine.split(" ")
            var clientIndex: Int = 0
            var nodeIndex: Int = 0
            System.out.println(inputLine(0))
            inputLine(0) match {

                /*
                 * start up the right number of nodes and clients, and store the
                 *  connections to them for sending further commands
                 */
                case "start" ⇒
                    val numServers = inputLine(1).toInt
                    val numClients = inputLine(2).toInt
                    start(numServers, numClients)

                /*
                 * Instruct the client specified by clientIndex to send the message
                 * to the proper paxos node
                 */
                case "sendMessage" ⇒
                    sendMsg(inputLine(1).toInt, inputLine.drop(2).mkString(" "))

                /* TODO
                 * Print out the client specified by clientIndex's chat history
                 * in the format described on the handout.
                 */
                case "printChatLog" ⇒
                    clientIndex = inputLine(1).toInt
                    sendClientMsg(inputLine(2).toInt, PrintLog)
                case "allClear" ⇒
                /* TODO
                 * Ensure that this blocks until all messages that are going to
                 * come to consensus in PAXOS do, and that all clients have heard
                 * of them
                 */
                case "crashServer" ⇒
                    nodeIndex = inputLine(1).toInt
                /* TODO Immediately crash the server specified by nodeIndex */
                case "restartServer" ⇒
                    nodeIndex = inputLine(1).toInt
                /* TODO Restart the server specified by nodeIndex */
                case "timeBombLeader" ⇒
                    val numMessages: Int = inputLine(1).toInt
                /* TODO
                 * Instruct the leader to crash after sending the number of paxos
                 * related messages specified by numMessages
                 */
            }
        }
    }

    def start(numServers: Int, numClients: Int) {
    }

    def sendMsg(clientIdx: Int, str: String) {
    }

    def send(node: Node, msg: Msg): Unit = ???

    def sendClientMsg(clientIdx: Int, msg: Msg) {
        send(clients(clientIdx), msg)
    }
}
