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

package org.apache.jmeter.protocol.http.sampler.hc;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * {@link GZIPInputStream} subclass that has a flag to accept 
 * "edgy streams" that signal end of stream with {@link EOFException} 
 * which seems to be rather frequent
 * 
 * @see <a href="https://bz.apache.org/bugzilla/show_bug.cgi?id=61058">Bugzilla 61058</a>
 * @since 5.0
 */
public class LaxGZIPInputStream extends GZIPInputStream {
    private final boolean relax;
    
    /**
     * @param wrapped the InputStream that should be wrapped
     * @param relax flag to enable relaxed mode
     * @throws IOException when super class throws an IOException
     */
    public LaxGZIPInputStream(InputStream wrapped, boolean relax) throws IOException {
        super(wrapped);
        this.relax = relax;
    }

    /* (non-Javadoc)
     * @see org.apache.http.client.entity.DeflateInputStream#read(byte[], int, int)
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        try {
            return super.read(b, off, len);
        } catch (final EOFException ex) {
            return handleRelaxMode(ex, relax);
        }
    }

    @Override
    public int read() throws IOException {
        try {
            return super.read();
        } catch (final EOFException ex) {
            return handleRelaxMode(ex, relax);
        }
    }

    @Override
    public int read(byte[] b) throws IOException {
        try {
            return super.read(b);
        } catch (final EOFException ex) {
            return handleRelaxMode(ex, relax);
        }
    }
    
    /**
     * @param ex EOFException
     * @param relaxMode relax mode enabled
     * @return -1 if relax
     * @throws EOFException
     */
    private int handleRelaxMode(final EOFException ex, final boolean relaxMode) throws EOFException {
        if(relaxMode) {
            return -1;
        } else {
            throw ex;
        }
    }
}
