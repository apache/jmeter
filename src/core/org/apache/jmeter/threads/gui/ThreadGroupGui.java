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
package org.apache.jmeter.threads.gui;
import java.awt.Font;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.NamePanel;
import org.apache.jmeter.gui.util.FocusRequester;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.gui.util.NumberFieldErrorListener;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;

/****************************************
 * Title: JMeter Description: Copyright: Copyright (c) 2000 Company: Apache
 *
 *@author    Michael Stover
 *@created   $Date$
 *@version   1.0
 ***************************************/

public class ThreadGroupGui extends JPanel implements JMeterGUIComponent
{
	LoopControlPanel loopPanel;

	private final static String THREAD_NAME = "Thread Field";
	private final static String RAMP_NAME = "Ramp Up Field";

	private JTextField threadInput;
	private JTextField rampInput;
	private NamePanel namePanel;

	/****************************************
	 * !ToDo (Constructor description)
	 ***************************************/
	public ThreadGroupGui()
	{
		init();
		setName(getStaticLabel());
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public Collection getMenuCategories()
	{
		return null;
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param name  !ToDo (Parameter description)
	 ***************************************/
	public void setName(String name)
	{
		super.setName(name);
		namePanel.setName(name);
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public String getName()
	{
		return namePanel.getName();
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public TestElement createTestElement()
	{
		ThreadGroup tg = new ThreadGroup();
		tg.setProperty(TestElement.GUI_CLASS, this.getClass().getName());
		tg.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
		tg.setSamplerController((LoopController)loopPanel.createTestElement());
		tg.setProperty(TestElement.NAME, namePanel.getName());
		tg.setProperty(ThreadGroup.NUM_THREADS, threadInput.getText());
		tg.setProperty(ThreadGroup.RAMP_TIME, rampInput.getText());
		return tg;
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param tg  !ToDo (Parameter description)
	 ***************************************/
	public void configure(TestElement tg)
	{
		setName((String)tg.getProperty(TestElement.NAME));
		threadInput.setText(tg.getProperty(ThreadGroup.NUM_THREADS).toString());
		rampInput.setText(tg.getProperty(ThreadGroup.RAMP_TIME).toString());
		loopPanel.configure((TestElement)tg.getProperty(ThreadGroup.MAIN_CONTROLLER));
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public JPopupMenu createPopupMenu()
	{
		JPopupMenu pop = new JPopupMenu();
		pop.add(MenuFactory.makeMenus(new String[]{MenuFactory.CONTROLLERS,
				MenuFactory.LISTENERS, MenuFactory.SAMPLERS, MenuFactory.TIMERS,
				MenuFactory.CONFIG_ELEMENTS}, JMeterUtils.getResString("Add"),
				"Add"));
		MenuFactory.addEditMenu(pop, true);
		MenuFactory.addFileMenu(pop);
		return pop;
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public JPanel createControllerPanel()
	{
		loopPanel = new LoopControlPanel(false);
		LoopController looper= (LoopController)loopPanel.createTestElement();
		looper.setLoops(-1);
		loopPanel.configure(looper);		
		return loopPanel;
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public String getStaticLabel()
	{
		return JMeterUtils.getResString("ThreadGroup");
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
		JLabel panelTitleLabel = new JLabel(JMeterUtils.getResString("thread_group_title"));
		Font curFont = panelTitleLabel.getFont();
		int curFontSize = curFont.getSize();
		curFontSize += 4;
		panelTitleLabel.setFont(new Font(curFont.getFontName(), curFont.getStyle(), curFontSize));
		mainPanel.add(panelTitleLabel);

		// NAME
		namePanel = new NamePanel();
		mainPanel.add(namePanel);

		// THREAD PROPERTIES
		JPanel threadPropsPanel = new JPanel();
		margin = new EmptyBorder(5, 10, 10, 10);
		threadPropsPanel.setLayout(new VerticalLayout(0, VerticalLayout.LEFT));
		threadPropsPanel.setBorder(new CompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils.getResString("thread_delay_properties")), margin));

		// NUMBER OF THREADS
		JPanel threadPanel = new JPanel();
		JLabel threadLabel = new JLabel(JMeterUtils.getResString("number_of_threads"));
		threadPanel.add(threadLabel);
		threadInput = new JTextField(5);
		threadInput.setText("1");
		threadInput.addFocusListener(NumberFieldErrorListener.getNumberFieldErrorListener());
		threadInput.setName(THREAD_NAME);
		threadPanel.add(threadInput);
		threadPropsPanel.add(threadPanel);
		new FocusRequester(threadInput);

		// RAMP-UP
		JPanel rampPanel = new JPanel();
		JLabel rampLabel = new JLabel(JMeterUtils.getResString("ramp_up"));
		rampPanel.add(rampLabel);
		rampInput = new JTextField(5);
		rampInput.setText("1");
		rampInput.setName(RAMP_NAME);
		rampInput.addFocusListener(NumberFieldErrorListener.getNumberFieldErrorListener());
		rampPanel.add(rampInput);
		threadPropsPanel.add(rampPanel);

		// LOOP COUNT
		threadPropsPanel.add(createControllerPanel());

		mainPanel.add(threadPropsPanel);

		this.add(mainPanel);
	}
}
