package ethanp.paxos

import java.util.Scanner

/**
 * Ethan Petuchowski 3/24/15
 */
object Master {

    val clients = Vector[Client]()
    val servers = Vector[Server]()
    val leaderIdx = 0

    def startAllNodes(numServers: Int, numClients: Int) = ???

    def send(node: Node, msg: Msg): Unit = ???

    def sendClientStrMsg(clientIndex: Int, s: String): Unit = ???

    def sendClientMsg(clientIdx: Int, msg: Msg) = send(clients(clientIdx), msg)

    def sendServerMsg(i: Int, msg: Msg) = send(servers(i), msg)

    def broadcast(nodes: Vector[Node], msg: Msg) = nodes.foreach(n ⇒ send(n, msg))


    /* expects usage COMMAND < testFile.test */
    def main(args: Array[String]) {
        val scan: Scanner = new Scanner(System.in)
        while (scan.hasNextLine) {
            val inputLine: Array[String] = scan.nextLine.split(" ")

            System.out.println(inputLine(0))
            inputLine(0) match {

                /*
                 * start up the right number of nodes and clients, and store the
                 *  connections to them for sending further commands
                 */
                case "start" ⇒
                    val numServers = inputLine(1).toInt
                    val numClients = inputLine(2).toInt
                    startAllNodes(numServers, numClients)

                /*
                 * Instruct the client specified by clientIndex to send the message
                 * to the proper paxos node
                 */
                case "sendMessage" ⇒
                    val clientIndex: Int = inputLine(1).toInt
                    sendClientStrMsg(clientIndex, inputLine.drop(2).mkString(" "))

                /*
                 * Print out the client specified by clientIndex's chat history
                 * in the format described on the handout.
                 */
                case "printChatLog" ⇒
                    val clientIndex = inputLine(1).toInt
                    sendClientMsg(clientIndex, PrintLog)

                /* TODO
                 * Ensure that this BLOCKS until all messages that are going to
                 * come to consensus in paxos do, and that all clients have heard
                 * of them
                 */
                case "allClear" ⇒
                    broadcast(clients ++ servers, AllClear)
                    ???

                /* Immediately crash the server specified by nodeIndex */
                case "crashServer" ⇒
                    val nodeIndex = inputLine(1).toInt
                    sendServerMsg(nodeIndex, Crash)

                /* TODO Restart the server specified by nodeIndex */
                case "restartServer" ⇒
                    val nodeIndex = inputLine(1).toInt
                    ???

                /*
                 * Instruct the leader to crash after sending the number of paxos
                 * related messages specified by numMessages
                 */
                case "timeBombLeader" ⇒
                    val numMsgs: Int = inputLine(1).toInt
                    sendServerMsg(leaderIdx, CrashAfter(numMsgs))
            }
        }
    }
}
