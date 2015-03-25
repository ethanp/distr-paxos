package ethanp.system

/**
 * Ethan Petuchowski
 * 3/25/15
 */
object Common {
    type PID = Int
    val leaderID: PID = 1
    val masterID: PID = 0
    val clientOffset = 3000
    val serverOffset = 4000
    def serverPortFromPid(pid : PID) = pid + serverOffset
    def clientPortFromPid(pid : PID) = pid + clientOffset
}
