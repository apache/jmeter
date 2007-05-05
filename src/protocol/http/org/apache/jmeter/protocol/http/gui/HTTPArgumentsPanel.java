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
import javax.swing.table.TableColumn;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.reflect.Functor;

public class HTTPArgumentsPanel extends ArgumentsPanel {

	private static final String ENCODE_OR_NOT = JMeterUtils.getResString("encode?"); //$NON-NLS-1$

	private static final String INCLUDE_EQUALS = JMeterUtils.getResString("include_equals"); //$NON-NLS-1$

	protected void initializeTableModel() {
		tableModel = new ObjectTableModel(new String[] {
				ArgumentsPanel.COLUMN_NAMES_0, ArgumentsPanel.COLUMN_NAMES_1, ENCODE_OR_NOT, INCLUDE_EQUALS },
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
	
	protected void sizeColumns(JTable table) {
		int resizeMode = table.getAutoResizeMode();
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		fixSize(table.getColumn(INCLUDE_EQUALS));
		fixSize(table.getColumn(ENCODE_OR_NOT));
		table.setAutoResizeMode(resizeMode);
	}

	protected Object makeNewArgument() {
		HTTPArgument arg = new HTTPArgument("", "");
		arg.setAlwaysEncoded(false);
		arg.setUseEquals(true);
		return arg;
	}

	private void fixSize(TableColumn column) {
		column.sizeWidthToFit();
		// column.setMinWidth(column.getWidth());
		column.setMaxWidth((int) (column.getWidth() * 1.5));
		column.setWidth(column.getMaxWidth());
		column.setResizable(false);
	}

	public HTTPArgumentsPanel() {
		super(JMeterUtils.getResString("paramtable")); //$NON-NLS-1$
	}

	public TestElement createTestElement() {
		stopTableEditing();
		Iterator modelData = tableModel.iterator();
		Arguments args = new Arguments();
		while (modelData.hasNext()) {
			HTTPArgument arg = (HTTPArgument) modelData.next();
			args.addArgument(arg);
		}
		this.configureTestElement(args);
		return (TestElement) args.clone();
	}

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
