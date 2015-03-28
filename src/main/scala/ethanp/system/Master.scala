package ethanp.system

import java.util.Scanner

import ethanp.node.{Client, Server}
import ethanp.paxos.Leader
import ethanp.system.Common._

import scala.collection.mutable

/**
 * Ethan Petuchowski 3/24/15
 */
object Master {

    val clients = mutable.Map[PID, Client]()
    val servers = mutable.Map[PID, Server]()

    var numServers = -1
    var numClients = -1

    def getAllNodes = clients.values ++ servers.values

    /**
     * might not work before calling "allClear".
     */
    def getLeader: Leader = {
        for (s ← servers.values) {
            if (s.leader != null) {
                val t = servers(s.leader.activeLeaderID)
                if (t.leader != null) {
                    return t.leader
                }
            }
        }
        throw new RuntimeException("no hay un jefe ahora señor")
    }

    def startAllNodes(numServers: Int, numClients: Int) {
        this.numServers = numServers
        this.numClients = numClients

        /* create nodes and start each of their servers */

        for (i ← 0 until numServers) {
            val server: Server = new Server(i) // blocks until ServerSocket connects
            servers.put(i, server)
            new Thread(server).start() // start receiving messages over conns
        }

        for (i ← 0 until numClients) {
            val client: Client = new Client(i)
            clients.put(i, client)
            new Thread(client).start()
        }

        /* connect the nodes to each other */
        getAllNodes foreach { node ⇒
            node.blockingInitAllConns(numClients, numServers)
            Thread sleep 20
        }

        /* the servers run for leadership */
        servers.values.foreach(_.leader.asyncRandomDelayThenSpawnScout())
        Thread sleep 1000
    }

    def handle(input: String) {
        println(s"handling $input")
        handle(input split " ")
    }

    def handle(inputWords: Array[String]) {
        inputWords(0) match {
            /*
             * start up the right number of nodes and clients, and store the
             *  connections to them for sending further commands
             */
            case "start" ⇒
                val numServers = inputWords(1).toInt
                val numClients = inputWords(2).toInt
                startAllNodes(numServers, numClients)

            /*
             * Instruct the client specified by clientIndex to send the message
             * to the proper paxos node
             */
            case "sendMessage" ⇒
                val clientIndex: Int = inputWords(1).toInt
                val chatText = inputWords drop 2 mkString " "
                clients(clientIndex) propose chatText

            /*
             * Print out the client specified by clientIndex's chat history
             * in the format described on the handout.
             */
            case "printChatLog" ⇒
                val clientIndex = inputWords(1).toInt
                clients(clientIndex) printLog()

            /*
             * BLOCKS until all messages that are going to come to consensus in paxos do,
             * and that all clients have heard them.
             */
            case "allClear" ⇒
                var clear = false
                var i = 1
                while (i < 200 && !clear) {
                    /* continue iff any proposals have been proposed but not decided */
                    val totalProposals = clients.values.map(_.proposals.values.size).sum
                    val chatLogSizes = clients.values map (_.chatLog.size)
                    clear = chatLogSizes forall (_ >= totalProposals)
                    if (!clear) Thread sleep 20
                    i += 1
                }
                if (!clear) throw new RuntimeException("waited 4 sec, but still not 'all clear'!")


            /* Immediately crash the server specified by nodeIndex */
            case "crashServer" ⇒
                val nodeIndex = inputWords(1).toInt
                ???

            /* Restart the server specified by nodeIndex */
            case "restartServer" ⇒
                val nodeIndex = inputWords(1).toInt
                ???

            /*
             * Instruct the leader to crash after sending the number of paxos
             * related messages specified by numMessages
             */
            case "timeBombLeader" ⇒
                val numMsgs: Int = inputWords(1).toInt
                getLeader setTimebombAfter numMsgs
        }
    }

    /**
     * their script essentially expects usage along the lines of
     *
     * COMMAND < testFile.test
     *
     * Come to think of it, their script would have been way simpler in plain Bash.
     * Oh, well maybe that wouldn't be Windows compatible enough or something.
     *
     * Note: all their tests start with "start a b", maybe I can leverage that
     *       to simplify things.
     */
    def main(args: Array[String]) {
        val scan = new Scanner(System in)
        while (scan hasNextLine) handle (scan nextLine)
    }
}
