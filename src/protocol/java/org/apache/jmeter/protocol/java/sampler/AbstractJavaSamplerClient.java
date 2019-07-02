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

package org.apache.jmeter.protocol.java.sampler;

import org.apache.jmeter.config.Arguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract implementation of the JavaSamplerClient interface. This
 * implementation provides default implementations of most of the methods in the
 * interface, as well as some convenience methods, in order to simplify
 * development of JavaSamplerClient implementations.
 * <p>
 * See {@link org.apache.jmeter.protocol.java.test.SleepTest} for an example of
 * how to extend this class.
 * <p>
 * While it may be necessary to make changes to the JavaSamplerClient interface
 * from time to time (therefore requiring changes to any implementations of this
 * interface), we intend to make this abstract class provide reasonable
 * implementations of any new methods so that subclasses do not necessarily need
 * to be updated for new versions. Therefore, when creating a new
 * JavaSamplerClient implementation, developers are encouraged to subclass this
 * abstract class rather than implementing the JavaSamplerClient interface
 * directly. Implementing JavaSamplerClient directly will continue to be
 * supported for cases where extending this class is not possible (for example,
 * when the client class is already a subclass of some other class).
 * <p>
 * The runTest() method of JavaSamplerClient does not have a default
 * implementation here, so subclasses must define at least this method. It may
 * be useful to override other methods as well.
 *
 * @see JavaSamplerClient#runTest(JavaSamplerContext)
 *
 */
public abstract class AbstractJavaSamplerClient implements JavaSamplerClient {
    private static final Logger log = LoggerFactory.getLogger(AbstractJavaSamplerClient.class);

    @SuppressWarnings("deprecation") // will be removed in 3.3
    private static final org.apache.log.Logger oldLogger = org.apache.jorphan.logging.LoggingManager.getLoggerForClass();

    /* Implements JavaSamplerClient.setupTest(JavaSamplerContext) */
    @Override
    public void setupTest(JavaSamplerContext context) {
        log.debug(getClass().getName() + ": setupTest");
    }

    /* Implements JavaSamplerClient.teardownTest(JavaSamplerContext) */
    @Override
    public void teardownTest(JavaSamplerContext context) {
        log.debug(getClass().getName() + ": teardownTest");
    }

    /* Implements JavaSamplerClient.getDefaultParameters() */
    @Override
    public Arguments getDefaultParameters() {
        return null;
    }

    /**
     * Get a Logger instance which can be used by subclasses to log information.
     * This is the same Logger which is used by the base JavaSampler classes
     * (jmeter.protocol.java).
     *
     * @return a Logger instance which can be used for logging
     * @deprecated Will be removed in 3.3, use {@link AbstractJavaSamplerClient#getNewLogger()}
     */
    @Deprecated
    protected org.apache.log.Logger getLogger() {
        return oldLogger;
    }

    /**
     * Get a Logger instance which can be used by subclasses to log information.
     * This is the same Logger which is used by the base JavaSampler classes
     * (jmeter.protocol.java).
     *
     * @return {@link Logger}  instance which can be used for logging
     */
    protected Logger getNewLogger() {
        return log;
    }
}
