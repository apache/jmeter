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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.util.JMeterUtils;

/**
 * HTTP Sampler which can read from file: URLs
 */
public class HTTPFileImpl extends HTTPAbstractImpl {
    private static final int MAX_BYTES_TO_STORE_PER_REQUEST =
            JMeterUtils.getPropDefault("httpsampler.max_bytes_to_store_per_request", 10 * 1024 *1024); // $NON-NLS-1$ // default value: 10MB

    protected HTTPFileImpl(HTTPSamplerBase base) {
        super(base);
    }

    @Override
    public boolean interrupt() {
        return false;
    }

    @Override
    protected HTTPSampleResult sample(URL url, String method,
            boolean areFollowingRedirect, int frameDepth) {

        HTTPSampleResult res = new HTTPSampleResult();
        res.setHTTPMethod(HTTPConstants.GET); // Dummy
        res.setURL(url);
        res.setSampleLabel(url.toString());
        InputStream is = null;
        res.sampleStart();
        int bufferSize = 4096;
        try ( org.apache.commons.io.output.ByteArrayOutputStream bos = new org.apache.commons.io.output.ByteArrayOutputStream(bufferSize) ) {
            byte[] responseData;
            URLConnection conn = url.openConnection();
            is = conn.getInputStream();
            byte[] readBuffer = new byte[bufferSize];
            int bytesReadInBuffer = 0;
            long totalBytes = 0;
            boolean storeInBOS = true;
            while ((bytesReadInBuffer = is.read(readBuffer)) > -1) {
                if(storeInBOS) {
                    if(totalBytes+bytesReadInBuffer<=MAX_BYTES_TO_STORE_PER_REQUEST) {
                        bos.write(readBuffer, 0, bytesReadInBuffer);
                    } else {
                        bos.write(readBuffer, 0, (int)(MAX_BYTES_TO_STORE_PER_REQUEST-totalBytes));
                        storeInBOS = false;
                    }
                }
                totalBytes += bytesReadInBuffer;
            }
            responseData = bos.toByteArray();
            res.sampleEnd();
            res.setResponseData(responseData);
            res.setBodySize(totalBytes);
            res.setResponseCodeOK();
            res.setResponseMessageOK();
            res.setSuccessful(true);
            StringBuilder ctb=new StringBuilder("text/html"); // $NON-NLS-1$
            // TODO can this be obtained from the file somehow?
            String contentEncoding = getContentEncoding();
            if (contentEncoding.length() > 0) {
                ctb.append("; charset="); // $NON-NLS-1$
                ctb.append(contentEncoding);
            }
            String ct = ctb.toString();
            res.setContentType(ct);
            res.setEncodingAndType(ct);

            res = resultProcessing(areFollowingRedirect, frameDepth, res);

            return res;
        } catch (IOException e) {
            return errorResult(e, res);
        } finally {
            IOUtils.closeQuietly(is);
        }

    }
}
