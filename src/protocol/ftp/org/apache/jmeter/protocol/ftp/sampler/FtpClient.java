/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.jmeter.protocol.ftp.sampler;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/*
 * Simple FTP client (non-passive transfers don't work yet)
 * kind of a hack, lots of room for optimizations
 */

/**
 *  Description of the Class
 *
 *@author     mike
 *@created    August 31, 2001
 */
public class FtpClient
{
	private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.protocol.ftp");
	File f = new File("e:\\");
	BufferedWriter out;
	BufferedReader in;
	Socket s;
	boolean passive = false;
	static int port = 21;
	static int dataPort = 4096;

	/**
	 *  Constructor for the FtpClient object
	 */
	public FtpClient()
	{
	}

	/**
	 *  set passive mode
	 *
	 *@param  flag  The new Passive value
	 */
	public void setPassive(boolean flag)
	{
		passive = flag;
	}

	/**
	 *  get a file from the server
	 *
	 *@return                  The Response value
	 *@exception  IOException  Description of Exception
	 */
	public String getResponse() throws IOException
	{
		StringBuffer response = new StringBuffer();
		String line = in.readLine();
		response.append(line);
		log.info("FtpClient.getResponse(): #" + line + "#");
		while (line.charAt(3) == '-')
		{
			line = in.readLine();
			response.append("\n");
			response.append(line);
			log.info("FtpClient.getResponse(): #" + line + "#");
		}
		log.info("return response");
		return response.toString();
	}

	/**
	 *  get a file from the server
	 *
	 *@param  file           Description of Parameter
	 *@return                Description of the Returned Value
	 *@exception  Exception  Description of Exception
	 */
	public String get(String file) throws Exception
	{
		send("SYST");
		getResponse();
		send("PWD");
		getResponse();
		send("TYPE I");
		getResponse();
		String data = "";
		if (!passive)
		{
			dataPort++;
			int upper = getUpper(dataPort);
			int lower = getLower(dataPort);
			String ip = InetAddress.getLocalHost().getHostAddress().replace('.', ',');
			String port = ip + "," + upper + "," + lower;
			log.info("port:" + port);
			send("PORT " + port);
			getResponse();
			dataGrabber grab = new dataGrabber(ip, dataPort);
			while (!grab.isPortCreated())
			{
			}
			send("RETR " + file);
			String response = in.readLine();
			log.info(response);
			log.info(""+dataPort);
			data = "FTP client - File Not Found";
			if (!response.startsWith("5"))
			{
				while (!grab.isDone())
				{
				}
				data = grab.getData();
			}
		}
		else
		{
			send("PASV");
			String port = getResponse();
			while (!port.startsWith("227"))
			{
				port = getResponse();
			}
			int start = port.indexOf('(');
			int end = port.indexOf(')');
			port = port.substring(start + 1, end);
			int a = port.indexOf(',');
			int b = port.indexOf(',', a + 1);
			int c = port.indexOf(',', b + 1);
			int d = port.indexOf(',', c + 1);
			int e = port.indexOf(',', d + 1);
			String ip = port.substring(0, a) + "." + port.substring(a + 1, b) + "." + port.substring(b + 1, c) + "." + port.substring(c + 1, d);
			int upper = Integer.parseInt(port.substring(d + 1, e));
			int lower = Integer.parseInt(port.substring(e + 1));
			int dataPort = getPort(upper, lower);
			send("RETR " + file);
			dataGrabber grab = new dataGrabber(ip, dataPort);
			getResponse();
			while (!grab.isDone())
			{
			}
			data = grab.getData();
		}
		return data;
	}

	/**
	 *  connect to server
	 *
	 *@param  host           Description of Parameter
	 *@param  username       Description of Parameter
	 *@param  password       Description of Parameter
	 *@exception  Exception  Description of Exception
	 */
	public void connect(String host, String username, String password) throws Exception
	{
		InetAddress addr = InetAddress.getByName(host);
		s = new Socket(addr, port);
		out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
		
		InputStreamReader isr = new InputStreamReader(s.getInputStream());
		in = new BufferedReader(isr);
		send("USER " + username);
		send("PASS " + password);
	}

	/**
	 *  disconnect from the server
	 */
	public void disconnect()
	{
		try
		{
			send("QUIT");
			getResponse();
		}
		catch (Exception e)
		{
			log.error("FTP client - ",e);
		}
		try
		{
			in.close();
			out.close();
			s.close();
		}
		catch (Exception e)
		{
			log.error("FTP client - ",e);
		}
	}

	/**
	 *  send a command to the server
	 *
	 *@param  command          Description of Parameter
	 *@exception  IOException  Description of Exception
	 */
	public void send(String command) throws IOException
	{
		for (int i = 0; i < command.length(); i++)
		{
			out.write(command.charAt(i));
		}
		out.write('\r');
		out.write('\n');
		out.flush();
	}

	/**
	 *  Gets the Port attribute of the FtpClient class
	 *
	 *@param  upper  Description of Parameter
	 *@param  lower  Description of Parameter
	 *@return        The Port value
	 */
	public static int getPort(int upper, int lower)
	{
		return upper * 256 + lower;
	}

	/**
	 *  Gets the Upper attribute of the FtpClient class
	 *
	 *@param  port  Description of Parameter
	 *@return       The Upper value
	 */
	public static int getUpper(int port)
	{
		return port / 256;
	}

	/**
	 *  Gets the Lower attribute of the FtpClient class
	 *
	 *@param  port  Description of Parameter
	 *@return       The Lower value
	 */
	public static int getLower(int port)
	{
		return port % 256;
	}

	/**
	 *  grabs the data from the dataport
	 *
	 *@author     mike
	 *@created    August 31, 2001
	 */
	public class dataGrabber implements Runnable
	{
		StringBuffer buffer = new StringBuffer();
		Socket s;
		boolean done = false;
		boolean portCreated = false;
		String host = "";
		int port = 22;

		/**
		 *  Constructor for the dataGrabber object
		 *
		 *@param  host           Description of Parameter
		 *@param  port           Description of Parameter
		 *@exception  Exception  Description of Exception
		 */
		public dataGrabber(String host, int port) throws Exception
		{
			this.host = host;
			this.port = port;
			new Thread((Runnable) this).start();
		}

		/**
		 *  Gets the Done attribute of the dataGrabber object
		 *
		 *@return    The Done value
		 */
		public boolean isDone()
		{
			return done;
		}

		/**
		 *  Gets the Data attribute of the dataGrabber object
		 *
		 *@return    The Data value
		 */
		public String getData()
		{
			return buffer.toString();
		}

		/**
		 *  Gets the PortCreated attribute of the dataGrabber object
		 *
		 *@return    The PortCreated value
		 */
		public boolean isPortCreated()
		{
			return portCreated;
		}

		/**
		 *  Main processing method for the dataGrabber object
		 */
		public void run()
		{
			try
			{
				if (passive)
				{
					s = new Socket(host, port);
				}
				else
				{
					log.info("creating socket on " + port);
					ServerSocket server = new ServerSocket(port);
					log.info("accepting...");
					portCreated = true;
					s = server.accept();
					log.info("accepted");
				}
			}
			catch (Exception e)
			{
			}
			try
			{
				InputStream in = s.getInputStream();
				BufferedInputStream dataIn = new BufferedInputStream(in);
				int bufferSize = 4096;
				byte[] inputBuffer = new byte[bufferSize];
				int i = 0;
				while ((i = dataIn.read(inputBuffer, 0, bufferSize)) != -1)
				{
					buffer.append((char) i);
				}
				dataIn.close();
				s.close();
			}
			catch (Exception e)
			{
				log.error("FTP client: dataGrabber",e);
			}
			done = true;
		}
	}
}
