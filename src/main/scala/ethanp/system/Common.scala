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

    val heartbeatTimeout: Long = 200

    val logStartup = true
    def printStartup(x: Any) = if (logStartup) println(x)

    val logHeartbeats = false
    def printHeartbeat(s: Any) = if (logHeartbeats) println(s)
}
