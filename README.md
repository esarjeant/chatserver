Confabulator - Server
=====================

AUTHOR: Eric W. Sarjeant <eric@sarjeant.com>  
DATE:   January 2006


Introduction
------------
This is an updated version of an rudimentary Java Chat Server, which was
first written to test a couple features added to Sun JDK 1.5. This has been 
built using Maven and includes Docker container support to launch the 
server on a separate instance.

The latest version of this can also be downloaded from my
homepage <http://micromux.com>.

Running Standalone
------------------
When not running via Docker, at a command line this may be easily
launched:

`java -jar chatserver.jar`

There is also a Maven exec target which can be invoked:

`mvn exec:java -Dexec.mainClass="org.sarjeant.chat.ChatServer"`

Running Docker
--------------
The installation and configuration of your docker environment is somewhat outside
the scope of this document. For general configuration advise, you may wish to start
here:

<http://docker.com>

Once Docker is setup, the Maven docker plugin may be used to build an instance of
container with Java support and then deploy the Chat Server application to this 
instance.



Connecting to the Server
------------------------
Once a chat server is listening, the client may run using `telnet`; simply connect
to the chat server on the default port:

`telnet localhost 8888`

After establishing a connection, commands are issued directly over the telnet client. 
All commands are terminated with CRLF or NL:

  `LOGIN`  Begin a session with your name: `LOGIN [username]`
  `LOGOUT` End the session: `LOGOUT`
  `JOIN`   Open a chat room: `JOIN #[room]`
  `PART`   Leave a chat room: `PART #[room]`
  `MSG`    Send a message to a chat room: `MSG #[room] [text]`

Server shutdown is using Ctrl+C at the console or terminating Java directly.