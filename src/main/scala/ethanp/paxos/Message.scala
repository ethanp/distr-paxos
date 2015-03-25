package ethanp.paxos

/**
 * Ethan Petuchowski
 * 3/24/15
 */
sealed abstract class Msg
case object Preempted extends Msg
case object PrintLog extends Msg
case object AllClear extends Msg
case object Crash extends Msg
case class CrashAfter(numMsgs: Int) extends Msg
case class NodeConnection(nodeId: Int) extends Msg
