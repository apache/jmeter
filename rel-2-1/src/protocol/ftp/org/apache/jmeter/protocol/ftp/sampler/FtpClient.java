/*
 * Copyright 2001-2004,2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.apache.jmeter.protocol.ftp.sampler;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Simple FTP client (non-passive transfers don't work yet). Kind of a hack,
 * lots of room for optimizations.
 * 
 * @author mike Created August 31, 2001
 * @version $Revision$ Last updated: $Date$
 */
public class FtpClient {
	private static final Logger log = LoggingManager.getLoggerForClass();

	private BufferedWriter out;

	private BufferedReader in;

	private Socket s;

	private boolean passive = false;

	private static int port = 21;

	private static int dataPort = 4096;

	/**
	 * Constructor for the FtpClient object.
	 */
	public FtpClient() {
	}

	/**
	 * Set passive mode.
	 * 
	 * @param flag
	 *            the new Passive value
	 */
	public void setPassive(boolean flag) {
		passive = flag;
	}

	/**
	 * Get a file from the server.
	 * 
	 * @return the Response value
	 */
	public String getResponse() throws IOException {
		StringBuffer response = new StringBuffer();
		String line = in.readLine();
		response.append(line);
		log.info("FtpClient.getResponse(): #" + line + "#");
		while (line.charAt(3) == '-') {
			line = in.readLine();
			response.append("\n");
			response.append(line);
			log.info("FtpClient.getResponse(): #" + line + "#");
		}
		log.info("return response");
		return response.toString();
	}

	/**
	 * Get a file from the server.
	 */
	public String get(String file) throws Exception {
		send("SYST");
		getResponse();
		send("PWD");
		getResponse();
		send("TYPE I");
		getResponse();
		String data = "";
		if (!passive) {
			dataPort++;
			int upper = getUpper(dataPort);
			int lower = getLower(dataPort);
			String ip = InetAddress.getLocalHost().getHostAddress().replace('.', ',');
			String ports = ip + "," + upper + "," + lower;
			log.info("port:" + ports);
			send("PORT " + ports);
			getResponse();
			DataGrabber grab = new DataGrabber(ip, dataPort);
            grab.begin();
			while (!grab.isPortCreated()) {
			}
			send("RETR " + file);
			String response = in.readLine();
			log.info(response);
			log.info("" + dataPort);
			data = "FTP client - File Not Found";
			if (!response.startsWith("5")) {
				while (!grab.isDone()) {
				}
				data = grab.getData();
			}
		} else {
			send("PASV");
			String portResp = getResponse();
			while (!portResp.startsWith("227")) {
				portResp = getResponse();
			}
			int start = portResp.indexOf('(');
			int end = portResp.indexOf(')');
			portResp = portResp.substring(start + 1, end);
			int a = portResp.indexOf(',');
			int b = portResp.indexOf(',', a + 1);
			int c = portResp.indexOf(',', b + 1);
			int d = portResp.indexOf(',', c + 1);
			int e = portResp.indexOf(',', d + 1);
			String ip = portResp.substring(0, a) + "." + portResp.substring(a + 1, b) + "."
					+ portResp.substring(b + 1, c) + "." + portResp.substring(c + 1, d);
			int upper = Integer.parseInt(portResp.substring(d + 1, e));
			int lower = Integer.parseInt(portResp.substring(e + 1));
			send("RETR " + file);
			DataGrabber grab = new DataGrabber(ip, getPort(upper, lower));
            grab.begin();
            getResponse();
			while (!grab.isDone()) {
			}
			data = grab.getData();
		}
		return data;
	}

	/**
	 * Connect to server.
	 */
	public void connect(String host, String username, String password) throws Exception {
		InetAddress addr = InetAddress.getByName(host);
		s = new Socket(addr, port);
		out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));

		InputStreamReader isr = new InputStreamReader(s.getInputStream());
		in = new BufferedReader(isr);
		send("USER " + username);
		send("PASS " + password);
	}

	/**
	 * Disconnect from the server
	 */
	public void disconnect() {
		try {
			send("QUIT");
			getResponse();
		} catch (Exception e) {
			log.error("FTP client - ", e);
		}
		try {
			in.close();
			out.close();
			s.close();
		} catch (Exception e) {
			log.error("FTP client - ", e);
		}
	}

	/**
	 * Send a command to the server.
	 */
	public void send(String command) throws IOException {
		for (int i = 0; i < command.length(); i++) {
			out.write(command.charAt(i));
		}
		out.write('\r');
		out.write('\n');
		out.flush();
	}

	/**
	 * Gets the Port attribute of the FtpClient class.
	 * 
	 * @return the Port value
	 */
	public static int getPort(int upper, int lower) {
		return upper * 256 + lower;
	}

	/**
	 * Gets the Upper attribute of the FtpClient class.
	 * 
	 * @return the Upper value
	 */
	public static int getUpper(int lport) {
		return lport / 256;
	}

	/**
	 * Gets the Lower attribute of the FtpClient class.
	 * 
	 * @return the Lower value
	 */
	public static int getLower(int lport) {
		return lport % 256;
	}

	/**
	 * Grabs the data from the dataport.
	 * 
	 * @author mike Created August 31, 2001
	 * @version $Revision$ Last updated: $Date$
	 */
	private class DataGrabber implements Runnable {
		private StringBuffer buffer = new StringBuffer();

        private Socket sock;

        private boolean done = false;

        private boolean portCreated = false;

        private String host = "";

        private int dgPort = 22;

        private Thread thread;

		/**
		 * Constructor for the dataGrabber object.
		 */
		public DataGrabber(String host, int port) throws Exception {
			this.host = host;
			this.dgPort = port;
			this.thread = new Thread(this);
		}

        public void begin(){
            thread.start();
        }
		/**
		 * Gets the Done attribute of the dataGrabber object.
		 * 
		 * @return the Done value
		 */
		public boolean isDone() {
			return done;
		}

		/**
		 * Gets the Data attribute of the dataGrabber object.
		 * 
		 * @return the Data value
		 */
		public String getData() {
			return buffer.toString();
		}

		/**
		 * Gets the PortCreated attribute of the dataGrabber object.
		 * 
		 * @return the PortCreated value
		 */
		public boolean isPortCreated() {
			return portCreated;
		}

		/**
		 * Main processing method for the dataGrabber object.
		 */
		public void run() {
			try {
				if (passive) {
					sock = new Socket(host, dgPort);
				} else {
					log.info("creating socket on " + dgPort);
					ServerSocket server = new ServerSocket(dgPort);
					log.info("accepting...");
					portCreated = true;
					sock = server.accept();
					log.info("accepted");
				}
            } catch (IOException e) {
            } catch (SecurityException e) {
            }
			try {
				InputStream inStr = sock.getInputStream();
				BufferedInputStream dataIn = new BufferedInputStream(inStr);
				int bufferSize = 4096;
				byte[] inputBuffer = new byte[bufferSize];
				int i = 0;
				while ((i = dataIn.read(inputBuffer, 0, bufferSize)) != -1) {
					buffer.append((char) i);
				}
				dataIn.close();
				sock.close();
			} catch (IOException e) {
				log.error("FTP client: dataGrabber", e);
			}
			done = true;
		}
	}
}
