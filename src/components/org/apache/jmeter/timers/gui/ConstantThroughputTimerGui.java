/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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
 
package org.apache.jmeter.timers.gui;

import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.timers.ConstantThroughputTimer;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;

/**
 * GUI for the Constant Throughput Timer.
 *
 * @author <a href="mailto:jsalvata@atg.com">Jordi Salvat i Alabart</a>
 * @author <a href="mailto:seade@backstagetech.com.au">Scott Eade</a>
 * @version $Id$
 */
public class ConstantThroughputTimerGui
	extends AbstractTimerGui
	implements KeyListener
{
    private final String DEFAULT_THROUGHPUT = "60";
    private final String THROUGHPUT_FIELD = "Throughput Field";

    private JTextField throughputField;

    /**
     * Constructor for an initialized but unconfigured
     * ConstantThroughputTimerGui.
     */
    public ConstantThroughputTimerGui()
    {
		init();
    }

	/**
	 * Get the title to display for this component.
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#getStaticLabel()
	 */
    public String getStaticLabel()
    {
		return JMeterUtils.getResString("constant_throughput_timer_title");
    }

	/**
	 * Create the test element underlying this GUI component.
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
	 */
    public TestElement createTestElement()
    {
		ConstantThroughputTimer timer = new ConstantThroughputTimer();
		modifyTestElement(timer);
		return timer;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    public void modifyTestElement(TestElement timer)
    {
        this.configureTestElement(timer);
        ((ConstantThroughputTimer)timer).setThroughput(Long.parseLong(throughputField.getText()));
    }

	/**
	 * Configure this GUI component from the underlying TestElement.
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#configure(TestElement)
	 */
    public void configure(TestElement el)
    {
		super.configure(el);
		ConstantThroughputTimer e= (ConstantThroughputTimer)el;
		throughputField.setText(String.valueOf(e.getThroughput()));
    }

    /**
     * Checks the key just entered in the Throughput text input field makes a
     * valid number, cleans the input field otherwise.
     *
     * @param e  KeyEvent - handled only if it happens in the Throughput field.
     */
    public void keyReleased(KeyEvent e)
    {
		String n = e.getComponent().getName();
		if(n.equals(THROUGHPUT_FIELD))
		{
		    try
		    {
			Long.parseLong(throughputField.getText());
		    }
		    catch(NumberFormatException nfe)
		    {
			if(throughputField.getText().length() > 0)
			{
			    JOptionPane.showMessageDialog(this,
				    "You must enter a valid number",
				    "Invalid data", JOptionPane.WARNING_MESSAGE);
			    // We reset the text to be an empty string instead
			    // of the default value. If we reset it to the
			    // default value, then the user has to delete
			    // that value and reenter his/her own. That's
			    // too much trouble for the user.
			}
		    }
		}
    }

	/**
	 * Process a KeyEvent.
	 *
	 * @param e the event to handle.
	 */
    public void keyPressed(KeyEvent e)
    {
    }

	/**
	 * Process a KeyEvent.
	 *
	 * @param e the event to handle.
	 */
    public void keyTyped(KeyEvent e)
    {
    }

    /**
     * Initializes this GUI component. Creates and lays out all GUI elements.
     */
    private void init()
    {
		this.setLayout(new VerticalLayout(5, VerticalLayout.LEFT,
			    			  VerticalLayout.TOP));
	
		// MAIN PANEL
		JPanel mainPanel = new JPanel();
		Border margin = new EmptyBorder(10, 10, 5, 10);
		mainPanel.setBorder(margin);
		mainPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));
	
		// TITLE
		JLabel panelTitleLabel = new JLabel(
			JMeterUtils.getResString("constant_throughput_timer_title"));
		Font curFont = panelTitleLabel.getFont();
		int curFontSize = curFont.getSize();
		curFontSize += 4;
		panelTitleLabel.setFont(new Font(curFont.getFontName(),
			    curFont.getStyle(), curFontSize));
		mainPanel.add(panelTitleLabel);
	
		// NAME
		mainPanel.add(getNamePanel());
	
		// throughput
		JPanel throughputPanel = new JPanel();
		JLabel throughputLabel = new JLabel(
		      JMeterUtils.getResString("constant_throughput_timer_throughput"));
		throughputPanel.add(throughputLabel);
		throughputField = new JTextField(6);
		throughputField.setText(DEFAULT_THROUGHPUT);
		throughputPanel.add(throughputField);
		mainPanel.add(throughputPanel);
	
		throughputField.addKeyListener(this);
		throughputField.setName(THROUGHPUT_FIELD);
	
		this.add(mainPanel);
    }
    
}
