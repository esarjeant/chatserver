package org.sarjeant.chat;

/**
 * Primitive error handler for the chat server. This is the only exception
 * that the server can throw.
 * 
 * @author eric
 */
public class ChatServerException extends Exception
{
	
	public ChatServerException(String msg)
	{
		super(msg);
	}
}
