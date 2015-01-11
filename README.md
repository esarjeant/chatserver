Java Chat Server
================

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
The installation and configuration of a Docker environment is somewhat outside
the scope of this document. For general configuration advise, you may wish to start
here:

<http://docker.com>

Once Docker is setup, the maven build may be used to deploy an instance of
container with Java support and then install the Chat Server application to this 
instance.

To execute:

`mvn deploy`

Once deployed, `mvn ps` should show a running instance. You may also start the container
with:

`mvn docker:start`

When the Docker container is running, you may use the docker command to determine what
containers are currently online:

`docker ps`

This should return something like the following:

`CONTAINER ID        IMAGE                                COMMAND                CREATED             STATUS              PORTS                     NAMES`
`f2b2aab3d877        sarjeant/chatserver:0.0.1-SNAPSHOT   "java -jar /maven/ch   6 seconds ago       Up 5 seconds        0.0.0.0:49166->8888/tcp   evil_stallman`

Use the PORTS column to determine what the Chat Server is listening on; in this example it is
port #49166. To communicate to this instance, assuming everything is running on your local machine - 
at a command prompt:

`telnet localhost 49166`


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