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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;

import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.testelement.property.PropertyIterator;

/**
 * Class for setting the necessary headers for a POST request, and sending the
 * body of the POST.
 */
public class PostWriter {
    
	private static final String DASH_DASH = "--";  // $NON-NLS-1$

    /** The bounday string between multiparts */
    protected final static String BOUNDARY = "---------------------------7d159c1302d0y0"; // $NON-NLS-1$

	private final static byte[] CRLF = { 0x0d, 0x0A };

	public static final String ENCODING = "ISO-8859-1"; // $NON-NLS-1$

    /** The form data that is going to be sent as url encoded */
    private byte[] formDataUrlEncoded;    
    /** The form data that is going to be sent in post body */
    private byte[] formDataPostBody;
    /** The start of the file multipart to be sent */
    private byte[] formDataFileStartMultipart;
    /** The boundary string for multipart */
    private String boundary;
    
    /**
     * Constructor for PostWriter.
     * Uses the PostWriter.BOUNDARY as the boundary string
     * 
     */
    public PostWriter() {
        this(BOUNDARY);
    }

    /**
     * Constructor for PostWriter
     * 
     * @param boundary the boundary string to use as marker between multipart parts
     */
    public PostWriter(String boundary) {
        this.boundary = boundary;
    }

	/**
	 * Send POST data from Entry to the open connection.
     * 
     * @return the post body sent. Actual file content is not returned, it
     * is just shown as a placeholder text "actual file content"
	 */
	public String sendPostData(URLConnection connection, HTTPSampler sampler) throws IOException {
        // Buffer to hold the post body, except file content
        StringBuffer postedBody = new StringBuffer(1000);
        
        // Check if we should do a multipart/form-data or an
        // application/x-www-form-urlencoded post request
        if(sampler.getUseMultipartForPost()) {
            OutputStream out = connection.getOutputStream();
            
            // Write the form data post body, which we have constructed
            // in the setHeaders. This contains the multipart start divider
            // and any form data, i.e. arguments
            out.write(formDataPostBody);
            // We get the posted bytes as UTF-8, since java is using UTF-8
            postedBody.append(new String(formDataPostBody, "UTF-8")); // $NON-NLS-1$
            
            // Add any files
            if(sampler.hasUploadableFiles()) {
                // First write the start multipart file
                out.write(formDataFileStartMultipart);
                // We get the posted bytes as UTF-8, since java is using UTF-8
                postedBody.append(new String(formDataFileStartMultipart, "UTF-8")); // $NON-NLS-1$
                
                // Write the actual file content
                writeFileToStream(sampler.getFilename(), out);
                // We just add placeholder text for file content
                postedBody.append("<actual file content, not shown here>"); // $NON-NLS-1$

                // Write the end of multipart file
                byte[] fileMultipartEndDivider = getFileMultipartEndDivider(); 
                out.write(fileMultipartEndDivider);
                // We get the posted bytes as UTF-8, since java is using UTF-8
                postedBody.append(new String(fileMultipartEndDivider, "UTF-8")); // $NON-NLS-1$
            }

            // Write end of multipart
            byte[] multipartEndDivider = getMultipartEndDivider(); 
            out.write(multipartEndDivider);
            // We get the posted bytes as UTF-8, since java is using UTF-8
            postedBody.append(new String(multipartEndDivider, "UTF-8")); // $NON-NLS-1$

            out.flush();
            out.close();
        }
        else {
            // If there are no arguments, we can send a file as the body of the request
            if(sampler.getArguments() != null && sampler.getArguments().getArgumentCount() == 0 && sampler.getSendFileAsPostBody()) {
                OutputStream out = connection.getOutputStream();
                writeFileToStream(sampler.getFilename(), out);
                out.flush();
                out.close();

                // We just add placeholder text for file content
                postedBody.append("<actual file content, not shown here>"); // $NON-NLS-1$
            }
            else {            
                // In an application/x-www-form-urlencoded request, we only support
                // parameters, no file upload is allowed
                OutputStream out = connection.getOutputStream();
                out.write(formDataUrlEncoded);
                out.flush();
                out.close();

                // We get the posted bytes as UTF-8, since java is using UTF-8
                postedBody.append(new String(formDataUrlEncoded, "UTF-8")); // $NON-NLS-1$
            }            
        }
        return postedBody.toString();
	}
    
    public void setHeaders(URLConnection connection, HTTPSampler sampler) throws IOException {
    	// Get the encoding to use for the request
        String contentEncoding = sampler.getContentEncoding();
        if(contentEncoding == null || contentEncoding.length() == 0) {
            contentEncoding = ENCODING;
        }
        long contentLength = 0L;
    	
        // Check if we should do a multipart/form-data or an
        // application/x-www-form-urlencoded post request
        if(sampler.getUseMultipartForPost()) {
            // Set the content type
            connection.setRequestProperty(
                HTTPSamplerBase.HEADER_CONTENT_TYPE,
                HTTPSamplerBase.MULTIPART_FORM_DATA + "; boundary=" + getBoundary()); // $NON-NLS-1$
            
            // Write the form section
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            // First the multipart start divider
            bos.write(getMultipartDivider());
            // Add any parameters
            PropertyIterator args = sampler.getArguments().iterator();
            while (args.hasNext()) {
                HTTPArgument arg = (HTTPArgument) args.next().getObjectValue();
                // End the previous multipart
                bos.write(CRLF);
                // Write multipart for parameter
                writeFormMultipart(bos, arg.getName(), arg.getValue(), contentEncoding);
            }
            // If there are any files, we need to end the previous multipart
            if(sampler.hasUploadableFiles()) {
                // End the previous multipart
                bos.write(CRLF);
            }
            bos.flush();
            // Keep the content, will be sent later
            formDataPostBody = bos.toByteArray();
            bos.close();
            contentLength = formDataPostBody.length;

            // Now we just construct any multipart for the files
            // We only construct the file multipart start, we do not write
            // the actual file content
            if(sampler.hasUploadableFiles()) {
                bos = new ByteArrayOutputStream();
                // Write multipart for file
                writeStartFileMultipart(bos, sampler.getFilename(), sampler.getFileField(), sampler.getMimetype());
                bos.flush();
                formDataFileStartMultipart = bos.toByteArray();
                bos.close();
                contentLength += formDataFileStartMultipart.length;
                // Add also the length of the file content
                File uploadFile = new File(sampler.getFilename());
                contentLength += uploadFile.length();
                // And the end of the file multipart
                contentLength += getFileMultipartEndDivider().length;
            }

            // Add the end of multipart
            contentLength += getMultipartEndDivider().length;

            // Set the content length
            connection.setRequestProperty(HTTPSamplerBase.HEADER_CONTENT_LENGTH, Long.toString(contentLength));

            // Make the connection ready for sending post data
            connection.setDoOutput(true);
            connection.setDoInput(true);
        }
        else {
            // Check if the header manager had a content type header
            // This allows the user to specify his own content-type for a POST request
            String contentTypeHeader = connection.getRequestProperty(HTTPSamplerBase.HEADER_CONTENT_TYPE);
            boolean hasContentTypeHeader = contentTypeHeader != null && contentTypeHeader.length() > 0; 
            
            // If there are no arguments, we can send a file as the body of the request
            if(sampler.getArguments() != null && sampler.getArguments().getArgumentCount() == 0 && sampler.getSendFileAsPostBody()) {
                if(!hasContentTypeHeader) {
                    // Allow the mimetype of the file to control the content type
                    if(sampler.getMimetype() != null && sampler.getMimetype().length() > 0) {
                        connection.setRequestProperty(HTTPSamplerBase.HEADER_CONTENT_TYPE, sampler.getMimetype());
                    }
                    else {
                        connection.setRequestProperty(HTTPSamplerBase.HEADER_CONTENT_TYPE, HTTPSamplerBase.APPLICATION_X_WWW_FORM_URLENCODED);
                    }
                }
                
                // Create the content length we are going to write
                File inputFile = new File(sampler.getFilename());
                contentLength = inputFile.length();
            }
            else {
                // Set the content type
                if(!hasContentTypeHeader) {
                    connection.setRequestProperty(HTTPSamplerBase.HEADER_CONTENT_TYPE, HTTPSamplerBase.APPLICATION_X_WWW_FORM_URLENCODED);
                }
                
                // We create the post body content now, so we know the size
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                
                // If none of the arguments have a name specified, we
                // just send all the values as the post body
                String postBody = null;
                if(!sampler.getSendParameterValuesAsPostBody()) {
                    // It is a normal post request, with parameter names and values
                    postBody = sampler.getQueryString(contentEncoding);
                }
                else {
                    // Just append all the parameter values, and use that as the post body
                    StringBuffer postBodyBuffer = new StringBuffer();
                    PropertyIterator args = sampler.getArguments().iterator();
                    while (args.hasNext()) {
                        HTTPArgument arg = (HTTPArgument) args.next().getObjectValue();
                        postBodyBuffer.append(arg.getValue());
                    }
                    postBody = postBodyBuffer.toString();
                }

                // Query string should be encoded in UTF-8
                bos.write(postBody.getBytes("UTF-8")); // $NON-NLS-1$
                bos.flush();
                bos.close();

                // Keep the content, will be sent later
                formDataUrlEncoded = bos.toByteArray();
                contentLength = bos.toByteArray().length;
            }
            
            // Set the content length
            connection.setRequestProperty(HTTPSamplerBase.HEADER_CONTENT_LENGTH, Long.toString(contentLength));

            // Make the connection ready for sending post data
            connection.setDoOutput(true);
        }
    }
    
    /**
     * Get the boundary string, used to separate multiparts
     * 
     * @return the boundary string
     */
    protected String getBoundary() {
        return boundary;
    }

    /**
     * Get the bytes used to separate multiparts
     * 
     * @return the bytes used to separate multiparts
     * @throws IOException
     */
    private byte[] getMultipartDivider() throws IOException {
        return new String(DASH_DASH + getBoundary()).getBytes(ENCODING);
    }

    /**
     * Get the bytes used to end a file multipat
     * 
     * @return the bytes used to end a file multipart
     * @throws IOException
     */
    private byte[] getFileMultipartEndDivider() throws IOException{
        byte[] ending = new String(DASH_DASH + getBoundary()).getBytes(ENCODING);
        byte[] completeEnding = new byte[ending.length + CRLF.length];
        System.arraycopy(CRLF, 0, completeEnding, 0, CRLF.length);
        System.arraycopy(ending, 0, completeEnding, CRLF.length, ending.length);
        return completeEnding;
    }

    /**
     * Get the bytes used to end the multipart request
     * 
     * @return the bytes used to end the multipart request
     * @throws IOException
     */
    private byte[] getMultipartEndDivider() throws IOException{
        byte[] ending = DASH_DASH.getBytes(ENCODING);
        byte[] completeEnding = new byte[ending.length + CRLF.length];
        System.arraycopy(ending, 0, completeEnding, 0, ending.length);
        System.arraycopy(CRLF, 0, completeEnding, ending.length, CRLF.length);
        return completeEnding;
    }

    /**
     * Write the start of a file multipart, up to the point where the
     * actual file content should be written
     */
	private void writeStartFileMultipart(OutputStream out, String filename,
            String nameField, String mimetype)
            throws IOException {
        write(out, "Content-Disposition: form-data; name=\""); // $NON-NLS-1$
        write(out, nameField);
        write(out, "\"; filename=\"");// $NON-NLS-1$
        write(out, (new File(filename).getName()));
        writeln(out, "\""); // $NON-NLS-1$
        writeln(out, "Content-Type: " + mimetype); // $NON-NLS-1$
        writeln(out, "Content-Transfer-Encoding: binary"); // $NON-NLS-1$
        out.write(CRLF);
    }

    /**
     * Write the content of a file to the output stream
     * 
     * @param filename the filename of the file to write to the stream
     * @param out the stream to write to
     * @throws IOException
     */
    private void writeFileToStream(String filename, OutputStream out) throws IOException {
        byte[] buf = new byte[1024];
        // 1k - the previous 100k made no sense (there's tons of buffers
        // elsewhere in the chain) and it caused OOM when many concurrent
        // uploads were being done. Could be fixed by increasing the evacuation
        // ratio in bin/jmeter[.bat], but this is better.
        InputStream in = new BufferedInputStream(new FileInputStream(filename));
        int read;
        try {
            while ((read = in.read(buf)) > 0) {
                out.write(buf, 0, read);
            }
        }
        finally {
            in.close();
        }
    }

	/**
	 * Writes form data in multipart format.
	 */
	private void writeFormMultipart(OutputStream out, String name, String value, String charSet)
		throws IOException {
		writeln(out, "Content-Disposition: form-data; name=\"" + name + "\""); // $NON-NLS-1$ // $NON-NLS-2$
        writeln(out, "Content-Type: text/plain; charset=" + charSet); // $NON-NLS-1$
        writeln(out, "Content-Transfer-Encoding: 8bit"); // $NON-NLS-1$
        
		out.write(CRLF);
		out.write(value.getBytes(charSet));
		out.write(CRLF);
        // Write boundary end marker
        out.write(getMultipartDivider());
	}

    private void write(OutputStream out, String value) 
    throws UnsupportedEncodingException, IOException 
    {
    	out.write(value.getBytes(ENCODING)); 
    }
	

	private void writeln(OutputStream out, String value)
	throws UnsupportedEncodingException, IOException
	{
		out.write(value.getBytes(ENCODING));
		out.write(CRLF);
	}
}
