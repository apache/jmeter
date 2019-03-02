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

package org.apache.jorphan.exec;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread that copies a stream in the background; closes both input and output streams.
 * @since 2.8
 */
class StreamCopier extends Thread {

    private static final Logger log = LoggerFactory.getLogger(StreamCopier.class);

    private final InputStream is;
    private final OutputStream os;

    /**
     * @param is {@link InputStream}
     * @param os {@link OutputStream}
     * @throws IOException
     */
    StreamCopier(InputStream is, OutputStream os) throws IOException {
        this.is = is;
        this.os = os;
    }

    /**
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        final boolean isSystemOutput = os.equals(System.out) || os.equals(System.err);
        try {
            IOUtils.copyLarge(is, os);
            if (!isSystemOutput){
                os.close();
            }
            is.close();
        } catch (IOException e) {
            log.warn("Error writing stream", e);
        } finally {
            IOUtils.closeQuietly(is);
            if (!isSystemOutput){
                IOUtils.closeQuietly(os);
            }
        }
    }

}
