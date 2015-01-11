package org.sarjeant.chat;

/**
 * Describe the response from the server. A response can be
 * either success or failure, with the commands <code>OK</code>
 * and <code>ERROR</code> respectively.
 * 
 * @author  $Author$
 * @version $Revision$
 */
enum ChatResponse 
{

    OK              ("OK"),             // successful response
    ERR             ("ERROR"),          // error response
    GOTROOMMSG      ("GOTROOMMSG"),     // message for specified room
    GOTUSERMSG      ("GOTUSERMSG");     // message for specified user
    
    private String _response = null;

    ChatResponse(String resp)
    {
        _response = resp;
    }

    public String toString()
    {
        return _response;
    }

}
