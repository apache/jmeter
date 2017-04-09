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

package org.apache.jmeter.protocol.mongodb.sampler;

import org.apache.jmeter.protocol.mongodb.config.MongoSourceElement;
import org.apache.jmeter.protocol.mongodb.mongo.EvalResultHandler;
import org.apache.jmeter.protocol.mongodb.mongo.MongoDB;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DB;

/**
 */
public class MongoScriptSampler
    extends AbstractSampler
        implements TestBean {

    private static final long serialVersionUID = -7789012234636439896L;

    private static final Logger log = LoggerFactory.getLogger(MongoScriptSampler.class);

    public final static String SOURCE = "MongoScriptSampler.source"; //$NON-NLS-1$

    public final static String DATABASE = "MongoScriptSampler.database"; //$NON-NLS-1$
    public final static String USERNAME = "MongoScriptSampler.username"; //$NON-NLS-1$
    public final static String PASSWORD = "MongoScriptSampler.password"; //$NON-NLS-1$
    public final static String SCRIPT = "MongoScriptSampler.script"; //$NON-NLS-1$


    public MongoScriptSampler() {
        trace("MongoScriptSampler()");
    }

    @Override
    public SampleResult sample(Entry e) {
        trace("sample()");

        SampleResult res = new SampleResult();
        String data = getScript();

        res.setSampleLabel(getTitle());
        res.setResponseCodeOK();
        res.setResponseCode("200"); // $NON-NLS-1$
        res.setSuccessful(true);
        res.setResponseMessageOK();
        res.setSamplerData(data);
        res.setDataType(SampleResult.TEXT);
        res.setContentType("text/plain"); // $NON-NLS-1$
        res.sampleStart();

        try {
            MongoDB mongoDB = MongoSourceElement.getMongoDB(getSource());
            MongoScriptRunner runner = new MongoScriptRunner();
            DB db = mongoDB.getDB(getDatabase(), getUsername(), getPassword());
            res.latencyEnd();
            Object result = runner.evaluate(db, data);
            EvalResultHandler handler = new EvalResultHandler();
            String resultAsString = handler.handle(result);
            res.setResponseData(resultAsString.getBytes());
        } catch (Exception ex) {
            res.setResponseCode("500"); // $NON-NLS-1$
            res.setSuccessful(false);
            res.setResponseMessage(ex.toString());
            res.setResponseData(ex.getMessage().getBytes());
        } finally {
            res.sampleEnd();
        }
        return res;
    }

    public String getTitle() {
        return this.getName();
    }

    public String getScript() {
        return getPropertyAsString(SCRIPT);
    }

    public void setScript(String script) {
        setProperty(SCRIPT, script);
    }

    public String getDatabase() {
        return getPropertyAsString(DATABASE);
    }

    public void setDatabase(String database) {
        setProperty(DATABASE, database);
    }

    public String getUsername() {
        return getPropertyAsString(USERNAME);
    }

    public void setUsername(String username) {
        setProperty(USERNAME, username);
    }

    public String getPassword() {
        return getPropertyAsString(PASSWORD);
    }

    public void setPassword(String password) {
        setProperty(PASSWORD, password);
    }

    public String getSource() {
        return getPropertyAsString(SOURCE);
    }

    public void setSource(String source) {
        setProperty(SOURCE, source);
    }

    /*
    * Helper
    */
    private void trace(String s) {
        if(log.isDebugEnabled()) {
            log.debug(Thread.currentThread().getName() + " (" + getTitle() + " " + s + " " + this.toString());
        }
    }
}
