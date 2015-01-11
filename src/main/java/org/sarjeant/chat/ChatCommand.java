package org.sarjeant.chat;

/**
 * Command set for the chat server. This enumerator describes the available set
 * of commands for chat.
 * 
 * @author $Author$
 * @version $Revision$
 */
enum ChatCommand {
	
	LOGIN		("LOGIN"),     // begin a connection with your name
							   //  LOGIN <username><CRLF>
	
	LOGOUT		("LOGOUT"),    // end a connection
	
	JOIN		("JOIN"),      // open a chat room
							   //  JOIN #<chatroom><CRLF>
	
	PART		("PART"),      // leave a chat room
                               //  PART #<chatroom><CRLF>
    
	MSG			("MSG");       // send a message to a chat room
	                           //  MSG #<chatroom> <message-text><CRLF>
	
	private String _command = null;
	
	ChatCommand(String command)
	{
		_command = command;
	}
	
	public String toString()
	{
		return _command;
	}
    
}