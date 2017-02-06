/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.jmeter.protocol.http.sampler;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;

import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.Cookie;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Selector for the AJP/1.3 protocol
 * (i.e. what Tomcat uses with mod_jk)
 * It allows you to test Tomcat in AJP mode without
 * actually having Apache installed and configured
 *
 */
public class AjpSampler extends HTTPSamplerBase implements Interruptible {

    private static final long serialVersionUID = 234L;

    private static final Logger log= LoggerFactory.getLogger(AjpSampler.class);

    private static final char NEWLINE = '\n';
    private static final String COLON_SPACE = ": ";//$NON-NLS-1$

    /**
     *  Translates integer codes to request header names
     */
    private static final String[] HEADER_TRANS_ARRAY = {
        "accept",               //$NON-NLS-1$
        "accept-charset",       //$NON-NLS-1$
        "accept-encoding",      //$NON-NLS-1$
        "accept-language",      //$NON-NLS-1$
        "authorization",        //$NON-NLS-1$
        "connection",           //$NON-NLS-1$
        "content-type",         //$NON-NLS-1$
        "content-length",       //$NON-NLS-1$
        "cookie",               //$NON-NLS-1$
        "cookie2",              //$NON-NLS-1$
        "host",                 //$NON-NLS-1$
        "pragma",               //$NON-NLS-1$
        "referer",              //$NON-NLS-1$
        "user-agent"            //$NON-NLS-1$
    };

    /**
     * Base value for translated headers
     */
    static final int AJP_HEADER_BASE = 0xA000;

    static final int MAX_SEND_SIZE = 8*1024 - 4 - 4;

    private transient Socket channel = null;
    private transient Socket activeChannel = null;
    private int lastPort = -1;
    private String lastHost = null;
    private String localName = null;
    private String localAddress = null;
    private final byte [] inbuf = new byte[8*1024];
    private final byte [] outbuf = new byte[8*1024];
    private final transient ByteArrayOutputStream responseData = new ByteArrayOutputStream();
    private int inpos = 0;
    private int outpos = 0;
    private transient String stringBody = null;
    private transient InputStream body = null;

    public AjpSampler() {
    }

    @Override
    protected HTTPSampleResult sample(URL url,
                       String method,
                       boolean frd,
                       int fd) {
        HTTPSampleResult res = new HTTPSampleResult();
        res.setSampleLabel(url.toExternalForm());
        res.sampleStart();
        try {
            setupConnection(url, method, res);
            activeChannel = channel;
            execute(method, res);
            res.sampleEnd();
            res.setResponseData(responseData.toByteArray());
            return res;
        } catch(IOException iex) {
            res.sampleEnd();
            lastPort = -1; // force reopen on next sample
            channel = null;
            return errorResult(iex, res);
        } finally {
            activeChannel = null;
            JOrphanUtils.closeQuietly(body);
            body = null;
        }
    }

    @Override
    public void threadFinished() {
        if(channel != null) {
            try {
                channel.close();
            } catch(IOException iex) {
            log.debug("Error closing channel",iex);
            }
        }
        channel = null;
        JOrphanUtils.closeQuietly(body);
        body = null;
        stringBody = null;
    }

    private void setupConnection(URL url,
                 String method,
                 HTTPSampleResult res) throws IOException {

        String host = url.getHost();
        int port = url.getPort();
        if(port <= 0 || port == url.getDefaultPort()) {
            port = 8009;
        }
        String scheme = url.getProtocol();
        if(channel == null || !host.equals(lastHost) || port != lastPort) {
            if(channel != null) {
            channel.close();
            }
            channel = new Socket(host, port);
            int timeout = JMeterUtils.getPropDefault("httpclient.timeout",0);//$NON-NLS-1$
            if(timeout > 0) {
                channel.setSoTimeout(timeout);
            }
            localAddress = channel.getLocalAddress().getHostAddress();
            localName = channel.getLocalAddress().getHostName();
            lastHost = host;
            lastPort = port;
        }
        res.setURL(url);
        res.setHTTPMethod(method);
        outpos = 4;
        setByte((byte)2);
        if(method.equals(HTTPConstants.POST)) {
            setByte((byte)4);
        } else {
            setByte((byte)2);
        }
        if(JMeterUtils.getPropDefault("httpclient.version","1.1").equals("1.0")) {//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
            setString("HTTP/1.0");//$NON-NLS-1$
        } else {
            setString(HTTPConstants.HTTP_1_1);
        }
        setString(url.getPath());
        setString(localAddress);
        setString(localName);
        setString(host);
        setInt(url.getDefaultPort());
        setByte(HTTPConstants.PROTOCOL_HTTPS.equalsIgnoreCase(scheme) ? (byte)1 : (byte)0);
        setInt(getHeaderSize(method, url));
        String hdr = setConnectionHeaders(url, host, method);
        res.setRequestHeaders(hdr);
        res.setCookies(setConnectionCookies(url, getCookieManager()));
        String query = url.getQuery();
        if (query != null) {
            setByte((byte)0x05); // Marker for query string attribute
            setString(query);
        }
        setByte((byte)0xff); // More general attributes not supported
    }

    private int getHeaderSize(String method, URL url) {
        HeaderManager headers = getHeaderManager();
        CookieManager cookies = getCookieManager();
        AuthManager auth = getAuthManager();
        int hsz = 1; // Host always
        if(method.equals(HTTPConstants.POST)) {
            HTTPFileArg[] hfa = getHTTPFiles();
            if(hfa.length > 0) {
                hsz += 3;
            } else {
                hsz += 2;
            }
        }
        if(headers != null) {
            hsz += headers.size();
        }
        if(cookies != null) {
            hsz += cookies.getCookieCount();
        }
        if(auth != null) {
                String authHeader = auth.getAuthHeaderForURL(url);
            if(authHeader != null) {
            ++hsz;
            }
        }
        return hsz;
    }


    private String setConnectionHeaders(URL url, String host, String method)
    throws IOException {
        HeaderManager headers = getHeaderManager();
        AuthManager auth = getAuthManager();
        StringBuilder hbuf = new StringBuilder();
        // Allow Headers to override Host setting
        hbuf.append("Host").append(COLON_SPACE).append(host).append(NEWLINE);//$NON-NLS-1$
        setInt(0xA00b); //Host
        setString(host);
        if(headers != null) {
            for (JMeterProperty jMeterProperty : headers.getHeaders()) {
                Header header = (Header) jMeterProperty.getObjectValue();
                String n = header.getName();
                String v = header.getValue();
                hbuf.append(n).append(COLON_SPACE).append(v).append(NEWLINE);
                int hc = translateHeader(n);
                if(hc > 0) {
                    setInt(hc+AJP_HEADER_BASE);
                } else {
                    setString(n);
                }
                setString(v);
            }
        }
        if(method.equals(HTTPConstants.POST)) {
            int cl = -1;
            HTTPFileArg[] hfa = getHTTPFiles();
            if(hfa.length > 0) {
                HTTPFileArg fa = hfa[0];
                String fn = fa.getPath();
                File input = new File(fn);
                cl = (int)input.length();
                if(body != null) {
                    JOrphanUtils.closeQuietly(body);
                    body = null;
                }
                body = new BufferedInputStream(new FileInputStream(input));
                setString(HTTPConstants.HEADER_CONTENT_DISPOSITION);
                setString("form-data; name=\""+encode(fa.getParamName())+
                      "\"; filename=\"" + encode(fn) +"\""); //$NON-NLS-1$ //$NON-NLS-2$
                String mt = fa.getMimeType();
                hbuf.append(HTTPConstants.HEADER_CONTENT_TYPE).append(COLON_SPACE).append(mt).append(NEWLINE);
                setInt(0xA007); // content-type
                setString(mt);
            } else {
                hbuf.append(HTTPConstants.HEADER_CONTENT_TYPE).append(COLON_SPACE).append(HTTPConstants.APPLICATION_X_WWW_FORM_URLENCODED).append(NEWLINE);
                setInt(0xA007); // content-type
                setString(HTTPConstants.APPLICATION_X_WWW_FORM_URLENCODED);
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                for (JMeterProperty arg : getArguments()) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append('&');
                    }
                    sb.append(arg.getStringValue());
                }
                stringBody = sb.toString();
                byte [] sbody = stringBody.getBytes(); // TODO - charset?
                cl = sbody.length;
                body = new ByteArrayInputStream(sbody);
            }
            hbuf.append(HTTPConstants.HEADER_CONTENT_LENGTH).append(COLON_SPACE).append(String.valueOf(cl)).append(NEWLINE);
            setInt(0xA008); // Content-length
            setString(String.valueOf(cl));
        }
        if(auth != null) {
            String authHeader = auth.getAuthHeaderForURL(url);
            if(authHeader != null) {
                setInt(0xA005); // Authorization
                setString(authHeader);
                hbuf.append(HTTPConstants.HEADER_AUTHORIZATION).append(COLON_SPACE).append(authHeader).append(NEWLINE);
            }
        }
        return hbuf.toString();
    }

    private String encode(String value)  {
        StringBuilder newValue = new StringBuilder();
        char[] chars = value.toCharArray();
        for (char c : chars) {
            if (c == '\\')//$NON-NLS-1$
            {
                newValue.append("\\\\");//$NON-NLS-1$
            } else {
                newValue.append(c);
            }
        }
        return newValue.toString();
    }

    private String setConnectionCookies(URL url, CookieManager cookies) {
        String cookieHeader = null;
        if(cookies != null) {
            cookieHeader = cookies.getCookieHeaderForURL(url);
            for (JMeterProperty jMeterProperty : cookies.getCookies()) {
                Cookie cookie = (Cookie)(jMeterProperty.getObjectValue());
                setInt(0xA009); // Cookie
                setString(cookie.getName()+"="+cookie.getValue());//$NON-NLS-1$
            }
        }
        return cookieHeader;
    }

    private int translateHeader(String n) {
        for(int i=0; i < HEADER_TRANS_ARRAY.length; i++) {
            if(HEADER_TRANS_ARRAY[i].equalsIgnoreCase(n)) {
                return i+1;
            }
        }
        return -1;
    }

    private void setByte(byte b) {
        outbuf[outpos++] = b;
    }

    private void setInt(int n) {
        outbuf[outpos++] = (byte)((n >> 8)&0xff);
        outbuf[outpos++] = (byte) (n&0xff);
    }

    private void setString(String s) {
        if( s == null ) {
            setInt(0xFFFF);
        } else {
            int len = s.length();
            setInt(len);
            for(int i=0; i < len; i++) {
                setByte((byte)s.charAt(i));
            }
            setByte((byte)0);
        }
    }

    private void send() throws IOException {
        OutputStream os = channel.getOutputStream();
        int len = outpos;
        outpos = 0;
        setInt(0x1234);
        setInt(len-4);
        os.write(outbuf, 0, len);
    }

    private void execute(String method, HTTPSampleResult res)
    throws IOException {
        send();
        if(method.equals(HTTPConstants.POST)) {
            res.setQueryString(stringBody);
            sendPostBody();
        }
        handshake(res);
    }

    private void handshake(HTTPSampleResult res) throws IOException {
        responseData.reset();
        int msg = getMessage();
        while(msg != 5) {
            if(msg == 3) {
            int len = getInt();
                responseData.write(inbuf, inpos, len);
            } else if(msg == 4) {
                parseHeaders(res);
            } else if(msg == 6) {
                setNextBodyChunk();
                send();
            }
            msg = getMessage();
        }
    }


    private void sendPostBody() throws IOException {
        setNextBodyChunk();
        send();
    }

    private void setNextBodyChunk() throws IOException {
        int nr = 0;
        if(body != null) {
            int len = body.available();
            if(len < 0) {
                len = 0;
            } else if(len > MAX_SEND_SIZE) {
                len = MAX_SEND_SIZE;
            }
            outpos = 4;
            if(len > 0) {
                nr = body.read(outbuf, outpos+2, len);
            }
        } else {
            outpos = 4;
        }
        setInt(nr);
        outpos += nr;
    }


    private void parseHeaders(HTTPSampleResult res)
    throws IOException {
        int status = getInt();
        res.setResponseCode(Integer.toString(status));
        res.setSuccessful(200 <= status && status <= 399);
        String msg = getString();
        res.setResponseMessage(msg);
        int nh = getInt();
        StringBuilder sb = new StringBuilder();
        sb.append(HTTPConstants.HTTP_1_1 ).append(status).append(" ").append(msg).append(NEWLINE);//$NON-NLS-1$//$NON-NLS-2$
        for(int i=0; i < nh; i++) {
            String name;
            int thn = peekInt();
            if((thn & 0xff00) == AJP_HEADER_BASE) {
                name = HEADER_TRANS_ARRAY[(thn&0xff)-1];
                getInt(); // we need to use up the int now
            } else {
                name = getString();
            }
            String value = getString();
            if(HTTPConstants.HEADER_CONTENT_TYPE.equalsIgnoreCase(name)) {
                res.setContentType(value);
                res.setEncodingAndType(value);
            } else if(HTTPConstants.HEADER_SET_COOKIE.equalsIgnoreCase(name)) {
                CookieManager cookies = getCookieManager();
                if(cookies != null) {
                    cookies.addCookieFromHeader(value, res.getURL());
                }
            }
            sb.append(name).append(COLON_SPACE).append(value).append(NEWLINE);
        }
        res.setResponseHeaders(sb.toString());
    }


    private int getMessage() throws IOException {
        InputStream is = channel.getInputStream();
        inpos = 0;
        int nr = is.read(inbuf, inpos, 4);
        if(nr != 4) {
            channel.close();
            channel = null;
            throw new IOException("Connection Closed: "+nr);
        }
        getInt();
        int len = getInt();
        int toRead = len;
        int cpos = inpos;
        while(toRead > 0) {
            nr = is.read(inbuf, cpos, toRead);
            cpos += nr;
            toRead -= nr;
        }
        return getByte();
    }

    private byte getByte() {
        return inbuf[inpos++];
    }

    private int getInt() {
        int res = (inbuf[inpos++]<<8)&0xff00;
        res += inbuf[inpos++]&0xff;
        return res;
    }

    private int peekInt() {
        int res = (inbuf[inpos]<<8)&0xff00;
        res += inbuf[inpos+1]&0xff;
        return res;
    }

    private String getString() throws IOException {
        int len = getInt();
        String s = new String(inbuf, inpos, len, "iso-8859-1");//$NON-NLS-1$
        inpos+= len+1;
        return s;
    }

    @Override
    public boolean interrupt() {
        Socket chan = activeChannel;
        if (chan != null) {
            activeChannel = null;
            try {
                chan.close();
            } catch (Exception e) {
                // Ignored
            }
        }
        return chan != null;
    }
}
