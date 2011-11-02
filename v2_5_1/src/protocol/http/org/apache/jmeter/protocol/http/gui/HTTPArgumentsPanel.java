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

import javax.swing.JTable;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.PropertyIterator;
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
    }

    @Override
    public TestElement createTestElement() {
        Arguments args = getUnclonedParameters();
        this.configureTestElement(args);
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
            PropertyIterator iter = ((Arguments) el).getArguments().iterator();
            while (iter.hasNext()) {
                HTTPArgument arg = (HTTPArgument) iter.next().getObjectValue();
                tableModel.addRow(arg);
            }
        }
        checkDeleteStatus();
    }

    protected boolean isMetaDataNormal(HTTPArgument arg) {
        return arg.getMetaData() == null || arg.getMetaData().equals("=")
                || (arg.getValue() != null && arg.getValue().length() > 0);
    }
}
