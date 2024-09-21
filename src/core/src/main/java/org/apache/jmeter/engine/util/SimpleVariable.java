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

package org.apache.jmeter.engine.util;

import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

public class SimpleVariable {

    private String name;

    public SimpleVariable(String name) {
        this.name = name;
    }

    public SimpleVariable() {
        this.name = ""; //$NON-NLS-1$
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @see org.apache.jmeter.functions.Function#execute
     */
    @Override
    public String toString() {
        String ret = null;
        JMeterVariables vars = getVariables();

        if (vars != null) {
            ret = vars.get(name);
        }

        if (ret == null) {
            return "${" + name + "}";
        }

        return ret;
    }

    private static JMeterVariables getVariables() {
        JMeterContext context = JMeterContextService.getContext();
        return context.getVariables();
    }

}
