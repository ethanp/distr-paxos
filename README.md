Name: Ethan Petuchowski
UT-EID: ecp456
UTCD-ID: ethanp

Major Design Decisions
----------------------

The implementation is based on "Paxos Made Moderately Complex". The program
runs in a single process. When when the master receives the `start <s> <c>`
command, it spawns threads representing each node. These threads then
establish TCP connections to each other (except clients don't connect to other
clients). When a node is "killed", all it's Scouts' and Commanders' threads
and corresponding state are destroyed, and the node's state is all destroyed;
it also drops all messages enqueued for its reception by other nodes, and
drops all messages received after being killed. When it is restarted, its
state is reinitialized and it stops dropping incoming packets.

Installing Scala
----------------

I tried this on a non-programmer friend's Mac and it worked.

Download Java JDK 8 SE from
[here](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
, then run the installer.

To run it on a Mac with Homebrew installed, run

    brew install sbt

This is the Scala Build Tool, it knows how to download all required
dependencies.

Then go to the project directory (`"ScalaPaxos"`), and run

    sbt

This may take 2 minutes as it downloads Scala and other stuff.

Eventually, `sbt` will start, and an interpreter will appear, at which you
should type

    compile

It will download a whole bunch more stuff at this point (e.g. the testing
library), and then it will compile everything. The Scala compiler is very
processor intensive, so it might take 2 minutes.

Now to enable the COMMAND, you have to do

    brew install scala

Now go to the `"PaxosHandout"` directory and you can run e.g.

    python tester.py

The tests you gave out should all pass in about 30 seconds.

### To see it all happen

To turn on printing of messages being sent around, in
`ScalaPaxos/src/main/scala/ethanp/system/Common.scala`, set

    val logGen = true

