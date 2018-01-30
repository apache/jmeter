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

package org.apache.jmeter.protocol.http.gui;

import java.util.Iterator;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.reflect.Functor;

/**
 * A GUI panel allowing the user to enter HTTP Parameters.
 * These have names and values, as well as check-boxes to determine whether or not to
 * include the "=" sign in the output and whether or not to encode the output.
 */
public class HTTPArgumentsPanel extends ArgumentsPanel {

    private static final long serialVersionUID = 240L;

    private static final String ENCODE_OR_NOT = "encode?"; //$NON-NLS-1$

    private static final String INCLUDE_EQUALS = "include_equals"; //$NON-NLS-1$

    /** When pasting from the clipboard, split lines on linebreak or '&' */
    private static final String CLIPBOARD_LINE_DELIMITERS = "\n|&"; //$NON-NLS-1$

    /** When pasting from the clipboard, split parameters on tab or '=' */
    private static final String CLIPBOARD_ARG_DELIMITERS = "\t|="; //$NON-NLS-1$

    @Override
    protected void initializeTableModel() {
        tableModel = new ObjectTableModel(new String[] {
                ArgumentsPanel.COLUMN_RESOURCE_NAMES_0, ArgumentsPanel.COLUMN_RESOURCE_NAMES_1, ENCODE_OR_NOT, INCLUDE_EQUALS },
                HTTPArgument.class,
                new Functor[] {
                new Functor("getName"), //$NON-NLS-1$
                new Functor("getValue"), //$NON-NLS-1$
                new Functor("isAlwaysEncoded"), //$NON-NLS-1$
                new Functor("isUseEquals") }, //$NON-NLS-1$
                new Functor[] {
                new Functor("setName"), //$NON-NLS-1$
                new Functor("setValue"), //$NON-NLS-1$
                new Functor("setAlwaysEncoded"), //$NON-NLS-1$
                new Functor("setUseEquals") }, //$NON-NLS-1$
                new Class[] {String.class, String.class, Boolean.class, Boolean.class });
    }

    public static boolean testFunctors(){
        HTTPArgumentsPanel instance = new HTTPArgumentsPanel();
        instance.initializeTableModel();
        return instance.tableModel.checkFunctors(null,instance.getClass());
    }

    @Override
    protected void sizeColumns(JTable table) {
        GuiUtils.fixSize(table.getColumn(INCLUDE_EQUALS), table);
        GuiUtils.fixSize(table.getColumn(ENCODE_OR_NOT), table);
    }

    @Override
    protected HTTPArgument makeNewArgument() {
        HTTPArgument arg = new HTTPArgument("", "");
        arg.setAlwaysEncoded(false);
        arg.setUseEquals(true);
        return arg;
    }

    public HTTPArgumentsPanel() {
        super(JMeterUtils.getResString("paramtable")); //$NON-NLS-1$
        init();
    }

    @Override
    public TestElement createTestElement() {
        Arguments args = getUnclonedParameters();
        super.configureTestElement(args);
        return (TestElement) args.clone();
    }

    /**
     * Convert the argument panel contents to an {@link Arguments} collection.
     * 
     * @return a collection of {@link HTTPArgument} entries
     */
    public Arguments getParameters() {
        Arguments args = getUnclonedParameters();
        return (Arguments) args.clone();
    }

    private Arguments getUnclonedParameters() {
        stopTableEditing();
        @SuppressWarnings("unchecked") // only contains Argument (or HTTPArgument)
        Iterator<HTTPArgument> modelData = (Iterator<HTTPArgument>) tableModel.iterator();
        Arguments args = new Arguments();
        while (modelData.hasNext()) {
            HTTPArgument arg = modelData.next();
            args.addArgument(arg);
        }
        return args;
    }

    @Override
    public void configure(TestElement el) {
        super.configure(el);
        if (el instanceof Arguments) {
            tableModel.clearData();
            HTTPArgument.convertArgumentsToHTTP((Arguments) el);
            for (JMeterProperty jMeterProperty : ((Arguments) el).getArguments()) {
                HTTPArgument arg = (HTTPArgument) jMeterProperty.getObjectValue();
                tableModel.addRow(arg);
            }
        }
        checkButtonsStatus();
    }

    protected boolean isMetaDataNormal(HTTPArgument arg) {
        return arg.getMetaData() == null || arg.getMetaData().equals("=")
                || (arg.getValue() != null && arg.getValue().length() > 0);
    }

    @Override
    protected void addFromClipboard() {
        addFromClipboard(CLIPBOARD_LINE_DELIMITERS, CLIPBOARD_ARG_DELIMITERS);
    }

    @Override
    protected Argument createArgumentFromClipboard(String[] clipboardCols) {
        HTTPArgument argument = makeNewArgument();
        argument.setName(clipboardCols[0]);
        if (clipboardCols.length > 1) {
            argument.setValue(clipboardCols[1]);
            
            if (clipboardCols.length > 2) {
                
                // default to false if the string is not a boolean
                argument.setAlwaysEncoded(Boolean.parseBoolean(clipboardCols[2].trim()));
                
                if (clipboardCols.length > 3) {
                    Boolean useEqual = BooleanUtils.toBooleanObject(clipboardCols[3].trim());
                    // default to true if the string is not a boolean
                    argument.setUseEquals(useEqual!=null?useEqual.booleanValue():true);
                }
            }
        }
        
        return argument;
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        
        // register the right click menu
        JTable table = getTable();
        final JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem variabilizeItem = new JMenuItem(JMeterUtils.getResString("transform_into_variable"));
        variabilizeItem.addActionListener(e -> transformNameIntoVariable());
        popupMenu.add(variabilizeItem);
        table.setComponentPopupMenu(popupMenu);
    }
    
    /** 
     * replace the argument value of the selection with a variable 
     * the variable name is derived from the parameter name 
     */
    private void transformNameIntoVariable() {
        int[] rowsSelected = getTable().getSelectedRows();
        for (int selectedRow : rowsSelected) {
            String name = (String) tableModel.getValueAt(selectedRow, 0);
            if (StringUtils.isNotBlank(name)) {
                name = name.trim();
                name = name.replaceAll("\\$", "_");
                name = name.replaceAll("\\{", "_");
                name = name.replaceAll("\\}", "_");
                tableModel.setValueAt("${" + name + "}", selectedRow, 1);
            }
        }
    }
    
}
