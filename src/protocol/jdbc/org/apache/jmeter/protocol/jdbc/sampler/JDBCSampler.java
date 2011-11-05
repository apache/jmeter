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

package org.apache.jmeter.protocol.jdbc.sampler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.jmeter.protocol.jdbc.AbstractJDBCTestElement;
import org.apache.jmeter.protocol.jdbc.config.DataSourceElement;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * A sampler which understands JDBC database requests.
 *
 */
public class JDBCSampler extends AbstractJDBCTestElement implements Sampler, TestBean {
    
    private static final long serialVersionUID = 234L;
    
    private static final Logger log = LoggingManager.getLoggerForClass();

    /**
     * Creates a JDBCSampler.
     */
    public JDBCSampler() {
    }

    public SampleResult sample(Entry e) {
        log.debug("sampling jdbc");

        SampleResult res = new SampleResult();
        res.setSampleLabel(getName());
        res.setSamplerData(toString());
        res.setDataType(SampleResult.TEXT);
        res.setContentType("text/plain"); // $NON-NLS-1$
        res.setDataEncoding(ENCODING);

        // Assume we will be successful
        res.setSuccessful(true);
        res.setResponseMessageOK();
        res.setResponseCodeOK();


        res.sampleStart();
        Connection conn = null;

        try {

            try {
                conn = DataSourceElement.getConnection(getDataSource());
            } finally {
                res.latencyEnd(); // use latency to measure connection time
            }
            res.setResponseHeaders(conn.toString());
            res.setResponseData(execute(conn));
        } catch (SQLException ex) {
            final String errCode = Integer.toString(ex.getErrorCode());
            res.setResponseMessage(ex.toString());
            res.setResponseCode(ex.getSQLState()+ " " +errCode);
            res.setSuccessful(false);
        } catch (UnsupportedOperationException ex) {
            res.setResponseMessage(ex.toString());
            res.setResponseCode("000"); // TODO - is this correct?
            res.setSuccessful(false);
        } catch (IOException ex) {
            res.setResponseMessage(ex.toString());
            res.setResponseCode("000"); // TODO - is this correct?
            res.setSuccessful(false);
        } finally {
            close(conn);
        }

        // TODO: process warnings? Set Code and Message to success?
        res.sampleEnd();
        return res;
    }
}
