/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001,2003 The Apache Software Foundation.  All rights
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
package org.apache.jmeter.control.gui;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.control.ThroughputController;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;

/****************************************
 * Title: JMeter Description: Copyright: Copyright (c) 2000 Company: Apache
 *
 *@author    Kevin Hammond
 *@created   $Date$
 *@version   $Revision$
 ***************************************/

public class ThroughputControllerGui 
	extends AbstractControllerGui
{
    private JComboBox styleBox;
    private int style;
    private JTextField throughput;
    private JCheckBox perthread;
    private boolean isPerThread = true;

    private String BYNUMBER_LABEL = JMeterUtils.getResString("throughput_control_bynumber_label");
	private String BYPERCENT_LABEL = JMeterUtils.getResString("throughput_control_bypercent_label");
	private String THROUGHPUT_LABEL = JMeterUtils.getResString("throughput_control_tplabel");
	private String THROUGHPUT = "Througput Field";
	private String PERTHREAD_LABEL = JMeterUtils.getResString("throughput_control_perthread_label");

	/****************************************
	 * !ToDo (Constructor description)
	 ***************************************/
	public ThroughputControllerGui()
	{
		init();
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public TestElement createTestElement()
	{
		ThroughputController tc = new ThroughputController();
		modifyTestElement(tc);
		return tc;
	}

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    public void modifyTestElement(TestElement tc)
    {
        configureTestElement(tc);
		((ThroughputController)tc).setStyle(style);
		((ThroughputController)tc).setPerThread(isPerThread);
        if (style == ThroughputController.BYNUMBER) {
			try {
				((ThroughputController)tc).setMaxThroughput(Integer.parseInt(throughput.getText().trim()));
			} catch (NumberFormatException e) {
				((ThroughputController)tc).setMaxThroughput(1);
			}
        }
        else {
        	try {
        		((ThroughputController)tc).setPercentThroughput(Float.parseFloat(throughput.getText().trim()));
        	} catch (NumberFormatException e) {
        		((ThroughputController)tc).setPercentThroughput(100);
           	}
        }
    }

	public void configure(TestElement el)
	{
		super.configure(el);
		if (((ThroughputController)el).getStyle() == ThroughputController.BYNUMBER)
		{
			styleBox.getModel().setSelectedItem(BYNUMBER_LABEL);
			throughput.setText(String.valueOf(((ThroughputController)el).getMaxThroughput()));
		}
		else
		{
			styleBox.setSelectedItem(BYPERCENT_LABEL);
			throughput.setText(String.valueOf(((ThroughputController)el).getPercentThroughput()));
		}
		perthread.setSelected(((ThroughputController)el).isPerThread());		
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public String getStaticLabel()
	{
		return JMeterUtils.getResString("throughput_control_title");
	}

	private void init()
	{
		setLayout(new VerticalLayout(5, VerticalLayout.LEFT, VerticalLayout.TOP));
        setBorder(makeBorder());
		add(makeTitlePanel());

		DefaultComboBoxModel styleModel = new DefaultComboBoxModel();
		styleModel.addElement(BYNUMBER_LABEL);
		styleModel.addElement(BYPERCENT_LABEL);
		styleBox = new JComboBox(styleModel);
		styleBox.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if (((String)styleBox.getSelectedItem()).equals(BYNUMBER_LABEL))
						style = ThroughputController.BYNUMBER;
					else
						style = ThroughputController.BYPERCENT;
				}
			}
		);
		add(styleBox);

		// TYPE FIELD
		JPanel tpPanel = new JPanel();
		JLabel tpLabel = new JLabel(THROUGHPUT_LABEL);
		tpPanel.add(tpLabel);

		// TEXT FIELD
		throughput = new JTextField(5);
		tpPanel.add(throughput);
		throughput.setName(THROUGHPUT);
		throughput.setText("1");
//		throughput.addActionListener(this);
		tpPanel.add(throughput);
		add(tpPanel);
		
		// PERTHREAD FIELD
		perthread = new JCheckBox(PERTHREAD_LABEL, isPerThread);
		perthread.addItemListener(
			new ItemListener() 
			{
				public void itemStateChanged(ItemEvent event)
				{
					if (event.getStateChange() == ItemEvent.SELECTED)
						isPerThread = true;
					else
						isPerThread = false;
				}
			}
		);
//		add(perthread);
	}
}
