package org.sarjeant.chat;

import java.util.Date;

/**
 * Execute server shutdown. Provide feedback to connected clients that
 * the server is going offline.
 * 
 * @author  $Author $
 * @version $Revision $
 */
class ChatServerShutdown implements Runnable
{
	
	private ChatServerListener _listener = null;
	
	protected ChatServerShutdown(ChatServerListener csl)
	{
		_listener = csl;
	}
	
	public void run()
	{
		System.out.println("Execute Server Shutdown at " + new Date());
		
		synchronized (_listener)
		{
			_listener.shutdown();
		}
	}
}