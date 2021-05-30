/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.protocol.http.sampler;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.Collections;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.CONCURRENT)
public class TestHTTPHC4Impl {
    private JMeterContext jmctx;
    private JMeterVariables jmvars;
    private static final String SAME_USER = "__jmv_SAME_USER";

    @BeforeEach
    public void setUp() {
        jmctx = JMeterContextService.getContext();
        jmvars = new JMeterVariables();
    }

    @Test
    void testParameterWithMimeTypeWithCharset() throws Exception {
        HTTPSamplerBase sampler = (HTTPSamplerBase) new HttpTestSampleGui().createTestElement();
        sampler.setThreadContext(jmctx);
        sampler.setDoMultipart(true);
        HttpEntityEnclosingRequestBase post = new HttpPost();
        HTTPArgument argument = new HTTPArgument("upload", "some data");
        argument.setContentType("text/html; charset=utf-8");
        sampler.getArguments().addArgument(argument);
        HTTPHC4Impl hc = new HTTPHC4Impl(sampler);
        String requestData = hc.setupHttpEntityEnclosingRequestData(post);
        assertTrue(requestData.contains("charset=utf-8"));
    }

    @Test
    void testParameterWithMultipartAndExplicitHeader() throws Exception {
        HTTPSamplerBase sampler = (HTTPSamplerBase) new HttpTestSampleGui().createTestElement();
        sampler.setThreadContext(jmctx);
        sampler.setDoMultipart(true);
        sampler.setDoBrowserCompatibleMultipart(true);
        HttpEntityEnclosingRequestBase post = new HttpPost();
        post.addHeader(HTTPConstants.HEADER_CONTENT_TYPE, "application/json");
        sampler.setHTTPFiles(new HTTPFileArg[] {new HTTPFileArg("filename", "file", "application/octect; charset=utf-8")});
        HTTPHC4Impl hc = new HTTPHC4Impl(sampler);
        String requestData = hc.setupHttpEntityEnclosingRequestData(post);
        assertEquals(0, post.getHeaders(HTTPConstants.HEADER_CONTENT_TYPE).length);
        assertTrue(requestData.contains("charset=utf-8"));
    }

    @Test
    void testFileargWithMimeTypeWithCharset() throws Exception {
        HTTPSamplerBase sampler = (HTTPSamplerBase) new HttpTestSampleGui().createTestElement();
        sampler.setThreadContext(jmctx);
        sampler.setDoMultipart(true);
        HttpEntityEnclosingRequestBase post = new HttpPost();
        HTTPFileArg fileArg = new HTTPFileArg();
        fileArg.setMimeType("text/html; charset=utf-8");
        fileArg.setName("somefile.html");
        fileArg.setParamName("upload");
        File dummyFile = File.createTempFile("somefile", ".html");
        dummyFile.deleteOnExit();
        fileArg.setPath(dummyFile.getAbsolutePath());
        sampler.setHTTPFiles(Collections.singletonList(fileArg).toArray(new HTTPFileArg[1]));
        HTTPHC4Impl hc = new HTTPHC4Impl(sampler);
        String requestData = hc.setupHttpEntityEnclosingRequestData(post);
        assertTrue(requestData.contains("charset=utf-8"));
    }

    @Test
    public void testNotifyFirstSampleAfterLoopRestartWhenThreadIterationIsSameUser() {
        jmvars.putObject(SAME_USER, true);
        jmctx.setVariables(jmvars);
        HTTPSamplerBase sampler = (HTTPSamplerBase) new HttpTestSampleGui().createTestElement();
        sampler.setThreadContext(jmctx);
        HTTPHC4Impl hc = new HTTPHC4Impl(sampler);
        hc.notifyFirstSampleAfterLoopRestart();
        assertFalse("User is the same, the state shouldn't be reset", HTTPHC4Impl.resetStateOnThreadGroupIteration.get());
    }

    @Test
    public void testNotifyFirstSampleAfterLoopRestartWhenThreadIterationIsANewUser() {
        jmvars.putObject(SAME_USER, false);
        jmctx.setVariables(jmvars);
        HTTPSamplerBase sampler = (HTTPSamplerBase) new HttpTestSampleGui().createTestElement();
        sampler.setThreadContext(jmctx);
        HTTPHC4Impl hc = new HTTPHC4Impl(sampler);
        hc.notifyFirstSampleAfterLoopRestart();
        assertTrue("Users are different, the state should be reset", HTTPHC4Impl.resetStateOnThreadGroupIteration.get());
    }
}
