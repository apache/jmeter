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
package org.apache.jmeter.protocol.ftp.config.gui;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.protocol.ftp.sampler.FTPSampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;

/****************************************
 * Title: JMeter Description: Copyright: Copyright (c) 2000 Company: Apache
 *
 *@author    Michael Stover
 *@created   $Date$
 *@version   1.0
 ***************************************/

public class FtpConfigGui extends AbstractConfigGui
{
	private final static String SERVER = "server";
	private final static String FILENAME = "filename";

	private JTextField server = new JTextField(20);
	private JTextField filename = new JTextField(20);

	private boolean displayName = true;

	/****************************************
	 * !ToDo (Constructor description)
	 ***************************************/
	public FtpConfigGui()
	{
		this(true);
	}

	public String getStaticLabel()
	{
		return JMeterUtils.getResString("ftp_sample_title");
	}

	public void configure(TestElement element)
	{
		super.configure(element);
		server.setText(element.getPropertyAsString(FTPSampler.SERVER));
		filename.setText(element.getPropertyAsString(FTPSampler.FILENAME));
	}

	public TestElement createTestElement()
	{
		ConfigTestElement element = new ConfigTestElement();
		modifyTestElement(element);
		return element;
	}

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    public void modifyTestElement(TestElement element)
    {
        configureTestElement(element);
        element.setProperty(FTPSampler.SERVER,server.getText());
        element.setProperty(FTPSampler.FILENAME,filename.getText());
    }

	/****************************************
	 * !ToDo (Constructor description)
	 *
	 *@param displayName  !ToDo (Parameter description)
	 ***************************************/
	public FtpConfigGui(boolean displayName)
	{
		this.displayName = displayName;
		init();
	}

	private JPanel createServerPanel()
	{
		JPanel serverPanel = new JPanel();
		serverPanel.add(new JLabel(JMeterUtils.getResString("server")));
		server.setName(SERVER);
		serverPanel.add(server);

		return serverPanel;
	}

	private JPanel createFilenamePanel()
	{
		JPanel filenamePanel = new JPanel();
		filenamePanel.add(new JLabel(JMeterUtils.getResString("file_to_retrieve")));
		filename.setName(FILENAME);
		filenamePanel.add(filename);

		return filenamePanel;
	}

	private void init()
	{
		this.setLayout(new VerticalLayout(1, VerticalLayout.LEFT));
		if(displayName)
		{
			this.setLayout(new VerticalLayout(5, VerticalLayout.LEFT, VerticalLayout.TOP));

			// MAIN PANEL
			JPanel mainPanel = new JPanel();
			Border margin = new EmptyBorder(10, 10, 5, 10);
			mainPanel.setBorder(margin);
			mainPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));

			// NAME
			mainPanel.add(makeTitlePanel());

			// LOOP
			mainPanel.add(createServerPanel());
			mainPanel.add(createFilenamePanel());

			this.add(mainPanel);
		}
		else
		{
			this.add(createServerPanel());
			this.add(createFilenamePanel());
		}
	}
}
