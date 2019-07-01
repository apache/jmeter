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

package org.apache.jmeter.gui;

import java.awt.BorderLayout;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.WorkBench;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.util.JMeterUtils;

public class NamePanel extends JPanel implements JMeterGUIComponent {
    private static final long serialVersionUID = 240L;

    private static final String LABEL_RESOURCE = "root"; // $NON-NLS-1$

    /** A text field containing the name. */
    private final JTextField nameField = new JTextField(15);


   /**
     * Create a new NamePanel with the default name.
     */
    public NamePanel() {
        _setName(JMeterUtils.getResString(LABEL_RESOURCE));
        init();
    }

    /**
     * Initialize the GUI components and layout.
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(5, 0));
        // The label for the text field.
        JLabel nameLabel = new JLabel(JMeterUtils.getResString("name")); // $NON-NLS-1$
        nameLabel.setName("name");
        nameLabel.setLabelFor(nameField);

        add(nameLabel, BorderLayout.WEST);
        add(nameField, BorderLayout.CENTER);
    }

    @Override
    public void clearGui() {
        setName(getStaticLabel());
    }

    /**
     * Get the currently displayed name.
     *
     * @return the current name
     */
    @Override
    public String getName() {
        /*
         * Null check is needed because some implementations of JPanel end up calling
         * getName(). This is a bug, which has been reported as:
         * http://bugs.java.com/bugdatabase/view_bug.do?bug_id=JDK-8175888
         *
         * See also
         * https://www.securecoding.cert.org/confluence/display/java/MET05-J.+Ensure+that+constructors+do+not+call+overridable+methods
         * https://dev.eclipse.org/sonar/coding_rules#rule_key=squid%3AS1699
         */
        if (nameField != null) { // NOSONAR suppress warning as the null check is needed as per above
            return nameField.getText();
        }
        return ""; // $NON-NLS-1$
    }

    /** {@inheritDoc} */
    @Override
    public void setName(String name) {
        _setName(name);
    }

    private void _setName(String name) {
        super.setName(name);
        nameField.setText(name);
    }

    /** {@inheritDoc} */
    @Override
    public void configure(TestElement testElement) {
        setName(testElement.getName());
    }

    /** {@inheritDoc} */
    @Override
    public JPopupMenu createPopupMenu() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String getStaticLabel() {
        return JMeterUtils.getResString(getLabelResource());
    }

    /** {@inheritDoc} */
    @Override
    public String getLabelResource() {
        return LABEL_RESOURCE;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<String> getMenuCategories() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TestElement createTestElement() {
        WorkBench wb = new WorkBench();
        modifyTestElement(wb);
        return wb;
    }

    /** {@inheritDoc} */
    @Override
    public void modifyTestElement(TestElement wb) {
        wb.setName(getName());
        wb.setProperty(new StringProperty(TestElement.GUI_CLASS, this.getClass().getName()));
        wb.setProperty(new StringProperty(TestElement.TEST_CLASS, WorkBench.class.getName()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDocAnchor() {
        return null;
    }
}
