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

package org.apache.jmeter.util;

import java.util.Arrays;
import java.util.Properties;

import org.apache.jmeter.testbeans.TestBean;

/**
 * Parent class to handle common GUI design for BSF test elements
 */
public abstract class BSFBeanInfoSupport extends ScriptingBeanInfoSupport {

    private static final String[] LANGUAGE_TAGS;

    static {
        Properties languages = JMeterUtils.loadProperties("org/apache/bsf/Languages.properties"); // $NON-NLS-1$
        LANGUAGE_TAGS = new String[languages.size()];
        int i = 0;
        for (Object language : languages.keySet()) {
            LANGUAGE_TAGS[i++] = language.toString();
        }
        Arrays.sort(LANGUAGE_TAGS);
    }

    protected BSFBeanInfoSupport(Class<? extends TestBean> beanClass) {
        super(beanClass, LANGUAGE_TAGS);
    }

}
