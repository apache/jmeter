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

package org.apache.jmeter.control.gui;

import org.apache.jmeter.control.TransactionController;
import org.apache.jmeter.control.TransactionControllerSchema;
import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.gui.JBooleanPropertyEditor;
import org.apache.jmeter.gui.TestElementMetadata;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;

/**
 * A Transaction controller component.
 */
@GUIMenuSortOrder(2)
@TestElementMetadata(labelResource = "transaction_controller_title")
public class TransactionControllerGui extends AbstractControllerGui {

    private static final long serialVersionUID = 240L;

    /** If selected, then generate parent sample, otherwise as per original controller */
    private final JBooleanPropertyEditor generateParentSample =
            new JBooleanPropertyEditor(
                    TransactionControllerSchema.INSTANCE.getGenearteParentSample(),
                    JMeterUtils.getResString("transaction_controller_parent"));

    /** if selected, add duration of timers to total runtime */
    private final JBooleanPropertyEditor includeTimers =
            new JBooleanPropertyEditor(
                    TransactionControllerSchema.INSTANCE.getIncludeTimers(),
                    JMeterUtils.getResString("transaction_controller_include_timers"));

    /**
     * Create a new TransactionControllerGui instance.
     */
    public TransactionControllerGui() {
        init();
        bindingGroup.add(generateParentSample);
        bindingGroup.add(includeTimers);
    }

    @Override
    public TestElement makeTestElement() {
        return new TransactionController();
    }

    @Override
    public void assignDefaultValues(TestElement element) {
        super.assignDefaultValues(element);
        // See https://github.com/apache/jmeter/issues/3282
        ((TransactionController) element).setIncludeTimers(false);
    }

    @Override
    public String getLabelResource() {
        return "transaction_controller_title"; // $NON-NLS-1$
    }

    /**
     * Initialize the GUI components and layout for this component.
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new VerticalLayout(5, VerticalLayout.BOTH, VerticalLayout.TOP));
        setBorder(makeBorder());
        add(makeTitlePanel());
        add(generateParentSample);
        add(includeTimers);
    }
}
