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
package org.apache.jmeter.report.engine;

import java.io.Serializable;

import org.apache.jmeter.engine.JMeterEngineException;
import org.apache.jorphan.collections.HashTree;

public class StandardReportEngine implements Runnable, Serializable,
        ReportEngine {

    private static final long serialVersionUID = 240L;

    /**
     *
     */
    public StandardReportEngine() {
        super();
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.report.engine.ReportEngine#configure(org.apache.jorphan.collections.HashTree)
     */
    @Override
    public void configure(HashTree testPlan) {
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.report.engine.ReportEngine#runReport()
     */
    @Override
    public void runReport() throws JMeterEngineException {
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.report.engine.ReportEngine#stopReport()
     */
    @Override
    public void stopReport() {
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.report.engine.ReportEngine#reset()
     */
    @Override
    public void reset() {
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.report.engine.ReportEngine#exit()
     */
    @Override
    public void exit() {
    }

}
