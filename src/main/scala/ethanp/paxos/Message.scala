package ethanp.paxos

/**
 * Ethan Petuchowski
 * 3/24/15
 */
sealed abstract class Msg
case object Preempted extends Msg
case object PrintLog extends Msg
