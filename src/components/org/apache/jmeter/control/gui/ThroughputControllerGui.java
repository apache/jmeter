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

import javax.swing.DefaultComboBoxModel;
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

public class ThroughputControllerGui extends AbstractControllerGui implements ActionListener
{
    private JComboBox style;
    private JTextField throughput;

    private String BYNUMBER_LABEL = JMeterUtils.getResString("throughput_control_bynumber_label");
	private String BYPERCENT_LABEL = JMeterUtils.getResString("throughput_control_bypercent_label");
	private String THROUGHPUT_LABEL = JMeterUtils.getResString("throughput_control_tplabel");
	private String THROUGHPUT = "Througput Field";

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
        if (((String)style.getSelectedItem()).equals(BYNUMBER_LABEL)) {
        	((ThroughputController)tc).setStyle(ThroughputController.BYNUMBER);
			try {
				((ThroughputController)tc).setMaxThroughput(Integer.parseInt(throughput.getText().trim()));
			} catch (NumberFormatException e) {
				((ThroughputController)tc).setMaxThroughput(1);
			}
        }
        else {
        	((ThroughputController)tc).setStyle(ThroughputController.BYPERCENT);
        	try {
        		((ThroughputController)tc).setPercentThroughput(Float.parseFloat(throughput.getText().trim()));
        	} catch (NumberFormatException e) {
        		((ThroughputController)tc).setMaxThroughput(100);
           	}
        }
    }

	public void configure(TestElement el)
	{
		super.configure(el);
		if (((ThroughputController)el).getStyle() == ThroughputController.BYNUMBER)
		{
			style.getModel().setSelectedItem(BYNUMBER_LABEL);
			throughput.setText(String.valueOf(((ThroughputController)el).getMaxThroughput()));
		}
		else
		{
			style.setSelectedItem(BYPERCENT_LABEL);
			throughput.setText(String.valueOf(((ThroughputController)el).getPercentThroughput()));
		}
		
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
		style = new JComboBox(styleModel);
		add(style);

		// TYPE FIELD
		JPanel tpPanel = new JPanel();
		JLabel tpLabel = new JLabel(JMeterUtils.getResString("throughput_control_tplabel"));
		tpPanel.add(tpLabel);

		// TEXT FIELD
		throughput = new JTextField(5);
		tpPanel.add(throughput);
		throughput.setName(THROUGHPUT);
		throughput.setText("1");
		throughput.addActionListener(this);
		tpPanel.add(throughput);

		add(tpPanel);
	}
	
	public void actionPerformed (ActionEvent event) 
	{
		if (((String)style.getSelectedItem()).equals(BYNUMBER_LABEL))
		{
			try 
			{
				Integer.parseInt(throughput.getText().trim());
			} 
			catch (NumberFormatException e) 
			{
				
			}
		}
		else
		{
			try
			{
				Float.parseFloat(throughput.getText().trim());
			}
			catch (NumberFormatException e)
			{
				
			}
			
		}
	}

}
