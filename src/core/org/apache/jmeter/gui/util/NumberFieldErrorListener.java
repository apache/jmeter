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
import java.awt.TextComponent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JOptionPane;
import javax.swing.text.JTextComponent;

import org.apache.jmeter.util.JMeterUtils;

/**
 * @author mstover
 * @version $Revision$
 */
public class NumberFieldErrorListener extends FocusAdapter {

	private static NumberFieldErrorListener listener = new NumberFieldErrorListener();

	public static NumberFieldErrorListener getNumberFieldErrorListener() {
		return listener;
	}

	public void focusLost(FocusEvent e) {
		Component source = (Component) e.getSource();
		String text = "";
		if (source instanceof JTextComponent) {
			text = ((JTextComponent) source).getText();
		} else if (source instanceof TextComponent) {
			text = ((TextComponent) source).getText();
		}
		try {
			Integer.parseInt(text);
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(source, JMeterUtils.getResString("You must enter a valid number"),
					JMeterUtils.getResString("Invalid data"), JOptionPane.WARNING_MESSAGE);
			new FocusRequester(source);
		}
	}
}
