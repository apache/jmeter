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
 */
package org.apache.jmeter.monitor.model;

// For unit tests, @see TestObjectFactory

import org.apache.jmeter.monitor.parser.Parser;
import org.apache.jmeter.monitor.parser.ParserImpl;
import org.apache.jmeter.samplers.SampleResult;

/**
 * ObjectFactory is a simple factory class which creates new instances of
 * objects. It also provides convienant method to parse XML status results.
 */
public class ObjectFactory {

    private static class ObjectFactoryHolder {
        static final ObjectFactory FACTORY = new ObjectFactory();
      }

    private final Parser PARSER;

    /**
     *
     */
    protected ObjectFactory() {
        super();
        PARSER = new MonitorParser(this);
    }

    public static ObjectFactory getInstance() {
        return ObjectFactoryHolder.FACTORY;
    }

    public Status parseBytes(byte[] bytes) {
        return PARSER.parseBytes(bytes);
    }

    public Status parseString(String content) {
        return PARSER.parseString(content);
    }

    public Status parseSampleResult(SampleResult result) {
        return PARSER.parseSampleResult(result);
    }

    public Status createStatus() {
        return new StatusImpl();
    }

    public Connector createConnector() {
        return new ConnectorImpl();
    }

    public Jvm createJvm() {
        return new JvmImpl();
    }

    public Memory createMemory() {
        return new MemoryImpl();
    }

    public RequestInfo createRequestInfo() {
        return new RequestInfoImpl();
    }

    public ThreadInfo createThreadInfo() {
        return new ThreadInfoImpl();
    }

    public Worker createWorker() {
        return new WorkerImpl();
    }

    public Workers createWorkers() {
        return new WorkersImpl();
    }

    protected static class MonitorParser extends ParserImpl {
        public MonitorParser(ObjectFactory factory) {
            super(factory);
        }
    }
}
