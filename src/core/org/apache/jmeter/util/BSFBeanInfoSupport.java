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

import java.beans.PropertyDescriptor;
import java.util.Properties;

import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.FileEditor;
import org.apache.jmeter.testbeans.gui.TextAreaEditor;

/**
 * Parent class to handle common GUI design
 */
public abstract class BSFBeanInfoSupport extends BeanInfoSupport {

    private final static String[] LANGUAGE_TAGS;

    static {
        Properties languages = JMeterUtils.loadProperties("org/apache/bsf/Languages.properties"); // $NON-NLS-1$
        LANGUAGE_TAGS = new String[languages.size() + 1];
        int i = 0;
        for (Object language : languages.keySet()) {
            LANGUAGE_TAGS[i++] = language.toString();
        }
        LANGUAGE_TAGS[i] = "jexl";
    }

    protected BSFBeanInfoSupport(Class<?> beanClass) {
        super(beanClass);
        PropertyDescriptor p;

        p = property("scriptLanguage"); // $NON-NLS-1$
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, ""); // $NON-NLS-1$
        p.setValue(TAGS, LANGUAGE_TAGS);

        createPropertyGroup("scriptingLanguage", // $NON-NLS-1$
                new String[] { "scriptLanguage" }); // $NON-NLS-1$

        p = property("parameters"); // $NON-NLS-1$
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, ""); // $NON-NLS-1$

        createPropertyGroup("parameterGroup", // $NON-NLS-1$
                new String[] { "parameters" }); // $NON-NLS-1$

        p = property("filename"); // $NON-NLS-1$
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, ""); // $NON-NLS-1$
        p.setPropertyEditorClass(FileEditor.class);

        createPropertyGroup("filenameGroup",  // $NON-NLS-1$
                new String[] { "filename" }); // $NON-NLS-1$

        p = property("script"); // $NON-NLS-1$
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, ""); // $NON-NLS-1$
        p.setPropertyEditorClass(TextAreaEditor.class);

        createPropertyGroup("scripting", // $NON-NLS-1$
                new String[] { "script" }); // $NON-NLS-1$
    }

}
