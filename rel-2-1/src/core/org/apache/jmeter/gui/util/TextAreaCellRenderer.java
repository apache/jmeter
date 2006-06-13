// $Header$
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

package org.apache.jmeter.gui.util;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;

/**
 * @author mstover
 * @version $Revision$
 */
public class TextAreaCellRenderer implements TableCellRenderer {

	private JTextArea rend = new JTextArea("");

	public Component getTableCellRendererComponent(JTable arg0, Object arg1, boolean arg2, boolean arg3, int arg4,
			int arg5) {
		rend = new JTextArea(arg1.toString());
		rend.revalidate();
		if (!arg3 && !arg2) {
			rend.setBackground(JMeterColor.LAVENDER);
		}
		if (arg0.getRowHeight(arg4) < getPreferredHeight()) {
			arg0.setRowHeight(arg4, getPreferredHeight());
		}
		return rend;
	}

	public int getPreferredHeight() {
		return rend.getPreferredSize().height + 5;
	}
}
