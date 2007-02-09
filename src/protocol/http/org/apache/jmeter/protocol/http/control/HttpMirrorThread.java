/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

package org.apache.jmeter.protocol.http.control;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * Thread to handle one client request. Gets the request from the client and
 * sends the response back to the client.
 */
public class HttpMirrorThread extends Thread {
    private static final Logger log = LoggingManager.getLoggerForClass();

	/** Socket to client. */
	private final Socket clientSocket;

	public HttpMirrorThread(Socket _clientSocket) {
		this.clientSocket=_clientSocket;
	}

	/**
	 * Main processing method for the HttpMirror object
	 */
	public void run() {
		log.info("Starting thread");
		BufferedReader in = null;
		PrintWriter out = null;
		// Seems to fail unless we wait a short while before opening the streams
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
		}
		try {
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
			out.println("HTTP/1.0 200 OK");
			out.println("Content-Type: text/plain");
			out.println();
			out.flush();
			String line;
			while((line = in.readLine()) != null){
				out.println(line);
				if (line.length()==0) break;
			}
            int c;
            while(in.ready() && (c = in.read()) != -1) {
                out.write(c);
            }
			out.flush();
		} catch (Exception e) {
			log.error("", e);
		} finally {
			JOrphanUtils.closeQuietly(out);
			JOrphanUtils.closeQuietly(in);
			JOrphanUtils.closeQuietly(clientSocket);
		}
		log.info("End of Thread");
	}

}