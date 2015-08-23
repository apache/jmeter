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

package org.apache.jmeter.assertions;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;

import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TableEditor;
import org.apache.jmeter.util.JMeterUtils;

public class CompareAssertionBeanInfo extends BeanInfoSupport {

    public CompareAssertionBeanInfo() {
        super(CompareAssertion.class);
        createPropertyGroup("compareChoices", new String[] { "compareContent", "compareTime" }); //$NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$
        createPropertyGroup("comparison_filters", new String[]{"stringsToSkip"}); //$NON-NLS-1$ $NON-NLS-2$
        PropertyDescriptor p = property("compareContent"); //$NON-NLS-1$
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, Boolean.TRUE);
        p.setValue(NOT_EXPRESSION, Boolean.TRUE);
        p = property("compareTime"); //$NON-NLS-1$
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, Long.valueOf(-1));
        p.setValue(NOT_EXPRESSION, Boolean.FALSE);
        p = property("stringsToSkip"); //$NON-NLS-1$
        p.setPropertyEditorClass(TableEditor.class);
        p.setValue(TableEditor.CLASSNAME,SubstitutionElement.class.getName());
        p.setValue(TableEditor.HEADERS,new String[]{
                JMeterUtils.getResString("comparison_regex_string"), //$NON-NLS-1$
                JMeterUtils.getResString("comparison_regex_substitution")}); //$NON-NLS-1$
        p.setValue(TableEditor.OBJECT_PROPERTIES, // These are the names of the get/set methods
                new String[]{SubstitutionElement.REGEX, SubstitutionElement.SUBSTITUTE});
        p.setValue(NOT_UNDEFINED,Boolean.TRUE);
        p.setValue(DEFAULT, new ArrayList<>());
        p.setValue(MULTILINE,Boolean.TRUE);

    }

}
