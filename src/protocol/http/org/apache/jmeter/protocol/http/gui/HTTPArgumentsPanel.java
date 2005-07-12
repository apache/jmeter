/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
	/*
	 * NOTUSED private static final String ENCODED_VALUE =
	 * JMeterUtils.getResString("encoded_value");
	 */
	private static final String ENCODE_OR_NOT = JMeterUtils.getResString("encode?");

	private static final String INCLUDE_EQUALS = JMeterUtils.getResString("include_equals");

	protected void initializeTableModel() {
		tableModel = new ObjectTableModel(new String[] { ArgumentsPanel.COLUMN_NAMES_0, ArgumentsPanel.COLUMN_NAMES_1,
				ENCODE_OR_NOT, INCLUDE_EQUALS }, new Functor[] { new Functor("getName"), new Functor("getValue"),
				new Functor("isAlwaysEncoded"), new Functor("isUseEquals") }, new Functor[] { new Functor("setName"),
				new Functor("setValue"), new Functor("setAlwaysEncoded"), new Functor("setUseEquals") }, new Class[] {
				String.class, String.class, Boolean.class, Boolean.class });
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
		super(JMeterUtils.getResString("paramtable"));
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
