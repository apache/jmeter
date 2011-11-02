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

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @version $Revision$
 */
public class StatusImpl implements Status {
    private Jvm jvm = null;

    private String connectorPrefix = null;

    private final List<Connector> connectors;

    /**
     *
     */
    public StatusImpl() {
        super();
        connectors = new LinkedList<Connector>();
    }

    /** {@inheritDoc} */
    public Jvm getJvm() {
        return jvm;
    }

    /** {@inheritDoc} */
    public void setJvm(Jvm vm) {
        this.jvm = vm;
    }

    /** {@inheritDoc} */
    public List<Connector> getConnector() {
        return this.connectors;
    }

    public void addConnector(Connector conn) {
        this.connectors.add(conn);
    }

    public void setConnectorPrefix(String prefix) {
        connectorPrefix = prefix;
    }

    public String getConnectorPrefix(){
        return connectorPrefix;
    }
}
