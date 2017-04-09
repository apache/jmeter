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

package org.apache.jmeter.visualizers;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.UnsharedComponent;
import org.apache.jmeter.gui.util.HeaderAsPropertyRenderer;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.reflect.Functor;

public class PropertyControlGui extends AbstractConfigGui implements
        ActionListener, UnsharedComponent {

    private static final long serialVersionUID = 1L;

    private static final String COLUMN_NAMES_0 = "name"; // $NON-NLS-1$

    private static final String COLUMN_NAMES_1 = "value"; // $NON-NLS-1$

    private static final String SYSTEM = "system"; // $NON-NLS-1$

    private static final String JMETER = "jmeter"; // $NON-NLS-1$

    private final JCheckBox systemButton = new JCheckBox("System");

    private final JCheckBox jmeterButton = new JCheckBox("JMeter");

    private final JLabel tableLabel = new JLabel("Properties");

    /** The table containing the list of arguments. */
    private transient JTable table;

    /** The model for the arguments table. */
    protected transient ObjectTableModel tableModel;

    public PropertyControlGui() {
        super();
        init();
    }

    @Override
    public String getLabelResource() {
        return "property_visualiser_title"; // $NON-NLS-1$
    }

    @Override
    public Collection<String> getMenuCategories() {
        return Arrays.asList(MenuFactory.NON_TEST_ELEMENTS);
    }

    @Override
    public void actionPerformed(ActionEvent action) {
        String command = action.getActionCommand();
        
        if (SYSTEM.equals(command)){
            setUpData();
            return;
        }
        else if (JMETER.equals(command)){
            setUpData();
            return;
        }

    }

    @Override
    public TestElement createTestElement() {
        TestElement el = new ConfigTestElement();
        modifyTestElement(el);
        return el;
    }
    
    @Override
    public void configure(TestElement element) {
        super.configure(element);
        setUpData();
    }

    private void setUpData(){
        tableModel.clearData();
        Properties p = null;
        if (systemButton.isSelected()){
            p = System.getProperties();
        }
        if (jmeterButton.isSelected()) {
            p = JMeterUtils.getJMeterProperties();
        }
        if (p == null) {
            return;
        }
        Set<Map.Entry<Object, Object>> s = p.entrySet();
        List<Map.Entry<Object, Object>> al = new ArrayList<>(s);
        Collections.sort(al, new Comparator<Map.Entry<Object, Object>>(){
            @Override
            public int compare(Map.Entry<Object, Object> o1, Map.Entry<Object, Object> o2) {
                String m1 = (String) o1.getKey();
                String m2 = (String) o2.getKey();
                return m1.compareTo(m2);
            }
        });
        for (Map.Entry<Object, Object> row : al) {
            tableModel.addRow(row);
        }

    }

    @Override
    public void modifyTestElement(TestElement element) {
        configureTestElement(element);
    }

    private Component makeMainPanel() {
        initializeTableModel();
        table = new JTable(tableModel);
        table.getTableHeader().setDefaultRenderer(new HeaderAsPropertyRenderer());
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JMeterUtils.applyHiDPI(table);
        return makeScrollPane(table);
    }

    /**
     * Create a panel containing the title label for the table.
     *
     * @return a panel containing the title label
     */
    private Component makeLabelPanel() {
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        ButtonGroup bg = new ButtonGroup();
        bg.add(systemButton);
        bg.add(jmeterButton);
        jmeterButton.setSelected(true);
        systemButton.setActionCommand(SYSTEM);
        jmeterButton.setActionCommand(JMETER);
        systemButton.addActionListener(this);
        jmeterButton.addActionListener(this);

        labelPanel.add(systemButton);
        labelPanel.add(jmeterButton);
        labelPanel.add(tableLabel);
        return labelPanel;
    }


    /**
     * Initialize the components and layout of this component.
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);

        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(makeLabelPanel(), BorderLayout.NORTH);
        p.add(makeMainPanel(), BorderLayout.CENTER);
        // Force a minimum table height of 70 pixels
        p.add(Box.createVerticalStrut(70), BorderLayout.WEST);

        add(p, BorderLayout.CENTER);
        table.revalidate();
    }
    
    private void initializeTableModel() {
        tableModel = new ObjectTableModel(new String[] { COLUMN_NAMES_0, COLUMN_NAMES_1 },
                new Functor[] {
                    new Functor(Map.Entry.class, "getKey"), // $NON-NLS-1$
                    new Functor(Map.Entry.class, "getValue") // $NON-NLS-1$
                },
                new Functor[] {
                    null, // $NON-NLS-1$
                    new Functor(Map.Entry.class,"setValue", new Class[] { Object.class }) // $NON-NLS-1$
                },
                new Class[] { String.class, String.class });
    }
}
