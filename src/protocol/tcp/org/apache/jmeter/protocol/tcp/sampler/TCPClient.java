/*
 * Created on 24-Sep-2003
 *
 * Interface for generic TCP protocol handler 
 * 
 */
package org.apache.jmeter.protocol.tcp.sampler;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author sebb AT apache DOT org
 * @version $revision$ $date$
 */
public interface TCPClient
{
	void setupTest();
	void teardownTest();
	
	/**
	 * 
	 * @param os - OutputStream for socket
	 * @return String written to socket
	 */
	String write(OutputStream os);
	/**
	 * 
	 * @param is - InputStream for socket
	 * @return String read from socket
	 */
	String read(InputStream is);
}
