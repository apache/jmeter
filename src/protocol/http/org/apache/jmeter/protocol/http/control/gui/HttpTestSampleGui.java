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
package org.apache.jmeter.protocol.http.control.gui;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.jmeter.protocol.http.config.gui.MultipartUrlConfigGui;
import org.apache.jmeter.protocol.http.config.gui.UrlConfigGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerFull;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.jorphan.gui.layout.VerticalLayout;

/****************************************
 * Title: JMeter Description: Copyright: Copyright (c) 2000 Company: Apache
 *
 *@author    Michael Stover
 *@created   $Date$
 *@version   1.0
 ***************************************/

public class HttpTestSampleGui extends AbstractSamplerGui
{
	private UrlConfigGui urlConfigGui;
	private JCheckBox getImages;

	/****************************************
	 * !ToDo (Constructor description)
	 ***************************************/
	public HttpTestSampleGui()
	{
		init();
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param element  !ToDo (Parameter description)
	 ***************************************/
	public void configure(TestElement element)
	{
		super.configure(element);
		urlConfigGui.configure(element);
		String testClass = (String)element.getProperty(TestElement.TEST_CLASS);
		if(testClass != null && testClass.endsWith("Full"))
		{
			getImages.setSelected(true);
		}
		else
		{
			getImages.setSelected(false);
		}
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public TestElement createTestElement()
	{
		HTTPSampler sampler;
		TestElement el = urlConfigGui.createTestElement();
		if(getImages.isSelected())
		{
			sampler = new HTTPSamplerFull();
		}
		else
		{
			sampler = new HTTPSampler();
		}
		sampler.addTestElement(el);
		this.configureTestElement(sampler);
		return sampler;
	}

	/****************************************
	 * Gets the ClassLabel attribute of the HttpTestSample object
	 *
	 *@return   The ClassLabel value
	 ***************************************/
	public String getStaticLabel()
	{
		return JMeterUtils.getResString("web_testing_title");
	}

	private void init()
	{
		this.setLayout(new GridLayout(1,1));

		// MAIN PANEL
		JPanel mainPanel = new JPanel();
		Border margin = new EmptyBorder(10, 10, 5, 10);
		mainPanel.setBorder(margin);
		mainPanel.setLayout(new BorderLayout());
		JPanel titlePanel = new JPanel(new BorderLayout());

		// TITLE
		JLabel panelTitleLabel = new JLabel(JMeterUtils.getResString("web_testing_title"));
		Font curFont = panelTitleLabel.getFont();
		int curFontSize = curFont.getSize();
		curFontSize += 4;
		panelTitleLabel.setFont(new Font(curFont.getFontName(), curFont.getStyle(), curFontSize));
		titlePanel.add(panelTitleLabel,BorderLayout.NORTH);

		// NAME
		titlePanel.add(getNamePanel(),BorderLayout.SOUTH);
		mainPanel.add(titlePanel,BorderLayout.NORTH);

		// URL CONFIG
		urlConfigGui = new MultipartUrlConfigGui();
		mainPanel.add(urlConfigGui,BorderLayout.CENTER);

		// OPTIONAL TASKS
		mainPanel.add(createOptionalTasksPanel(),BorderLayout.SOUTH);

		this.add(mainPanel);
	}

	private JPanel createOptionalTasksPanel()
	{
		// OPTIONAL TASKS
		JPanel optionalTasksPanel = new JPanel();
		optionalTasksPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT, VerticalLayout.TOP));
		optionalTasksPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils.getResString("optional_tasks")));

		// RETRIEVE IMAGES
		JPanel retrieveImagesPanel = new JPanel();
		retrieveImagesPanel.setLayout(new BoxLayout(retrieveImagesPanel, BoxLayout.X_AXIS));
		retrieveImagesPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
		getImages = new JCheckBox(JMeterUtils.getResString("web_testing_retrieve_images"));
		retrieveImagesPanel.add(getImages);

		optionalTasksPanel.add(retrieveImagesPanel);

		return optionalTasksPanel;
	}
}
