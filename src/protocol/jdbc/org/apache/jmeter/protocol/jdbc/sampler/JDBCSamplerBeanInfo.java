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

/*
 * Created on May 16, 2004
 *
 */
package org.apache.jmeter.protocol.jdbc.sampler;

import java.beans.PropertyDescriptor;

import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TextAreaEditor;

public class JDBCSamplerBeanInfo extends BeanInfoSupport {

    /**
     *
     */
    public JDBCSamplerBeanInfo() {
        super(JDBCSampler.class);

        createPropertyGroup("varName", // $NON-NLS-1$
                new String[]{"dataSource" }); // $NON-NLS-1$

        createPropertyGroup("sql", // $NON-NLS-1$
                new String[] {
                "queryType", // $NON-NLS-1$
                "query", // $NON-NLS-1$
                "queryArguments", // $NON-NLS-1$
                "queryArgumentsTypes", // $NON-NLS-1$
                "variableNames", // $NON-NLS-1$
                "resultVariable", // $NON-NLS-1$
                });

        PropertyDescriptor p = property("dataSource"); // $NON-NLS-1$
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");

        p = property("queryArguments"); // $NON-NLS-1$
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");

        p = property("queryArgumentsTypes"); // $NON-NLS-1$
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");

        p = property("variableNames"); // $NON-NLS-1$
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");

        p = property("resultVariable"); // $NON-NLS-1$
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");

        p = property("queryType"); // $NON-NLS-1$
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, JDBCSampler.SELECT);
        p.setValue(NOT_OTHER,Boolean.TRUE);
        p.setValue(TAGS,new String[]{
                JDBCSampler.SELECT,
                JDBCSampler.UPDATE,
                JDBCSampler.CALLABLE,
                JDBCSampler.PREPARED_SELECT,
                JDBCSampler.PREPARED_UPDATE,
                JDBCSampler.COMMIT,
                JDBCSampler.ROLLBACK,
                JDBCSampler.AUTOCOMMIT_FALSE,
                JDBCSampler.AUTOCOMMIT_TRUE,
                });

        p = property("query"); // $NON-NLS-1$
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p.setPropertyEditorClass(TextAreaEditor.class);

    }
}
