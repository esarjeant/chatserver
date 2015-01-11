package org.sarjeant.chat;

import java.io.*;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

/**
 * Primary chat server thread. Once started, this listens on the specified
 * port and spawns handlers for each client connection.
 * 
 * @author  $Author$
 * @version $Revision$
 */
public class ChatServerListener implements Runnable
{

	// (bytes) maximum buffer allowed client for input
	private static final int MAX_BUF_LEN = 2046;
	
	// (5 minutes) time to wait for valid command in millis
	private static final int MAX_TIME_WAIT = 300000;

	private boolean _running = false;
	private ServerSocket _listener = null;
	
	private Vector<String> _chatUsers = new Vector<String>();         // the set of active connections
	private Vector<ChatServerHandler> _chatHandles = new Vector<ChatServerHandler>();       // the set of active logins
	
	/**
	 * Create the main chat listener on the specified port. 
	 * 
	 * @param port
	 * @throws IOException
	 */
	public ChatServerListener(int port) throws IOException
	{
		_listener = new ServerSocket(port);
    }
	
	public void run()
	{

		_running = true;
		System.out.println("Listening on port " + _listener.getLocalPort());			

        while (_running)
		{
			
        	try
        	{

        		Socket conn = _listener.accept();

        		// create a handle for the chat
        		ChatServerHandler handle = new ChatServerHandler(conn);
        		_chatHandles.add(handle);

        		// start processing messages from the user
        		Thread thconn = new Thread(handle);
        		thconn.start();

        	}
        	catch (IOException io)
        	{
        		System.err.println(io.getMessage());
        	}
        	
		}
        
        // close the server port
        try
        {
        	_listener.close();
        } catch (IOException io)
        {
        	// ignore...
        }
        
	}
	
	public synchronized void shutdown()
	{
		_running = false;
	}
	
	/**
	 * Login the user to the system. If the user cannot be logged in
	 * then return false, otherwise return true.
	 * 
	 * @param username  The user to login.
	 * @return  Value is <code>true</code> if the login succeeds.
	 */
	private boolean login(String username)
	{
		synchronized (_chatUsers)
		{
			if (!_chatUsers.contains(username))
			{
				return _chatUsers.add(username);
			}
			else
			{
				return false;
			}
		}
	}

	/**
	 * Logout the user from the system. If the user cannot be found then
	 * a <code>false</code> value is returned here.
	 * 
	 * @param username  The user to logout
	 * @return  Value is <code>true</code> if the logout succeeds.
	 */
	private boolean logout(String username)
	{
		synchronized (_chatUsers)
		{
			if (_chatUsers.contains(username))
			{
				return _chatUsers.remove(username);
			}
			else
			{
				return false;
			}
		}
	}

	/**
	 * Send a message to either a user or a room. If there is an error the
	 * an IOException occurs.
	 *
	 * @param user   The user originating the message.
	 * @param dest   The destination, either another user or a room.
	 * @param msg    Text of the message to dispatch.
	 * @throws IOException  Fatal error.
	 */
	private void msg(String user, String dest, String msg) throws IOException, ChatServerException
	{
		
		synchronized (_chatHandles)
		{
			// the # indicates we are sending msg to room
			if ('#' == dest.charAt(0))
			{
				
				StringBuffer outMsg = new StringBuffer();
				outMsg.append(user).append(' ');
				outMsg.append(dest).append(' ');
				outMsg.append(msg);
				
				for (int idx = 0; idx < _chatHandles.size(); idx++)
				{
					ChatServerHandler handle = (ChatServerHandler)_chatHandles.get(idx);
					if (handle.hasRoom(dest))
					{
						handle.send(ChatResponse.GOTROOMMSG, outMsg.toString());
					}
				}
				
				return;
				
			}
			else
			{
				
				StringBuffer outMsg = new StringBuffer();
				outMsg.append(user).append(' ');
				outMsg.append(msg);
				
				for (int idx = 0; idx < _chatHandles.size(); idx++)
				{
					ChatServerHandler handle = (ChatServerHandler)_chatHandles.get(idx);
					if (handle.isUser(dest))
					{
						handle.send(ChatResponse.GOTUSERMSG, outMsg.toString());
						return;
					}
				}
				
				throw new ChatServerException("Specified User Not Found: " + dest);

			}
		}
	}
	
	/**
	 * Inner class to process the commands from a connection that has been
	 * accepted from the main loop.
	 *  
	 * @author eric
	 */
	class ChatServerHandler implements Runnable
	{

		private Socket _sock = null;
		private InetAddress _addr = null;
		private String _username = null;
		private Vector _chatRooms = new Vector();
		
		OutputStreamWriter _writer = null;
		
		ChatServerHandler(Socket sock)
		{
			_sock = sock;
			_addr = _sock.getInetAddress();
		}
		
		public void run()
		{
			
			try
			{
				
				InputStream input = _sock.getInputStream();
				OutputStream output = _sock.getOutputStream();
	
				InputStreamReader reader = new InputStreamReader(input);
				_writer = new OutputStreamWriter(output);

				ByteArrayOutputStream buf = new ByteArrayOutputStream(MAX_BUF_LEN);
				int c = -1;
				long ts = System.currentTimeMillis();  // track time we started
				
				// some intro... 
				send(ChatResponse.OK, ChatServer.APP_ABOUT);
				
				while (_sock.isConnected() && (c = reader.read()) != -1)
				{

					if (MAX_BUF_LEN == buf.size())
					{
						
						buf.reset();
						
						// check for command - after max wait the client is killed
						if ((System.currentTimeMillis() - ts) > MAX_TIME_WAIT)
						{
							_sock.close();
						}

					}

					// append chars other that CR
					if ((char)c != '\r')
					{
						buf.write((char)c);
					}
					
					// detect newline
					if ((char)c == '\n')
					{

						try
						{
							processRequest(buf.toString());
							send(ChatResponse.OK, null);
						}
						catch (ChatServerException cse)
						{
							send(ChatResponse.ERR, cse.getMessage());
						}
						
						buf.reset();
						ts = System.currentTimeMillis();
						
					}
					
				}
				
			} catch (IOException io)
			{
				System.err.println("ChatServerListener: " + io.getMessage() + " for host " + _addr.getHostAddress());
			}
				
		}
		
		/**
		 * Response output to the client. Each message to the client must be
         * of a specified <code>resp</code> type.
		 *
         * @param resp  The type of message being sent.
		 * @param text  The text of the message to send or <code>null</code>
         *              if there is no text to send.
		 * @throws IOException  Fatal error during messaging.
		 */
		protected synchronized void send(ChatResponse resp, String text) throws IOException
		{
			if (_writer != null)
			{
                
                _writer.write(resp.toString());
                _writer.write(' ');
                
                if (text != null)
                    _writer.write(text);
                
				_writer.write("\r\n");
				_writer.flush();
			}
		}
		
		/**
		 * Ask this server connection if it is for the specified login.
		 * 
		 * @param username   The user to check against.
		 * @return <code>true</code> if this connection is for that user.
		 */
		public boolean isUser(String username)
		{
			return (_username != null && _username.equals(username));
		}

		/**
		 * Indicate if the connection is actively in the specified room.
		 * 
		 * @param room  The room to check.
		 * @return <code>true</code> if the user on this connection is in that room.
		 */
		public boolean hasRoom(String room)
		{

			boolean hasroom = false;
			
			synchronized (_chatRooms)
			{
				hasroom = _chatRooms.contains(room);
			}
			
			return hasroom;
			
		}

		/**
		 * Parse the command from the user and perform request actions. If
		 * the command cannot be parsed then return an error.
		 * 
		 * @param msg  The raw data to process. This should be prefixed by
         *             a valid command with parameters, if the data is
         *             malformed then an error occurs here.
		 * @throws ChatServerException Error during processing, most likely
         *         the result of a malformed command.
		 */
		private void processRequest(String msg) throws ChatServerException
		{
			
			String[] msgarray = msg.trim().split(" ");
            ChatCommand cmd = null;

            // ignore empty commands
            if (0 == msgarray.length)
            {
                throw new ChatServerException("Invalid Message for Processing");
            }
            
            // attempt to parse the command
            try
            {
                cmd = ChatCommand.valueOf(msgarray[0]);
            } catch (IllegalArgumentException iae)
            {
                throw new ChatServerException("Invalid Command (" + msgarray[0] + ")");
            }
            
			if (1 == msgarray.length)
			{
				if (cmd == ChatCommand.LOGOUT)
				{
					if (_username != null)
					{
						logout(_username);
						_username = null;					
					}
					
					try
					{
						_sock.close();
					}
					catch (IOException io)
					{
						System.err.println(io.getMessage());
					}
					
					return;
					
				}
			}
			
			if (2 == msgarray.length)
			{
				if (cmd == ChatCommand.LOGIN)
				{
					if (login(msgarray[1]))
					{
						_username = msgarray[1];
						return;
					}
					else
					{
						throw new ChatServerException("Username not accepted");
					}
				}
				
				if (cmd == ChatCommand.JOIN)
				{
					join(msgarray[1]);
					return;
				}
				
				if (cmd == ChatCommand.PART)
				{
					part(msgarray[1]);
					return;
				}
				
			}
			
			if (msgarray.length > 2)
			{
				
				if (cmd == ChatCommand.MSG)
				{
					
					StringBuffer messageText = new StringBuffer();
					for (int idx = 2; idx < msgarray.length; idx++)
					{
						messageText.append(msgarray[idx]).append(' ');
					}

					try
					{
						msg(_username, msgarray[1], messageText.toString());
						return;
					} catch (IOException io)
					{
						throw new ChatServerException(io.getMessage());
					}
					
				}
			}

			// if we got this far - then this is an error
			throw new ChatServerException("Specified Command Invalid");
			
		}
			
		/**
		 * Add the current user to the specified discussion room.
		 * 
		 * @param group  The name of the room to join, it must begin with a <code>#</code> character.
		 * @throws ChatServerException  Unable to join the specified group.
		 */
		private void join(String room) throws ChatServerException
		{
			
			if ('#' == room.charAt(0))
			{				
				synchronized (_chatRooms)
				{
					if (_chatRooms.contains(room))
					{
						throw new ChatServerException("Already joined to " + room);
					}
					
					_chatRooms.add(room);
				}
			}
			else
			{
				throw new ChatServerException("Join Name Must Begin with \'#\' Character");
			}
			
		}	
			
		/**
		 * Remove the current user to the specified discussion room.
		 * 
		 * @param group  The name of the room to leave, it must begin with a <code>#</code> character.
		 * @throws ChatServerException  Unable to leave the specified group.
		 */
		private void part(String room) throws ChatServerException
		{
			
			synchronized (_chatRooms)
			{
				if (_chatRooms.contains(room))
				{
					_chatRooms.remove(room);
				}
				else
				{
					throw new ChatServerException("Not Joined to " + room);
				}
				
			}			
		}	
			
	}

}
