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
import java.util.ResourceBundle;

import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testbeans.gui.FileEditor;
import org.apache.jmeter.testbeans.gui.TextAreaEditor;

/**
 * Parent class to define common GUI parameters for BSF and JSR223 test elements
 */
public abstract class ScriptingBeanInfoSupport extends BeanInfoSupport {

    public ScriptingBeanInfoSupport(Class<? extends TestBean> beanClass, String[] languageTags) {
        this(beanClass, languageTags, null);
    }

    protected ScriptingBeanInfoSupport(Class<? extends TestBean> beanClass, String[] languageTags, ResourceBundle rb) {
        super(beanClass);
        PropertyDescriptor p;

        p = property("scriptLanguage"); // $NON-NLS-1$
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, ""); // $NON-NLS-1$
        if (rb != null) {
            p.setValue(RESOURCE_BUNDLE, rb);
        }
        p.setValue(TAGS, languageTags);

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
        
        // the cache key property was kept in the JSR223TestElement 
        //  for compatibility with jmeter <= 2.13
        // but it should be removed in a future version
        // mark the property as 'hidden' to remove it from the gui
        if (JSR223TestElement.class.isAssignableFrom(beanClass) ) {
            p = property("cacheKey");
            if(p != null) {
                p.setHidden(true);
            }
        }
    }

}
