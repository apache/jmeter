/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002,2003 The Apache Software Foundation.  All rights
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
package org.apache.jmeter.gui;
import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.apache.jmeter.testelement.OnErrorTestElement;
import org.apache.jmeter.util.JMeterUtils;

public class OnErrorPanel extends JPanel 
{
	// Sampler error action buttons
	private JRadioButton continueBox;
	private JRadioButton stopThrdBox;
	private JRadioButton stopTestBox;

	private JPanel createOnErrorPanel()
	{
		JPanel panel = new JPanel();
		panel.setBorder(
			BorderFactory.createTitledBorder(
				JMeterUtils.getResString("sampler_on_error_action")));

		ButtonGroup group = new ButtonGroup();

		continueBox =
			new JRadioButton(JMeterUtils.getResString("sampler_on_error_continue"));
		group.add(continueBox);
		continueBox.setSelected(true);
		panel.add(continueBox);

		stopThrdBox =
			new JRadioButton(JMeterUtils.getResString("sampler_on_error_stop_thread"));
		group.add(stopThrdBox);
		panel.add(stopThrdBox);

		stopTestBox =
			new JRadioButton(JMeterUtils.getResString("sampler_on_error_stop_test"));
		group.add(stopTestBox);
		panel.add(stopTestBox);

		return panel;
	}
    /**
     * Create a new NamePanel with the default name.
     */
    public OnErrorPanel()
    {
        init();
    }

    /**
     * Initialize the GUI components and layout.
     */
    private void init()
    {
        setLayout(new BorderLayout(5, 0));
        add(createOnErrorPanel());
    }
    public void configure(int errorAction)
    {
		stopTestBox.setSelected(errorAction == OnErrorTestElement.ON_ERROR_STOPTEST);
		stopThrdBox.setSelected(errorAction == OnErrorTestElement.ON_ERROR_STOPTHREAD);
		//continueBox.setSelected(etc);// no need to set the remaining Radio Button
    }
    
    public int getOnErrorSetting()
    {
		if (stopTestBox.isSelected()) return OnErrorTestElement.ON_ERROR_STOPTEST;
		if (stopThrdBox.isSelected()) return OnErrorTestElement.ON_ERROR_STOPTHREAD;

		// Defaults to continue
		return OnErrorTestElement.ON_ERROR_CONTINUE;
    }
}
