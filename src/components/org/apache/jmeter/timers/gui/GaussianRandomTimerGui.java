/*
 * ====================================================================
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
 
package org.apache.jmeter.timers.gui;

import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.apache.jmeter.gui.util.FocusRequester;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.timers.GaussianRandomTimer;
import org.apache.jmeter.timers.RandomTimer;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;

/**
 * Implementation of a gaussian random timer.
 *
 * @author    Michael Stover
 * @author <a href="mailto:seade@backstagetech.com.au">Scott Eade</a>
 * @version $Id$
 */
public class GaussianRandomTimerGui extends AbstractTimerGui implements KeyListener
{

	private final String DELAY_FIELD = "Delay Field";
	private final String RANGE_FIELD = "Range Field";

	public final String DEFAULT_DELAY = "300";
	public final String DEFAULT_RANGE = "100.0";

	private JTextField delayField;
	private JTextField rangeField;

	/**
	 * No-arg constructor.
	 */
	public GaussianRandomTimerGui()
	{
		init();
	}

	/**
	 * Handle an error.
	 *
	 * @param e the Exception that was thrown.
	 * @param thrower the JComponent that threw the Exception.
	 */
	public static void error(Exception e, JComponent thrower)
	{
		JOptionPane.showMessageDialog(thrower, e, "Error", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Create the test element underlying this GUI component.
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
	 */
	public TestElement createTestElement()
	{
		RandomTimer timer = new GaussianRandomTimer();
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
        ((RandomTimer)timer).setDelay(delayField.getText());
        ((RandomTimer)timer).setRange(rangeField.getText());
    }

	/**
	 * Configure this GUI component from the underlying TestElement.
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#configure(TestElement)
	 */
	public void configure(TestElement el)
	{
		super.configure(el);
		delayField.setText(el.getPropertyAsString(RandomTimer.DELAY));
		rangeField.setText(el.getPropertyAsString(RandomTimer.RANGE));
	}

	/**
	 * Get the title to display for this component.
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#getStaticLabel()
	 */
	public String getStaticLabel()
	{
		return JMeterUtils.getResString("gaussian_timer_title");
	}

	/**
	 * Process a KeyEvent.
	 *
	 * @param e the event to handle.
	 */
	public void keyReleased(KeyEvent e)
	{
		String temp = e.getComponent().getName();

		if(temp.equals(RANGE_FIELD))
		{
			try
			{
				Double.parseDouble(rangeField.getText());
			}
			catch(NumberFormatException nfe)
			{
				if(rangeField.getText().length() > 0)
				{
					JOptionPane.showMessageDialog(this, "You must enter a valid number",
							"Invalid data", JOptionPane.WARNING_MESSAGE);
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
	 * Initialize this component.
	 */
	private void init()
	{
		this.setLayout(new VerticalLayout(5, VerticalLayout.LEFT, VerticalLayout.TOP));

		// MAIN PANEL
		JPanel mainPanel = new JPanel();
		Border margin = new EmptyBorder(10, 10, 5, 10);
		mainPanel.setBorder(margin);
		mainPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));

		// TITLE
		JLabel panelTitleLabel = new JLabel(JMeterUtils.getResString("gaussian_timer_title"));
		Font curFont = panelTitleLabel.getFont();
		int curFontSize = curFont.getSize();
		curFontSize += 4;
		panelTitleLabel.setFont(new Font(curFont.getFontName(), curFont.getStyle(), curFontSize));
		mainPanel.add(panelTitleLabel);

		// NAME
		mainPanel.add(getNamePanel());

		// THREAD DELAY PROPERTIES
		JPanel threadDelayPropsPanel = new JPanel();
		margin = new EmptyBorder(5, 10, 10, 10);
		threadDelayPropsPanel.setLayout(new VerticalLayout(0, VerticalLayout.LEFT));
		threadDelayPropsPanel.setBorder(new CompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils.getResString("thread_delay_properties")), margin));

		// DELAY DEVIATION
		JPanel delayDevPanel = new JPanel();
		JLabel rangeLabel = new JLabel(JMeterUtils.getResString("gaussian_timer_range"));
		delayDevPanel.add(rangeLabel);
		rangeField = new JTextField(6);
		rangeField.setText(DEFAULT_RANGE);
		rangeField.setName(RANGE_FIELD);
		rangeField.addKeyListener(this);
		delayDevPanel.add(rangeField);
		threadDelayPropsPanel.add(delayDevPanel);
		mainPanel.add(threadDelayPropsPanel);

		// AVG DELAY
		JPanel avgDelayPanel = new JPanel();
		JLabel delayLabel = new JLabel(JMeterUtils.getResString("gaussian_timer_delay"));
		avgDelayPanel.add(delayLabel);
		delayField = new JTextField(20);
		delayField.setText(DEFAULT_DELAY);
		delayField.setName(DELAY_FIELD);
		delayField.addKeyListener(this);
		avgDelayPanel.add(delayField);
		threadDelayPropsPanel.add(avgDelayPanel);

		this.add(mainPanel);

		// Set the initial focus to the delay field
		new FocusRequester(rangeField);
	}
	
}
