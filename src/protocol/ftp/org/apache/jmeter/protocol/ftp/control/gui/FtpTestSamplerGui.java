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
package org.apache.jmeter.protocol.ftp.control.gui;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import org.apache.jmeter.config.gui.LoginConfigGui;
import org.apache.jmeter.config.gui.LoginConfigGui;
import org.apache.jmeter.gui.*;
import org.apache.jmeter.gui.util.VerticalLayout;
import org.apache.jmeter.protocol.ftp.config.gui.FtpConfigGui;
import org.apache.jmeter.protocol.ftp.control.*;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.protocol.ftp.sampler.FTPSampler;

/****************************************
 * Title: Apache JMeter Description: Copyright: Copyright (c) 2000 Company:
 * Apache Foundation
 *
 *@author    Michael Stover
 *@created   $Date$
 *@version   1.0
 ***************************************/

public class FtpTestSamplerGui extends AbstractSamplerGui
{
	private LoginConfigGui loginPanel;
	private FtpConfigGui ftpDefaultPanel;

	/****************************************
	 * !ToDo (Constructor description)
	 ***************************************/
	public FtpTestSamplerGui()
	{
		init();
	}

	public void configure(TestElement element)
	{
		super.configure(element);
		loginPanel.configure(element);
		ftpDefaultPanel.configure(element);
	}


	public TestElement createTestElement()
	{
		FTPSampler sampler = new FTPSampler();
		sampler.addTestElement(ftpDefaultPanel.createTestElement());
		sampler.addTestElement(loginPanel.createTestElement());
		this.configureTestElement(sampler);
		return sampler;
	}

	public String getStaticLabel()
	{
		return JMeterUtils.getResString("ftp_testing_title");
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
		JLabel panelTitleLabel = new JLabel(JMeterUtils.getResString("ftp_testing_title"));
		Font curFont = panelTitleLabel.getFont();
		int curFontSize = curFont.getSize();
		curFontSize += 4;
		panelTitleLabel.setFont(new Font(curFont.getFontName(), curFont.getStyle(), curFontSize));
		mainPanel.add(panelTitleLabel);

		// NAME
		mainPanel.add(getNamePanel());

		loginPanel = new LoginConfigGui(false);
		ftpDefaultPanel = new FtpConfigGui(false);
		//ftpDefaultPanel.setBorder(BorderFactory.createTitledBorder("Default Values"));
		loginPanel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("login_config")));

		mainPanel.add(getNamePanel());
		mainPanel.add(ftpDefaultPanel);
		mainPanel.add(loginPanel);
		this.add(mainPanel);
	}
}
