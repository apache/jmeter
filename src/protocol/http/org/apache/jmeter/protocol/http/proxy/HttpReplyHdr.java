package org.apache.jmeter.protocol.http.proxy;
/******************************************************************
*** File HttpReplyHdr.java
***
***/

import java.net.*;
import java.io.*;

//
// Class:     HttpReplyHdr
// Abstract:  The headers of the server HTTP reply.
//

public class HttpReplyHdr
{

	static String CR="\r\n";
	static String HTTP_PROTOCOL="HTTP/1.0";
	static String HTTP_SERVER="Java Proxy Server";

	String lastModified ="";
	long   contentLength=0;
	String extraErrorString ="";

/**
 * Sets the last modified date for a header;
 *
 * @param date  A string holding an interner date
 * @return      true
 */
public boolean setModifiedDate(String date)
	{
		lastModified = date;
		return true;
	}

/**
 * Adds an extra explanation. This extra information is
 * Added to the http headers failure explanation.
 *
 * @param str  Description to add.
 * @return     true.
 */
public boolean addErrorDescription(String str)
	{
		extraErrorString = str;
		return true;
	}

/**
 * Forms a http ok reply header
 *
 * @param ContentType The mime-type of the content
 * @return            A string with the header in it
 */
public String formOk(String ContentType,long  ContentLength)
	{

		 contentLength = ContentLength;

		 String out =new String();

		 out += HTTP_PROTOCOL + " 200 Ok" + CR;
		 out += "Server: " + HTTP_SERVER  + CR;
		 out += "MIME-version: 1.0"       + CR;

		 if (0 < ContentType.length())
			  out += "Content-type: " + ContentType + CR;
		 else
			  out += "Content-Type: text/html" + CR;

		 if (0 != contentLength)
			  out += "Content-Length: " + Long.toString(contentLength) + CR;

		 if (0 < lastModified.length())
			  out +="Last-Modified: " + lastModified + CR;

		 out +=CR;

		 return out;
	}


/**
 * private! builds an http document describing a headers reason.
 *
 * @param Error        Error name.
 * @param Description  Errors description.
 * @return             A string with the HTML description body
 */
private String formErrorBody(String Error,String Description)
	{
	String out;
	//Generate Error Body
	out  ="<HTML><HEAD><TITLE>";
	out += Error ;
	out +="</TITLE></HEAD>";
	out +="<BODY><H2>" + Error +"</H2>\n";
	out +="</P></H3>";
	out += Description;
	out +="</BODY></HTML>";
	return out;
	}



/**
 * builds an http document describing an error.
 *
 * @param Error        Error name.
 * @param Description  Errors description.
 * @return             A string with the HTML description body
 */
private String formError(String Error, String Description)
	{
	/* A HTTP RESPONCE HEADER LOOKS ALOT LIKE:
	 *
	 * HTTP/1.0 200 OK
	 * Date: Wednesday, 02-Feb-94 23:04:12 GMT
	 * Server: NCSA/1.1
	 * MIME-version: 1.0
	 * Last-modified: Monday, 15-Nov-93 23:33:16 GMT
	 * Content-type: text/html
	 * Content-length: 2345
	 * \r\n
	 */

	String body=formErrorBody(Error,Description);
	String header =new String();

	header +=HTTP_PROTOCOL +" " + Error + CR;
	header +="Server: " + HTTP_SERVER   + CR;
	header +="MIME-version: 1.0"        + CR;
	header +="Content-type: text/html"  + CR;

	if (0 < lastModified.length())
		 header +="Last-Modified: " + lastModified +CR;

	header +="Content-Length: " + String.valueOf(body.length())+ CR;

	header += CR;
	header += body;

	return header;
	}


/**
 * Indicates a new file was created.
 *
 * @return    The header in a string;
 */
public String formCreated()
	{
	return formError("201 Created","Object was created");
	}

/**
 * Indicates the document was accepted.
 *
 * @return    The header in a string;
 */
public String formAccepted()
	{
	return formError("202 Accepted","Object checked in");
	}

/**
 * Indicates only a partial responce was sent.
 *
 * @return    The header in a string;
 */
public String  formPartial()
	{
	return formError("203 Partial","Only partail document available");
	}

/**
 * Indicates a requested URL has moved to a new address or name.
 *
 * @return    The header in a string;
 */
public String formMoved()
	{
	//300 codes tell client to do actions
	return formError("301 Moved","File has moved");
	}

/**
 * Never seen this used.
 *
 * @return    The header in a string;
 */
public String formFound()
	{
	return formError("302 Found","Object was found");
	}

/**
 * The requested method is not implemented by the server.
 *
 * @return    The header in a string;
 */
public String formMethod()
	{
	return formError("303 Method unseported","Method unseported");
	}

/**
 * Indicates remote copy of the requested object is current.
 *
 * @return    The header in a string;
 */
public String formNotModified()
	{
	return formError("304 Not modified","Use local copy");
	}

/**
 * Client not otherized for the request.
 *
 * @return    The header in a string;
 */
public String formUnautorized()
	{
	return formError("401 Unathorized","Unathorized use of this service");
	}

/**
 * Payment is required for service.
 *
 * @return    The header in a string;
 */
public String formPaymentNeeded()
	{
	return formError("402 Payment required","Payment is required");
	}

/**
 * Client if forbidden to get the request service.
 *
 * @return    The header in a string;
 */
public String formForbidden()
	{
	return formError("403 Forbidden","You need permission for this service");
	}

/**
 * The requested object was not found.
 *
 * @return    The header in a string;
 */
public String formNotFound()
	{
	return formError("404 Not_found","Requested object was not found");
	}

/**
 * The server had a problem and could not fulfill the request.
 *
 * @return    The header in a string;
 */
public String formInternalError()
	{
	return formError("500 Internal server error","Server broke");
	}

/**
 * Server does not do the requested feature.
 *
 * @return    The header in a string;
 */
public String formNotImplemented()
	{
	return formError("501 Method not implemented","Service not implemented, programer was lazy");
	}

/**
 * Server is overloaded, client should try again latter.
 *
 * @return    The header in a string;
 */
public String formOverloaded()
	{
	return formError("502 Server overloaded","Try again latter");
	}

/**
 * Indicates the request took to long.
 *
 * @return    The header in a string;
 */
public String formTimeout()
	{
	return formError("503 Gateway timeout","The connection timed out");
	}

/**
 * Indicates the client's proxies could not locate a server.
 *
 * @return    The header in a string;
 */
public String formServerNotFound()
	{
	return formError("503 Gateway timeout","The requested server was not found");
	}

/**
 * Indicates the client is not allowed to access the object.
 *
 * @return    The header in a string;
 */
public String formNotAllowed()
	{
	return formError("403 Access Denied","Access is not allowed");
	}
}
