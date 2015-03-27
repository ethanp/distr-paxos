package ethanp.paxos

import ethanp.node.{Server, Client}
import ethanp.system.Master

/**
 * Ethan Petuchowski
 * 3/24/15
 */
trait MsgSpec {
  val servers = Master.servers.values
  val clients = Master.clients.values

  def getActiveLeaderID = servers.head.leader.activeLeaderID
  def getActiveLeader = Master.servers(getActiveLeaderID).leader

  def allClients(f: Client ⇒ Boolean) = assert(clients forall f)
  def allServers(f: Server ⇒ Boolean) = assert(servers forall f)
}
