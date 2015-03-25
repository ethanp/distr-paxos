package ethanp.system

import java.util.Scanner

import ethanp.node.{Client, Node}
import ethanp.system.Common._

import scala.collection.{GenTraversable, mutable}

/**
  * Ethan Petuchowski 3/24/15
  */
object Master {

     val clients = mutable.Map[PID, Client]()
     val servers = mutable.Map[PID, Server]()

//     val clientConns = mutable.Map[PID, MsgBuff]()
//     val serverConns = mutable.Map[PID, MsgBuff]()

     def getAllNodes = clients.values ++ servers.values

     def startAllNodes(numServers: Int, numClients: Int) {

         /* create nodes and start each of their servers */

         for (i ← 1 to numServers) {
             val server: Server = new Server(i) // blocks until ServerSocket connects
             servers.put(i, server)
             new Thread(server).start()

             // creates TCP socket conn, which they accept and create their own MsgBuff out of
             // not sure I need this now though because as long as I'm going the multithreaded route
             // I have a direct voice to these guys
//             serverConns.put(i, new MsgBuff(server.listenPort))
         }

         for (i ← 1 to numClients) {
             val client: Client = new Client(i)
             clients.put(i, client)
             new Thread(client).start()
//             clientConns.put(i, new MsgBuff(client.listenPort))
         }

         /* connect the nodes to each other */

         getAllNodes foreach (_ blockingInitAllConns(numClients, numServers))
     }

     def send(node: Node, msg: Msg): Unit = ???

     def sendClientStrMsg(clientIndex: Int, s: String): Unit = ???

     def sendClientMsg(clientIdx: Int, msg: Msg) = send(clients(clientIdx), msg)

     def sendServerMsg(i: Int, msg: Msg) = send(servers(i), msg)

     def broadcast(nodes: GenTraversable[Node], msg: Msg) = nodes.foreach(n ⇒ send(n, msg))


     /**
      * their script essentially expects usage along the lines of
      *
      *      COMMAND < testFile.test
      *
      * Come to think of it, their script would have been way simpler in plain Bash.
      * Oh, well maybe that wouldn't be Windows compatible enough or something.
      */
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
                     sendClientStrMsg(clientIndex, inputLine drop 2 mkString " ")

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
                     broadcast(getAllNodes, AllClear)
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
                     sendServerMsg(leaderID, CrashAfter(numMsgs))
             }
         }
     }
 }
