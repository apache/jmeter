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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
/*
import org.apache.jmeter.engine.util.ValueReplacer;
import org.apache.jmeter.protocol.http.control.HttpMirrorControl;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler2;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.util.EncoderCache;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
*/
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.extensions.TestSetup;

/**
 * Class for testing the HTTPMirrorThread, which is handling the
 * incoming requests for the HTTPMirrorServer
 */
public class TestHTTPMirrorThread extends TestCase {
    /** The encodings used for http headers and control information */
    private final static String ISO_8859_1 = "ISO-8859-1"; // $NON-NLS-1$
    private final static String UTF_8 = "UTF-8"; // $NON-NLS-1$

    private static final byte[] CRLF = { 0x0d, 0x0a };
    private final static int HTTP_SERVER_PORT = 8181;

    public TestHTTPMirrorThread(String arg0) {
        super(arg0);
    }
    
    public static Test suite(){
        TestSetup setup = new TestSetup(new TestSuite(TestHTTPMirrorThread.class)){         
            private HttpMirrorServer httpServer;
            
            @Override
            protected void setUp() throws Exception {
                httpServer = startHttpMirror(HTTP_SERVER_PORT);
            }
            
            @Override
            protected void tearDown() throws Exception {
                // Shutdown the http server
                httpServer.stopServer();
                httpServer = null;
            }
        };
        return setup;
    }

    /**
     * Utility method to handle starting the HttpMirrorServer for testing.
     * Also used by TestHTTPSamplersAgainstHttpMirrorServer
     */
    public static HttpMirrorServer startHttpMirror(int port) throws Exception {
        HttpMirrorServer server = null;
        server = new HttpMirrorServer(port);
        server.start();
        Exception e = null;
        for (int i=0; i < 10; i++) {// Wait up to 1 second
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
            e = server.getException();
            if (e != null) {// Already failed
                throw new Exception("Could not start mirror server on port: "+port+". "+e);
            }
            if (server.isAlive()) {
                break; // succeeded
            }
        }
        
        if (!server.isAlive()){
            throw new Exception("Could not start mirror server on port: "+port);
        }
        return server;
    }

    public void testGetRequest() throws Exception {        
        // Connect to the http server, and do a simple http get
        Socket clientSocket = new Socket("localhost", HTTP_SERVER_PORT);
        OutputStream outputStream = clientSocket.getOutputStream();
        InputStream inputStream = clientSocket.getInputStream();
        
        // Write to the socket
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // Headers
        bos.write("GET / HTTP 1.1".getBytes(ISO_8859_1));
        bos.write(CRLF);
        bos.write("Host: localhost".getBytes(ISO_8859_1));
        bos.write(CRLF);
        bos.write(CRLF);
        bos.close();
        outputStream.write(bos.toByteArray());
        
        // Read the response
        ByteArrayOutputStream response = new ByteArrayOutputStream();        
        byte[] buffer = new byte[1024];
        int length = 0;
        while(( length = inputStream.read(buffer)) != -1) {
            response.write(buffer, 0, length);
        }
        response.close();
        byte[] mirroredResponse = getMirroredResponse(response.toByteArray());          
        // Check that the request and response matches
        checkArraysHaveSameContent(bos.toByteArray(), mirroredResponse);
        // Close the connection
        clientSocket.close();

        // Connect to the http server, and do a simple http get, with
        // a pause in the middle of transmitting the header
        clientSocket = new Socket("localhost", HTTP_SERVER_PORT);
        outputStream = clientSocket.getOutputStream();
        inputStream = clientSocket.getInputStream();
        
        // Write to the socket
        bos = new ByteArrayOutputStream();
        // Headers
        bos.write("GET / HTTP 1.1".getBytes(ISO_8859_1));
        bos.write(CRLF);
        // Write the start of the headers, and then sleep, so that the mirror
        // thread will have to block to wait for more data to appear
        bos.close();
        byte[] firstChunk = bos.toByteArray(); 
        outputStream.write(firstChunk);
        Thread.sleep(300);
        // Write the rest of the headers
        bos = new ByteArrayOutputStream();
        bos.write("Host: localhost".getBytes(ISO_8859_1));
        bos.write(CRLF);
        bos.write(CRLF);
        bos.close();
        byte[] secondChunk = bos.toByteArray(); 
        outputStream.write(secondChunk);
        // Read the response
        response = new ByteArrayOutputStream();        
        buffer = new byte[1024];
        length = 0;
        while((length = inputStream.read(buffer)) != -1) {
            response.write(buffer, 0, length);
        }
        response.close();
        mirroredResponse = getMirroredResponse(response.toByteArray());
        // The content sent
        bos = new ByteArrayOutputStream();
        bos.write(firstChunk);
        bos.write(secondChunk);
        bos.close();        
        // Check that the request and response matches
        checkArraysHaveSameContent(bos.toByteArray(), mirroredResponse);
        // Close the connection
        clientSocket.close();        
    }

    public void testPostRequest() throws Exception {
        // Connect to the http server, and do a simple http post
        Socket clientSocket = new Socket("localhost", HTTP_SERVER_PORT);
        OutputStream outputStream = clientSocket.getOutputStream();
        InputStream inputStream = clientSocket.getInputStream();
        // Construct body
        StringBuilder postBodyBuffer = new StringBuilder();
        for(int i = 0; i < 1000; i++) {
            postBodyBuffer.append("abc");
        }
        byte[] postBody = postBodyBuffer.toString().getBytes(ISO_8859_1);
        
        // Write to the socket
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // Headers
        bos.write("GET / HTTP 1.1".getBytes(ISO_8859_1));
        bos.write(CRLF);
        bos.write("Host: localhost".getBytes(ISO_8859_1));
        bos.write(CRLF);
        bos.write(("Content-type: text/plain; charset=" + ISO_8859_1).getBytes(ISO_8859_1));
        bos.write(CRLF);
        bos.write(("Content-length: " + postBody.length).getBytes(ISO_8859_1));
        bos.write(CRLF);
        bos.write(CRLF);
        bos.write(postBody);
        bos.close();
        // Write the headers and body
        outputStream.write(bos.toByteArray());
        // Read the response
        ByteArrayOutputStream response = new ByteArrayOutputStream();        
        byte[] buffer = new byte[1024];
        int length = 0;
        while((length = inputStream.read(buffer)) != -1) {
            response.write(buffer, 0, length);
        }
        response.close();
        byte[] mirroredResponse = getMirroredResponse(response.toByteArray());          
        // Check that the request and response matches
        checkArraysHaveSameContent(bos.toByteArray(), mirroredResponse);
        // Close the connection
        clientSocket.close();

        // Connect to the http server, and do a simple http post, with
        // a pause after transmitting the headers
        clientSocket = new Socket("localhost", HTTP_SERVER_PORT);
        outputStream = clientSocket.getOutputStream();
        inputStream = clientSocket.getInputStream();
        
        // Write to the socket
        bos = new ByteArrayOutputStream();
        // Headers
        bos.write("GET / HTTP 1.1".getBytes(ISO_8859_1));
        bos.write(CRLF);
        bos.write("Host: localhost".getBytes(ISO_8859_1));
        bos.write(CRLF);
        bos.write(("Content-type: text/plain; charset=" + ISO_8859_1).getBytes(ISO_8859_1));
        bos.write(CRLF);
        bos.write(("Content-length: " + postBody.length).getBytes(ISO_8859_1));
        bos.write(CRLF);
        bos.write(CRLF);
        bos.close();
        // Write the headers, and then sleep
        bos.close();
        byte[] firstChunk = bos.toByteArray(); 
        outputStream.write(firstChunk);
        Thread.sleep(300);
        
        // Write the body
        byte[] secondChunk = postBody;         
        outputStream.write(secondChunk);
        // Read the response
        response = new ByteArrayOutputStream();        
        buffer = new byte[1024];
        length = 0;
        while((length = inputStream.read(buffer)) != -1) {
            response.write(buffer, 0, length);
        }
        response.close();
        mirroredResponse = getMirroredResponse(response.toByteArray());
        // The content sent
        bos = new ByteArrayOutputStream();
        bos.write(firstChunk);
        bos.write(secondChunk);
        bos.close();        
        // Check that the request and response matches
        checkArraysHaveSameContent(bos.toByteArray(), mirroredResponse);
        // Close the connection
        clientSocket.close();
        
        // Connect to the http server, and do a simple http post with utf-8
        // encoding of the body, which caused problems when reader/writer
        // classes were used in the HttpMirrorThread
        clientSocket = new Socket("localhost", HTTP_SERVER_PORT);
        outputStream = clientSocket.getOutputStream();
        inputStream = clientSocket.getInputStream();
        // Construct body
        postBodyBuffer = new StringBuilder();
        for(int i = 0; i < 1000; i++) {
            postBodyBuffer.append("\u0364\u00c5\u2052");
        }
        postBody = postBodyBuffer.toString().getBytes(UTF_8);
        
        // Write to the socket
        bos = new ByteArrayOutputStream();
        // Headers
        bos.write("GET / HTTP 1.1".getBytes(ISO_8859_1));
        bos.write(CRLF);
        bos.write("Host: localhost".getBytes(ISO_8859_1));
        bos.write(CRLF);
        bos.write(("Content-type: text/plain; charset=" + UTF_8).getBytes(ISO_8859_1));
        bos.write(CRLF);
        bos.write(("Content-length: " + postBody.length).getBytes(ISO_8859_1));
        bos.write(CRLF);
        bos.write(CRLF);
        bos.close();
        // Write the headers, and then sleep
        bos.close();
        firstChunk = bos.toByteArray(); 
        outputStream.write(firstChunk);
        Thread.sleep(300);
        
        // Write the body
        secondChunk = postBody;         
        outputStream.write(secondChunk);
        // Read the response
        response = new ByteArrayOutputStream();        
        buffer = new byte[1024];
        length = 0;
        while((length = inputStream.read(buffer)) != -1) {
            response.write(buffer, 0, length);
        }
        response.close();
        mirroredResponse = getMirroredResponse(response.toByteArray());
        // The content sent
        bos = new ByteArrayOutputStream();
        bos.write(firstChunk);
        bos.write(secondChunk);
        bos.close();
        // Check that the request and response matches       
        checkArraysHaveSameContent(bos.toByteArray(), mirroredResponse);
        // Close the connection
        clientSocket.close();
    }
/*
    public void testPostRequestChunked() throws Exception {
        // TODO - implement testing of chunked post request
    }
*/    

    /**
     * Check that the the two byte arrays have identical content
     * 
     * @param expected
     * @param actual
     * @throws UnsupportedEncodingException 
     */
    private void checkArraysHaveSameContent(byte[] expected, byte[] actual) throws UnsupportedEncodingException {
        if(expected != null && actual != null) {
            if(expected.length != actual.length) {
                System.out.println(">>>>>>>>>>>>>>>>>>>> (expected) : length " + expected.length);
                System.out.println(new String(expected,"UTF-8"));
                System.out.println("==================== (actual) : length " + actual.length);
                System.out.println(new String(actual,"UTF-8"));
                System.out.println("<<<<<<<<<<<<<<<<<<<<");
                fail("arrays have different length, expected is " + expected.length + ", actual is " + actual.length);
            }
            else {
                for(int i = 0; i < expected.length; i++) {
                    if(expected[i] != actual[i]) {
                        System.out.println(">>>>>>>>>>>>>>>>>>>> (expected) : length " + expected.length);
                        System.out.println(new String(expected,0,i+1, ISO_8859_1));
                        System.out.println("==================== (actual) : length " + actual.length);
                        System.out.println(new String(actual,0,i+1, ISO_8859_1));
                        System.out.println("<<<<<<<<<<<<<<<<<<<<");
/*
                        // Useful to when debugging
                        for(int j = 0; j  < expected.length; j++) {
                            System.out.print(expected[j] + " ");
                        }
                        System.out.println();
                        for(int j = 0; j  < actual.length; j++) {
                            System.out.print(actual[j] + " ");
                        }
                        System.out.println();
*/                      
                        fail("byte at position " + i + " is different, expected is " + expected[i] + ", actual is " + actual[i]);
                    }
                }
            }
        }
        else {
            fail("expected or actual byte arrays were null");
        }
    }
    
    private byte[] getMirroredResponse(byte[] allResponse) {
        // The response includes the headers from the mirror server,
        // we want to skip those, to only keep the content mirrored.
        // Look for the first CRLFCRLF section
        int startOfMirrorResponse = 0;
        for(int i = 0; i < allResponse.length; i++) {
            // TODO : This is a bit fragile
            if(allResponse[i] == 0x0d && allResponse[i+1] == 0x0a && allResponse[i+2] == 0x0d && allResponse[i+3] == 0x0a) {
                startOfMirrorResponse = i + 4;
                break;
            }
        }
        byte[] mirrorResponse = new byte[allResponse.length - startOfMirrorResponse]; 
        System.arraycopy(allResponse, startOfMirrorResponse, mirrorResponse, 0, mirrorResponse.length);
        return mirrorResponse;      
    }
}
