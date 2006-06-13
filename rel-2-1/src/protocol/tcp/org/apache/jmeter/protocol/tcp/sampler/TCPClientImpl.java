/*
 * Copyright 2003-2004,2006 The Apache Software Foundation.
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

/*
 * Basic TCP Sampler Client class
 * 
 * Can be used to test the TCP Sampler against an HTTP server
 * 
 * The protocol handler class name is defined by the property tcp.handler
 * 
 */
package org.apache.jmeter.protocol.tcp.sampler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * 
 * 
 */
public class TCPClientImpl implements TCPClient {
	private static Logger log = LoggingManager.getLoggerForClass();

	private byte eolByte = (byte) JMeterUtils.getPropDefault("tcp.eolByte", 0);

	public TCPClientImpl() {
		super();
		log.info("Using eolByte=" + eolByte);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.protocol.tcp.sampler.TCPClient#setupTest()
	 */
	public void setupTest() {
		log.info("setuptest");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.protocol.tcp.sampler.TCPClient#teardownTest()
	 */
	public void teardownTest() {
		log.info("teardowntest");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.protocol.tcp.sampler.TCPClient#write(java.io.OutputStream,
	 *      java.lang.String)
	 */
	public void write(OutputStream os, String s) {
		try {
			os.write(s.getBytes());
			os.flush();
		} catch (IOException e) {
			log.warn("Write error", e);
		}
		log.debug("Wrote: " + s);
		return;
	}

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.protocol.tcp.sampler.TCPClient#write(java.io.OutputStream,
     *      java.io.InputStream)
     */
    public void write(OutputStream os, InputStream is) {
        byte buff[]=new byte[512];
        try {
            while(is.read(buff) > 0){
                os.write(buff);
                os.flush();
            }
        } catch (IOException e) {
            log.warn("Write error", e);
        }
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.protocol.tcp.sampler.TCPClient#read(java.io.InputStream)
	 */
	public String read(InputStream is) {
		byte[] buffer = new byte[4096];
		ByteArrayOutputStream w = new ByteArrayOutputStream();
		int x = 0;
		try {
			while ((x = is.read(buffer)) > -1) {
				w.write(buffer, 0, x);
				if ((eolByte != 0) && (buffer[x - 1] == eolByte))
					break;
			}
			/*
			 * Timeout is reported as follows: JDK1.3: InterruptedIOException
			 * JDK1.4: SocketTimeoutException, which extends
			 * InterruptedIOException
			 * 
			 * So to make the code work on both, just check for
			 * InterruptedIOException
			 * 
			 * If 1.3 support is dropped, can change to using
			 * SocketTimeoutException
			 * 
			 * For more accurate detection of timeouts under 1.3, one could
			 * perhaps examine the Exception message text...
			 * 
			 */
		} catch (InterruptedIOException e) {
			// drop out to handle buffer
		} catch (IOException e) {
			log.warn("Read error:" + e);
			return "";
		}

		// do we need to close byte array (or flush it?)
		log.debug("Read: " + w.size() + "\n" + w.toString());
		return w.toString();
	}

	/**
	 * @return Returns the eolByte.
	 */
	public byte getEolByte() {
		return eolByte;
	}

	/**
	 * @param eolByte
	 *            The eolByte to set.
	 */
	public void setEolByte(byte eolByte) {
		this.eolByte = eolByte;
	}
}
