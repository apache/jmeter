/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.jmeter.assertions.gui;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.jmeter.assertions.SizeAssertion;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.apache.jorphan.gui.layout.VerticalLayout;


/****************************************
 * Title: Jakarta-JMeter Description: Copyright: Copyright (c) 2001 Company:
 * Apache
 *
 *@author    Michael Stover
 *@created   $Date$
 *@version   1.0
 ***************************************/

public class SizeAssertionGui extends AbstractAssertionGui implements FocusListener
{
	transient private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.elements");

	private JTextField size;

	/****************************************
	 * !ToDo (Constructor description)
	 ***************************************/
	public SizeAssertionGui()
	{
		init();
	}

	/**
	 * Returns the label to be shown within the JTree-Component.
	 */
	public String getStaticLabel()
	{
		return JMeterUtils.getResString("size_assertion_title");
	}

	public String getSizeAttributesTitle()
	{
		return JMeterUtils.getResString("size_assertion_size_test");
	}

	public TestElement createTestElement()
	{
		//ResponseAssertion el = new ResponseAssertion();
		SizeAssertion el = new SizeAssertion();
		configureTestElement(el);
		String sizeString = size.getText();
		long assertionSize = 0;
		try {
			assertionSize = Long.parseLong(sizeString);
		}
		catch (NumberFormatException e) {
			assertionSize = Long.MAX_VALUE;
		}
		el.setAllowedSize(assertionSize);
		return el;
	}

	/****************************************
	 * !ToDo (Method description)
	 ***************************************/
	public void configure(TestElement el)
	{
		super.configure(el);
		SizeAssertion assertion = (SizeAssertion)el;
		size.setText(String.valueOf(assertion.getAllowedSize()));
	}

	private void init()
	{
		this.setLayout(new VerticalLayout(5, VerticalLayout.LEFT, VerticalLayout.TOP));

		// MAIN PANEL
		JPanel mainPanel = new JPanel();
		Border margin = new EmptyBorder(10, 10, 5, 10);
		mainPanel.setBorder(margin);
		mainPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));

		// TITLE
		JLabel panelTitleLabel = new JLabel(getStaticLabel());
		Font curFont = panelTitleLabel.getFont();
		int curFontSize = curFont.getSize();
		curFontSize += 4;
		panelTitleLabel.setFont(new Font(curFont.getFontName(), curFont.getStyle(), curFontSize));
		mainPanel.add(panelTitleLabel);

		// NAME
		mainPanel.add(getNamePanel());

		// USER_INPUT
		JPanel sizePanel = new JPanel();
		sizePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), getSizeAttributesTitle()));
		FlowLayout layout = new FlowLayout();
		sizePanel.setLayout(layout);

		sizePanel.add(new JLabel(JMeterUtils.getResString("size_assertion_label")));
		size = new JTextField(5);
		size.addFocusListener(this);
		sizePanel.add(size);

		mainPanel.add(sizePanel);
		this.add(mainPanel);

	}

	/****************************************
	 * Description of the Method
	 *
	 *@param e  Description of Parameter
	 ***************************************/
	public void focusLost(FocusEvent e)
	{
		boolean isInvalid = false;
		String sizeString = size.getText();
		if (sizeString != null) {
			try {
				long assertionSize = Long.parseLong(sizeString);
				if (assertionSize < 0) {
					isInvalid = true;
				}
			}
			catch (NumberFormatException ex) {
				isInvalid = true;
			}
			if (isInvalid) {
				log.warn("SizeAssertionGui: Not a valid number!");
				JOptionPane.showMessageDialog(null, JMeterUtils.getResString("size_assertion_input_error"), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/****************************************
	 * Description of the Method
	 *
	 *@param e  Description of Parameter
	 ***************************************/
	public void focusGained(FocusEvent e) {
	}

}
