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
import java.nio.charset.StandardCharsets;

import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jorphan.util.JOrphanUtils;

/**
 * Class for setting the necessary headers for a POST request, and sending the
 * body of the POST.
 */
public class PostWriter {

    private static final String DASH_DASH = "--";  // $NON-NLS-1$
    private static final byte[] DASH_DASH_BYTES = {'-', '-'};

    /** The boundary string between multiparts */
    protected static final String BOUNDARY = "---------------------------7d159c1302d0y0"; // $NON-NLS-1$

    private static final byte[] CRLF = { 0x0d, 0x0A };

    public static final String ENCODING = StandardCharsets.ISO_8859_1.name();

    /** The form data that is going to be sent as url encoded */
    protected byte[] formDataUrlEncoded;
    /** The form data that is going to be sent in post body */
    protected byte[] formDataPostBody;
    /** The boundary string for multipart */
    private final String boundary;

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
     * @param connection
     *            the open connection to use for sending data
     * @param sampler
     *            sampler to get information about what to send
     * @return the post body sent. Actual file content is not returned, it is
     *         just shown as a placeholder text "actual file content"
     * @throws IOException when writing data fails
     */
    public String sendPostData(URLConnection connection, HTTPSamplerBase sampler) throws IOException {
        // Buffer to hold the post body, except file content
        StringBuilder postedBody = new StringBuilder(1000);

        HTTPFileArg[] files = sampler.getHTTPFiles();

        String contentEncoding = sampler.getContentEncoding();
        if(contentEncoding == null || contentEncoding.length() == 0) {
            contentEncoding = ENCODING;
        }

        // Check if we should do a multipart/form-data or an
        // application/x-www-form-urlencoded post request
        if(sampler.getUseMultipartForPost()) {
            OutputStream out = connection.getOutputStream();

            // Write the form data post body, which we have constructed
            // in the setHeaders. This contains the multipart start divider
            // and any form data, i.e. arguments
            out.write(formDataPostBody);
            // Retrieve the formatted data using the same encoding used to create it
            postedBody.append(new String(formDataPostBody, contentEncoding));

            // Add any files
            for (int i=0; i < files.length; i++) {
                HTTPFileArg file = files[i];
                // First write the start multipart file
                byte[] header = file.getHeader().getBytes();  // TODO - charset?
                out.write(header);
                // Retrieve the formatted data using the same encoding used to create it
                postedBody.append(new String(header)); // TODO - charset?
                // Write the actual file content
                writeFileToStream(file.getPath(), out);
                // We just add placeholder text for file content
                postedBody.append("<actual file content, not shown here>"); // $NON-NLS-1$
                // Write the end of multipart file
                byte[] fileMultipartEndDivider = getFileMultipartEndDivider();
                out.write(fileMultipartEndDivider);
                // Retrieve the formatted data using the same encoding used to create it
                postedBody.append(new String(fileMultipartEndDivider, ENCODING));
                if(i + 1 < files.length) {
                    out.write(CRLF);
                    postedBody.append(new String(CRLF, SampleResult.DEFAULT_HTTP_ENCODING));
                }
            }
            // Write end of multipart
            byte[] multipartEndDivider = getMultipartEndDivider();
            out.write(multipartEndDivider);
            postedBody.append(new String(multipartEndDivider, ENCODING));

            out.flush();
            out.close();
        }
        else {
            // If there are no arguments, we can send a file as the body of the request
            if(sampler.getArguments() != null && !sampler.hasArguments() && sampler.getSendFileAsPostBody()) {
                OutputStream out = connection.getOutputStream();
                // we're sure that there is at least one file because of
                // getSendFileAsPostBody method's return value.
                HTTPFileArg file = files[0];
                writeFileToStream(file.getPath(), out);
                out.flush();
                out.close();

                // We just add placeholder text for file content
                postedBody.append("<actual file content, not shown here>"); // $NON-NLS-1$
            }
            else if (formDataUrlEncoded != null){ // may be null for PUT
                // In an application/x-www-form-urlencoded request, we only support
                // parameters, no file upload is allowed
                OutputStream out = connection.getOutputStream();
                out.write(formDataUrlEncoded);
                out.flush();
                out.close();

                postedBody.append(new String(formDataUrlEncoded, contentEncoding));
            }
        }
        return postedBody.toString();
    }

    public void setHeaders(URLConnection connection, HTTPSamplerBase sampler) throws IOException {
        // Get the encoding to use for the request
        String contentEncoding = sampler.getContentEncoding();
        if(contentEncoding == null || contentEncoding.length() == 0) {
            contentEncoding = ENCODING;
        }
        long contentLength = 0L;
        HTTPFileArg[] files = sampler.getHTTPFiles();

        // Check if we should do a multipart/form-data or an
        // application/x-www-form-urlencoded post request
        if(sampler.getUseMultipartForPost()) {
            // Set the content type
            connection.setRequestProperty(
                    HTTPConstants.HEADER_CONTENT_TYPE,
                    HTTPConstants.MULTIPART_FORM_DATA + "; boundary=" + getBoundary()); // $NON-NLS-1$

            // Write the form section
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            // First the multipart start divider
            bos.write(getMultipartDivider());
            // Add any parameters
            for (JMeterProperty jMeterProperty : sampler.getArguments()) {
                HTTPArgument arg = (HTTPArgument) jMeterProperty.getObjectValue();
                String parameterName = arg.getName();
                if (arg.isSkippable(parameterName)) {
                    continue;
                }
                // End the previous multipart
                bos.write(CRLF);
                // Write multipart for parameter
                writeFormMultipart(bos, parameterName, arg.getValue(), contentEncoding, sampler.getDoBrowserCompatibleMultipart());
            }
            // If there are any files, we need to end the previous multipart
            if(files.length > 0) {
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
            for (int i=0; i < files.length; i++) {
                HTTPFileArg file = files[i];
                // Write multipart for file
                bos = new ByteArrayOutputStream();
                writeStartFileMultipart(bos, file.getPath(), file.getParamName(), file.getMimeType());
                bos.flush();
                String header = bos.toString(contentEncoding);// TODO is this correct?
                // If this is not the first file we can't write its header now
                // for simplicity we always save it, even if there is only one file
                file.setHeader(header);
                bos.close();
                contentLength += header.length();
                // Add also the length of the file content
                File uploadFile = new File(file.getPath());
                contentLength += uploadFile.length();
                // And the end of the file multipart
                contentLength += getFileMultipartEndDivider().length;
                if(i+1 < files.length) {
                    contentLength += CRLF.length;
                }
            }

            // Add the end of multipart
            contentLength += getMultipartEndDivider().length;

            // Set the content length
            connection.setRequestProperty(HTTPConstants.HEADER_CONTENT_LENGTH, Long.toString(contentLength));

            // Make the connection ready for sending post data
            connection.setDoOutput(true);
            connection.setDoInput(true);
        }
        else {
            // Check if the header manager had a content type header
            // This allows the user to specify his own content-type for a POST request
            String contentTypeHeader = connection.getRequestProperty(HTTPConstants.HEADER_CONTENT_TYPE);
            boolean hasContentTypeHeader = contentTypeHeader != null && contentTypeHeader.length() > 0;

            // If there are no arguments, we can send a file as the body of the request
            if(sampler.getArguments() != null && sampler.getArguments().getArgumentCount() == 0 && sampler.getSendFileAsPostBody()) {
                // we're sure that there is one file because of
                // getSendFileAsPostBody method's return value.
                HTTPFileArg file = files[0];
                if(!hasContentTypeHeader) {
                    // Allow the mimetype of the file to control the content type
                    if(file.getMimeType() != null && file.getMimeType().length() > 0) {
                        connection.setRequestProperty(HTTPConstants.HEADER_CONTENT_TYPE, file.getMimeType());
                    }
                    else {
                        connection.setRequestProperty(HTTPConstants.HEADER_CONTENT_TYPE, HTTPConstants.APPLICATION_X_WWW_FORM_URLENCODED);
                    }
                }
                // Create the content length we are going to write
                File inputFile = new File(file.getPath());
                contentLength = inputFile.length();
            }
            else {
                // We create the post body content now, so we know the size
                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                // If none of the arguments have a name specified, we
                // just send all the values as the post body
                String postBody = null;
                if(!sampler.getSendParameterValuesAsPostBody()) {
                    // Set the content type
                    if(!hasContentTypeHeader) {
                        connection.setRequestProperty(HTTPConstants.HEADER_CONTENT_TYPE, HTTPConstants.APPLICATION_X_WWW_FORM_URLENCODED);
                    }

                    // It is a normal post request, with parameter names and values
                    postBody = sampler.getQueryString(contentEncoding);
                }
                else {
                    // Allow the mimetype of the file to control the content type
                    // This is not obvious in GUI if you are not uploading any files,
                    // but just sending the content of nameless parameters
                    // TODO: needs a multiple file upload scenerio
                    if(!hasContentTypeHeader) {
                        HTTPFileArg file = files.length > 0? files[0] : null;
                        if(file != null && file.getMimeType() != null && file.getMimeType().length() > 0) {
                            connection.setRequestProperty(HTTPConstants.HEADER_CONTENT_TYPE, file.getMimeType());
                        }
                        else {
                            // TODO: is this the correct default?
                            connection.setRequestProperty(HTTPConstants.HEADER_CONTENT_TYPE, HTTPConstants.APPLICATION_X_WWW_FORM_URLENCODED);
                        }
                    }

                    // Just append all the parameter values, and use that as the post body
                    StringBuilder postBodyBuffer = new StringBuilder();
                    for (JMeterProperty jMeterProperty : sampler.getArguments()) {
                        HTTPArgument arg = (HTTPArgument) jMeterProperty.getObjectValue();
                        postBodyBuffer.append(arg.getEncodedValue(contentEncoding));
                    }
                    postBody = postBodyBuffer.toString();
                }

                bos.write(postBody.getBytes(contentEncoding));
                bos.flush();
                bos.close();

                // Keep the content, will be sent later
                formDataUrlEncoded = bos.toByteArray();
                contentLength = bos.toByteArray().length;
            }

            // Set the content length
            connection.setRequestProperty(HTTPConstants.HEADER_CONTENT_LENGTH, Long.toString(contentLength));

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
     * Encoded using ENCODING
     *
     * @return the bytes used to separate multiparts
     * @throws IOException
     */
    private byte[] getMultipartDivider() throws IOException {
        return (DASH_DASH + getBoundary()).getBytes(ENCODING);
    }

    /**
     * Get the bytes used to end a file multipart
     * Encoded using ENCODING
     *
     * @return the bytes used to end a file multipart
     * @throws IOException
     */
    private byte[] getFileMultipartEndDivider() throws IOException{
        byte[] ending = getMultipartDivider();
        byte[] completeEnding = new byte[ending.length + CRLF.length];
        System.arraycopy(CRLF, 0, completeEnding, 0, CRLF.length);
        System.arraycopy(ending, 0, completeEnding, CRLF.length, ending.length);
        return completeEnding;
    }

    /**
     * Get the bytes used to end the multipart request
     *
     * @return the bytes used to end the multipart request
     */
    private byte[] getMultipartEndDivider(){
        byte[] ending = DASH_DASH_BYTES;
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
        write(out, new File(filename).getName());
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
    private static void writeFileToStream(String filename, OutputStream out) throws IOException {
        byte[] buf = new byte[1024];
        // 1k - the previous 100k made no sense (there's tons of buffers
        // elsewhere in the chain) and it caused OOM when many concurrent
        // uploads were being done. Could be fixed by increasing the evacuation
        // ratio in bin/jmeter[.bat], but this is better.
        InputStream in = new BufferedInputStream(new FileInputStream(filename));
        int read;
        boolean noException = false;
        try { 
            while ((read = in.read(buf)) > 0) {
                out.write(buf, 0, read);
            }
            noException = true;
        }
        finally {
            if(!noException) {
                // Exception in progress
                JOrphanUtils.closeQuietly(in);
            } else {
                in.close();
            }
        }
    }

    /**
     * Writes form data in multipart format.
     */
    private void writeFormMultipart(OutputStream out, String name, String value, String charSet, 
            boolean browserCompatibleMultipart)
        throws IOException {
        writeln(out, "Content-Disposition: form-data; name=\"" + name + "\""); // $NON-NLS-1$ // $NON-NLS-2$
        if (!browserCompatibleMultipart){
            writeln(out, "Content-Type: text/plain; charset=" + charSet); // $NON-NLS-1$
            writeln(out, "Content-Transfer-Encoding: 8bit"); // $NON-NLS-1$
        }
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
