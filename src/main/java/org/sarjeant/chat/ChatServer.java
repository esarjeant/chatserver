package org.sarjeant.chat;

import java.io.IOException;

/**
 * Primary entry point for the chat server. This starts the main
 * listener to begin receiving requests.
 *  
 * @author  $Author$
 * @version $Revision$
 */
public class ChatServer 
{
    
    private static final String APP_NAME = "chatserver";
    private static final String APP_VERSION = "0.1";
    private static final String APP_BUILD = "011506";

    /**
     * Product version information for display during startup and connect.
     */
    public static final String APP_ABOUT = APP_NAME + " " + APP_VERSION + " (Build " + APP_BUILD + ")";

	/**
	 * Static main entry for the application. There are some command line
	 * parameters that can be used here:<br/>
	 * 
	 * <code>
	 * -port [number]
	 * </code>
	 * 
	 * @param args  Refer to the optional arguments specified above.
	 */
	public static void main(String[] args) throws IOException 
	{

		int port = 8888;
		
		if (2 == args.length)
		{
			if (args[0].equals("-port"))
			{
				port = Integer.parseInt(args[1]);
			}
		}
		
		// start server listener.. now start it baby
		ChatServerListener csl = new ChatServerListener(port);
		Thread thmain = new Thread(csl);
		
		// thread to cleanly shutdown server
		ChatServerShutdown css = new ChatServerShutdown(csl);
		Runtime.getRuntime().addShutdownHook(new Thread(css));
		
		// begin running...yahooo....i m a robot...
		thmain.start();
		
	}

}
