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
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sample TCPClient implementation.
 * Reads data until the defined EOL byte is reached.
 * If there is no EOL byte defined, then reads until
 * the end of the stream is reached.
 * The EOL byte is defined by the property "tcp.eolByte".
 */
public class TCPClientImpl extends AbstractTCPClient {
    private static final Logger log = LoggerFactory.getLogger(TCPClientImpl.class);

    private static final int EOL_INT = JMeterUtils.getPropDefault("tcp.eolByte", 1000); // $NON-NLS-1$
    private static final String CHARSET = JMeterUtils.getPropDefault("tcp.charset", Charset.defaultCharset().name()); // $NON-NLS-1$
    // default is not in range of a byte

    public TCPClientImpl() {
        super();
        setEolByte(EOL_INT);
        if (useEolByte) {
            log.info("Using eolByte={}", eolByte);
        }
        setCharset(CHARSET);
        String configuredCharset = JMeterUtils.getProperty("tcp.charset");
        if(StringUtils.isEmpty(configuredCharset)) {
            log.info("Using platform default charset:{}",CHARSET);
        } else {
            log.info("Using charset:{}", configuredCharset);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(OutputStream os, String s)  throws IOException{
        if(log.isDebugEnabled()) {
            log.debug("WriteS: {}", showEOL(s));
        }
        os.write(s.getBytes(CHARSET));
        os.flush();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(OutputStream os, InputStream is) throws IOException{
        byte[] buff = new byte[512];
        while(is.read(buff) > 0){
            if(log.isDebugEnabled()) {
                log.debug("WriteIS: {}", showEOL(new String(buff, CHARSET)));
            }
            os.write(buff);
            os.flush();
        }
    }

    @Deprecated
    public String read(InputStream is) throws ReadException {
        return read(is, new SampleResult());
    }

    /**
     * Reads data until the defined EOL byte is reached.
     * If there is no EOL byte defined, then reads until
     * the end of the stream is reached.
     */
    @Override
    public String read(InputStream is, SampleResult sampleResult) throws ReadException{
        ByteArrayOutputStream w = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[4096];
            int x;
            boolean first = true;
            while ((x = is.read(buffer)) > -1) {
                if (first) {
                    sampleResult.latencyEnd();
                    first = false;
                }
                w.write(buffer, 0, x);
                if (useEolByte && (buffer[x - 1] == eolByte)) {
                    break;
                }
            }

            // do we need to close byte array (or flush it?)
            if(log.isDebugEnabled()) {
                log.debug("Read: {}\n{}", w.size(), w.toString());
            }
            return w.toString(CHARSET);
        } catch (IOException e) {
            throw new ReadException("Error reading from server, bytes read: " + w.size(), e, w.toString());
        }
    }

    private String showEOL(final String input) {
        StringBuilder sb = new StringBuilder(input.length()*2);
        for(int i=0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (ch < ' ') {
                sb.append('[');
                sb.append((int)ch);
                sb.append(']');
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
}
