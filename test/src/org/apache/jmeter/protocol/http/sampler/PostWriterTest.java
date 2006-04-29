/*
 * Copyright 2005,2006 The Apache Software Foundation.
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

package org.apache.jmeter.protocol.http.sampler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.util.HTTPArgument;

public class PostWriterTest extends TestCase {
    
    private final static byte[] CRLF = { 0x0d, 0x0A };
    
    private PostWriter postWriter;
    private URLConnection connection;
    private HTTPSampler sampler;
    private File temporaryFile;
    
    protected void setUp() throws Exception {
        postWriter = new PostWriter();
        connection = new StubURLConnection("http://fake_url/test");
        sampler = new HTTPSampler();// This must be the original (Java) HTTP sampler
        
        // create a temporary file to make sure we always have a file to give to the PostWriter 
        // Whereever we are or Whatever the current path is.
        temporaryFile = File.createTempFile("foo", "txt");
        OutputStream output = new FileOutputStream(temporaryFile);
        output.write("foo content".getBytes());
        output.flush();
        output.close();
    }

    protected void tearDown() throws Exception {
        // delete temporay file
        temporaryFile.delete();
    }

    /*
     * Test method for 'org.apache.jmeter.protocol.http.sampler.PostWriter.sendPostData(URLConnection, HTTPSampler)'
     */
    public void testSendPostData() throws IOException {
        setupFilename(sampler);
        setupCommons(sampler);
        
        postWriter.sendPostData(connection, sampler);
        
        assertEquals(createExpectedOutputStream().toString(), connection.getOutputStream().toString());
    }

    /*
     * Test method for 'org.apache.jmeter.protocol.http.sampler.PostWriter.sendPostData(URLConnection, HTTPSampler)'
     */
    public void testSendPostData_NoFilename() throws IOException {
        setupNoFilename(sampler);
        setupCommons(sampler);

        postWriter.sendPostData(connection, sampler);
        
        assertEquals("title=mytitle&description=mydescription", connection.getOutputStream().toString());
    }

    /*
     * Test method for 'org.apache.jmeter.protocol.http.sampler.PostWriter.setHeaders(URLConnection, HTTPSampler)'
     */
    public void testSetHeaders() throws IOException {
        setupFilename(sampler);
        setupCommons(sampler);
        
        postWriter.setHeaders(connection, sampler);
        
        assertEquals("multipart/form-data; boundary=" + PostWriter.BOUNDARY, connection.getRequestProperty("Content-Type"));
    }

    /*
     * Test method for 'org.apache.jmeter.protocol.http.sampler.PostWriter.setHeaders(URLConnection, HTTPSampler)'
     */
    public void testSetHeaders_NoFilename() throws IOException {
        setupNoFilename(sampler);
        setupCommons(sampler);
        
        postWriter.setHeaders(connection, sampler);
        
        assertEquals("application/x-www-form-urlencoded", connection.getRequestProperty("Content-Type"));
        assertEquals("39", connection.getRequestProperty("Content-Length"));
    }

    /**
     * setup commons parts of HTTPSampler with a no filename.
     *  
     * @param httpSampler
     * @throws IOException
     */
    private void setupNoFilename(HTTPSampler httpSampler) {
        httpSampler.setFilename("");
        httpSampler.setMimetype("application/octet-stream");
    }

    /**
     * setup commons parts of HTTPSampler with a filename.
     * 
     * @param httpSampler
     * @throws IOException
     */
    private void setupFilename(HTTPSampler httpSampler) {
        // httpSampler.setFilename("test/src/org/apache/jmeter/protocol/http/sampler/foo.txt");
        httpSampler.setFilename(temporaryFile.getAbsolutePath());
        httpSampler.setMimetype("text/plain");
    }

    /**
     * setup commons parts of HTTPSampler form test* methods.
     * 
     * @param httpSampler
     * @throws IOException
     */
    private void setupCommons(HTTPSampler httpSampler) {
        httpSampler.setFileField("upload");
        Arguments args = new Arguments();
        args.addArgument(new HTTPArgument("title", "mytitle"));
        args.addArgument(new HTTPArgument("description", "mydescription"));
        httpSampler.setArguments(args);
    }
    
    /**
     * Create the expected output with CRLF. 
     */
    private OutputStream createExpectedOutputStream() throws IOException {
        /*
        -----------------------------7d159c1302d0y0
        Content-Disposition: form-data; name="title"

        mytitle
        -----------------------------7d159c1302d0y0
        Content-Disposition: form-data; name="description"

        mydescription
        -----------------------------7d159c1302d0y0
        Content-Disposition: form-data; name="upload"; filename="test/src/org/apache/jmeter/protocol/http/sampler/foo.txt"
        Content-Type: plain/text

        foo content
        -----------------------------7d159c1302d0y0--
        */

        final OutputStream output = new ByteArrayOutputStream();
        output.write("-----------------------------7d159c1302d0y0".getBytes());
        output.write(CRLF);
        output.write("Content-Disposition: form-data; name=\"title\"".getBytes());
        output.write(CRLF);
        output.write(CRLF);
        output.write("mytitle".getBytes());
        output.write(CRLF);
        output.write("-----------------------------7d159c1302d0y0".getBytes());
        output.write(CRLF);
        output.write("Content-Disposition: form-data; name=\"description\"".getBytes());
        output.write(CRLF);
        output.write(CRLF);
        output.write("mydescription".getBytes());
        output.write(CRLF);
        output.write("-----------------------------7d159c1302d0y0".getBytes());
        output.write(CRLF);
        // replace all backslash with double backslash
        String filename = temporaryFile.getAbsolutePath().replaceAll("\\\\","\\\\\\\\");
        output.write(("Content-Disposition: form-data; name=\"upload\"; filename=\"" + filename + "\"").getBytes());
        output.write(CRLF);
        output.write("Content-Type: text/plain".getBytes());
        output.write(CRLF);
        output.write(CRLF);
        output.write("foo content".getBytes());
        output.write(CRLF);
        output.write("-----------------------------7d159c1302d0y0--".getBytes());
        output.write(CRLF);
        output.flush();
        output.close();
        return output;
    }

    /**
     * Mock an HttpURLConnection.
     * extends HttpURLConnection instead of just URLConnection because there is a cast in PostWriter.
     */
    private static class StubURLConnection extends HttpURLConnection {
        private OutputStream output = new ByteArrayOutputStream();
        private Map properties = new HashMap();
        
        public StubURLConnection(String url) throws MalformedURLException {
            super(new URL(url));
        }

        public void connect() throws IOException {
        }
        
        public OutputStream getOutputStream() throws IOException {
            return output;
        }

        public void disconnect() {
        }

        public boolean usingProxy() {
            return false;
        }

        public String getRequestProperty(String key) {
            return (String) properties.get(key);
        }

        public void setRequestProperty(String key, String value) {
            properties.put(key, value);
        }
    }
}
