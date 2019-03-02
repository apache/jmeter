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

package org.apache.jmeter.protocol.jdbc.processor;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.jmeter.protocol.jdbc.AbstractJDBCTestElement;
import org.apache.jmeter.protocol.jdbc.config.DataSourceElement;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * As pre- and post-processors essentially do the same this class provides the implementation.
 */
public abstract class AbstractJDBCProcessor extends AbstractJDBCTestElement {

    private static final Logger log = LoggerFactory.getLogger(AbstractJDBCProcessor.class);

    private static final long serialVersionUID = 233L;

    /**
     * Calls the JDBC code to be executed.
     */
    protected void process() {
        if(JOrphanUtils.isBlank(getDataSource())) {
            throw new IllegalArgumentException("Variable Name must not be null in "+getName());
        }
        try (Connection conn = DataSourceElement.getConnection(getDataSource())){
            execute(conn);
        } catch (SQLException ex) {
            log.warn("SQL Problem in {}: {}", getName(), ex.toString());
        } catch (IOException ex) {
            log.warn("IO Problem in {}: {}"+ getName(), ex.toString());
        } catch (UnsupportedOperationException ex) {
            log.warn("Execution Problem in {}: {}", getName(), ex.toString());
        }
    }

}
