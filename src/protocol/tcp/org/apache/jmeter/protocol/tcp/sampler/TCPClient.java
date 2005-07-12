// $Header$
/*
 * Copyright 2003-2004 The Apache Software Foundation.
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
 * Created on 24-Sep-2003
 *
 * Interface for generic TCP protocol handler 
 * 
 */
package org.apache.jmeter.protocol.tcp.sampler;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @version $Revision$ $Date$
 */
public interface TCPClient {
	void setupTest();

	void teardownTest();

	/**
	 * 
	 * @param os -
	 *            OutputStream for socket
	 * @param is -
	 *            InputStream to be written to Socket
	 */
	void write(OutputStream os, InputStream is);

	/**
	 * 
	 * @param os -
	 *            OutputStream for socket
	 * @param s -
	 *            String to write
	 */
	void write(OutputStream os, String s);

	/**
	 * 
	 * @param is -
	 *            InputStream for socket
	 * @return String read from socket
	 */
	String read(InputStream is);

	/**
	 * @return Returns the eolByte.
	 */
	public byte getEolByte();

	/**
	 * @param eolByte
	 *            The eolByte to set.
	 */
	public void setEolByte(byte eolByte);
}