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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;

/**
 * HTTP Sampler which can read from file: URLs
 */
public class HTTPFileImpl extends HTTPAbstractImpl {

    protected HTTPFileImpl(HTTPSamplerBase base) {
        super(base);
    }

    public boolean interrupt() {
        return false;
    }

    @Override
    protected HTTPSampleResult sample(URL url, String method,
            boolean areFollowingRedirect, int frameDepth) {

        HTTPSampleResult res = new HTTPSampleResult();
        res.setHTTPMethod(GET); // Dummy
        res.setURL(url);
        res.setSampleLabel(url.toString());
        InputStream is = null;
        res.sampleStart();
        try {
            byte[] responseData;
            URLConnection conn = url.openConnection();
            is = conn.getInputStream();
            responseData = IOUtils.toByteArray(is);
            res.sampleEnd();
            res.setResponseData(responseData);
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
        } catch (FileNotFoundException e) {
            return errorResult(e, res);
        } catch (IOException e) {
            return errorResult(e, res);
        } finally {
            IOUtils.closeQuietly(is);
        }

    }
}
