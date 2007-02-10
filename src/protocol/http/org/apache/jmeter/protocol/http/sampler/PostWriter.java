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

package org.apache.jmeter.protocol.http.sampler;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLConnection;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.testelement.property.PropertyIterator;

/**
 */

public class PostWriter {
    
	private static final String DASH_DASH = "--";  // $NON-NLS-1$

    protected final static String BOUNDARY = "---------------------------7d159c1302d0y0"; // $NON-NLS-1$

	private final static byte[] CRLF = { 0x0d, 0x0A };

	protected static final String encoding = "iso-8859-1"; // $NON-NLS-1$

	// Not instantiable
	private PostWriter(){
		
	}
	/**
	 * Send POST data from Entry to the open connection.
	 */
	public static void sendPostData(URLConnection connection, HTTPSampler sampler) throws IOException {
		// If filename was specified then send the post using multipart syntax
		String filename = sampler.getFilename();
		if ((filename != null) && (filename.trim().length() > 0)) {
			OutputStream out = connection.getOutputStream();
			// Check if not using multi-part:
            if (sampler.getSendFileAsPostBody())
            {
                InputStream in = getFileStream(filename);
                byte[] buf = new byte[1024];
                int read;
                while ((read = in.read(buf)) > 0)
                {
                    out.write(buf, 0, read);
                }
                out.flush();
                in.close();
                return;
            }
			writeln(out, DASH_DASH + BOUNDARY);
			PropertyIterator args = sampler.getArguments().iterator();
			while (args.hasNext()) {
				Argument arg = (Argument) args.next().getObjectValue();
				writeFormMultipartStyle(out, arg.getName(), arg.getValue());
				writeln(out, DASH_DASH + BOUNDARY);
			}
			writeFileToURL(out, filename, sampler.getFileField(), getFileStream(filename), sampler.getMimetype());

			writeln(out, DASH_DASH + BOUNDARY + DASH_DASH);
			out.flush();
			out.close();
		}

		// No filename specified, so send the post using normal syntax
		else {
			String postData = sampler.getQueryString();
			PrintWriter out = new PrintWriter(connection.getOutputStream());
			out.print(postData);
			out.flush();
            out.close();
		}
	}

	public static void setHeaders(URLConnection connection, HTTPSampler sampler) throws IOException {
		((HttpURLConnection) connection).setRequestMethod(HTTPSamplerBase.POST);

		// If filename was specified then send the post using multipart syntax
		String filename = sampler.getFilename();
		if ((filename != null) && (filename.trim().length() > 0)) {
			if (!sampler.getSendFileAsPostBody()) { // unless the file is the body...
			    connection.setRequestProperty(HTTPSamplerBase.HEADER_CONTENT_TYPE, 
                    "multipart/form-data; boundary=" + BOUNDARY); // $NON-NLS-1$
			}
			connection.setDoOutput(true);
			connection.setDoInput(true);
		}

		// No filename specified, so send the post using normal syntax
		else {
			String postData = sampler.getQueryString();
			connection.setRequestProperty(HTTPSamplerBase.HEADER_CONTENT_LENGTH, Integer.toString(postData.length()));
			String hct= connection.getRequestProperty(HTTPSamplerBase.HEADER_CONTENT_TYPE);
			if (hct == null || hct.length() == 0) {
			    connection.setRequestProperty(HTTPSamplerBase.HEADER_CONTENT_TYPE, HTTPSamplerBase.APPLICATION_X_WWW_FORM_URLENCODED);
			}
			connection.setDoOutput(true);
		}
	}

	private static InputStream getFileStream(String filename) throws IOException {
		return new BufferedInputStream(new FileInputStream(filename));
	}

	/*
	 * NOTUSED private String getContentLength(MultipartUrlConfig config) { long
	 * size = 0; size += BOUNDARY.length() + 2; PropertyIterator iter =
	 * config.getArguments().iterator(); while (iter.hasNext()) { Argument item =
	 * (Argument) iter.next().getObjectValue(); size += item.getName().length() +
	 * item.getValue().toString().length(); size += CRLF.length * 4; size +=
	 * BOUNDARY.length() + 2; size += 39; } size += new
	 * File(config.getFilename()).length(); size += CRLF.length * 5; size +=
	 * BOUNDARY.length() + 2; size +=
	 * encode(config.getFileFieldName()).length(); size +=
	 * encode(config.getFilename()).length(); size +=
	 * config.getMimeType().length(); size += 66; size += 2 + (CRLF.length * 1);
	 * return Long.toString(size); }
	 */

	/**
	 * Writes out the contents of a file in correct multipart format.
	 */
	private static void writeFileToURL(OutputStream out, String filename,
            String fieldname, InputStream in, String mimetype)
            throws IOException {
        write(out, "Content-Disposition: form-data; name=\""); // $NON-NLS-1$
        write(out, HTTPSamplerBase.encodeBackSlashes(fieldname));
        write(out, "\"; filename=\"");// $NON-NLS-1$
        write(out, HTTPSamplerBase.encodeBackSlashes(filename));
        writeln(out, "\""); // $NON-NLS-1$
        writeln(out, "Content-Type: " + mimetype); // $NON-NLS-1$
        out.write(CRLF);

        byte[] buf = new byte[1024];
        // 1k - the previous 100k made no sense (there's tons of buffers
        // elsewhere in the chain) and it caused OOM when many concurrent
        // uploads were being done. Could be fixed by increasing the evacuation
        // ratio in bin/jmeter[.bat], but this is better.
        int read;
        while ((read = in.read(buf)) > 0) {
            out.write(buf, 0, read);
        }
        out.write(CRLF);
        in.close();
    }

	/**
	 * Writes form data in multipart format.
	 */
	private static void writeFormMultipartStyle(OutputStream out, String name, String value) throws IOException {
		writeln(out, "Content-Disposition: form-data; name=\"" + name + "\""); // $NON-NLS-1$ // $NON-NLS-2$
		out.write(CRLF);
		writeln(out, value);
	}

    private static void write(OutputStream out, String value) 
    throws UnsupportedEncodingException, IOException 
    {
    	out.write(value.getBytes(encoding)); 
    }
	

	private static void writeln(OutputStream out, String value) throws UnsupportedEncodingException, IOException {
		out.write(value.getBytes(encoding));
		out.write(CRLF);
	}
}
