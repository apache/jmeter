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
package org.apache.jmeter.protocol.http.config.gui;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
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

public class MultipartUrlConfigGui extends UrlConfigGui implements ActionListener
{

	JTextField filenameField;
	JTextField paramNameField;
	JTextField mimetypeField;
	JLabel filenameLabel;
	JLabel paramNameLabel;
	JLabel mimetypeLabel;
	JButton browseFileButton;
	private static String FILENAME = "filename";
	private static String BROWSE = "browse";
	private static String PARAMNAME = "paramname";
	private static String MIMETYPE = "mimetype";

	/****************************************
	 * !ToDo (Constructor description)
	 ***************************************/
	public MultipartUrlConfigGui()
	{
		super();
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public TestElement createTestElement()
	{
		TestElement ce = super.createTestElement();
		configureTestElement(ce);
		ce.setProperty(HTTPSampler.MIMETYPE, mimetypeField.getText());
		ce.setProperty(HTTPSampler.FILE_NAME, filenameField.getText());
		ce.setProperty(HTTPSampler.FILE_FIELD, paramNameField.getText());
		return ce;
	}

	public void configureSampler(HTTPSampler sampler)
	{
		sampler.setMimetype(mimetypeField.getText());
		sampler.setFileField(paramNameField.getText());
		sampler.setFilename(filenameField.getText());
		super.configureSampler(sampler);
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param el  !ToDo (Parameter description)
	 ***************************************/
	public void configure(TestElement el)
	{
		super.configure(el);
		mimetypeField.setText((String)el.getProperty(HTTPSampler.MIMETYPE));
		filenameField.setText((String)el.getProperty(HTTPSampler.FILE_NAME));
		paramNameField.setText((String)el.getProperty(HTTPSampler.FILE_FIELD));
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public String getStaticLabel()
	{
		return JMeterUtils.getResString("url_multipart_config_title");
	}

	/****************************************
	 * !ToDo (Method description)
	 ***************************************/
	public void updateGui() { }


	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param e  !ToDo (Parameter description)
	 ***************************************/
	public void actionPerformed(ActionEvent e)
	{
		String name = e.getActionCommand();
		if(name.equals(BROWSE))
		{
			JFileChooser chooser = FileDialoger.promptToOpenFile();
			if(chooser == null)
			{
				return;
			}
			File file = chooser.getSelectedFile();
			if(file != null)
			{
				filenameField.setText(file.getPath());
			}
		}
	}

	/****************************************
	 * !ToDo (Method description)
	 ***************************************/
	protected void init()
	{
		this.setLayout(new BorderLayout());

		// WEB SERVER PANEL
		JPanel webServerPanel = new JPanel();
		webServerPanel.setLayout(new BorderLayout());
		webServerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils.getResString("web_server")));
		webServerPanel.add(getDomainPanel(),BorderLayout.NORTH);
		webServerPanel.add(getPortPanel(),BorderLayout.SOUTH);

		// WEB REQUEST PANEL
		JPanel webRequestPanel = new JPanel(new BorderLayout());
		JPanel northPanel = new JPanel(new BorderLayout());
		webRequestPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils.getResString("web_request")));
		northPanel.add(getProtocolAndMethodPanel(),BorderLayout.NORTH);
		northPanel.add(getPathPanel(),BorderLayout.SOUTH);
		webRequestPanel.add(northPanel,BorderLayout.NORTH);
		webRequestPanel.add(getParameterPanel(),BorderLayout.CENTER);
		webRequestPanel.add(getFilePanel(),BorderLayout.SOUTH);

		// If displayName is TRUE, then this GUI is not embedded in another GUI.
		this.add(webServerPanel,BorderLayout.NORTH);
		this.add(webRequestPanel,BorderLayout.CENTER);
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	protected JPanel getFilePanel()
	{
		// FILE PANEL (all main components are add to this panel)
		JPanel filePanel = new JPanel();
		filePanel.setLayout(new VerticalLayout(1, VerticalLayout.LEFT));
		filePanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

		// SEND FILE LABEL
		JLabel sendFileLabel = new JLabel(JMeterUtils.getResString("send_file"));
		filePanel.add(sendFileLabel);

		// FILENAME PANEL (contains filename label and text field and Browse button)
		JPanel filenamePanel = new JPanel();
		filenamePanel.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));

		// --- FILENAME LABEL
		filenameLabel = new JLabel(JMeterUtils.getResString("send_file_filename_label"));
		filenameLabel.setEnabled(true);

		// --- FILENAME TEXT FIELD
		filenameField = new JTextField(15);
		filenameField.setEnabled(true);
		filenameField.setName(FILENAME);

		// --- BROWSE BUTTON
		browseFileButton = new JButton(JMeterUtils.getResString("send_file_browse"));
		browseFileButton.setEnabled(true);
		browseFileButton.setActionCommand(BROWSE);
		browseFileButton.addActionListener(this);

		filenamePanel.add(filenameLabel);
		filenamePanel.add(filenameField);
		filenamePanel.add(browseFileButton);

		// PARAM NAME PANEL (contains param name label and text field)
		JPanel paramNamePanel = new JPanel();
		paramNamePanel.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));

		// --- PARAM NAME LABEL
		paramNameLabel = new JLabel(JMeterUtils.getResString("send_file_param_name_label"));
		paramNameLabel.setEnabled(true);

		// --- PARAM NAME TEXT FIELD
		paramNameField = new JTextField(15);
		paramNameField.setName(PARAMNAME);
		paramNameField.setEnabled(true);

		paramNamePanel.add(paramNameLabel);
		paramNamePanel.add(paramNameField);

		// MIME TYPE PANEL (contains mime type label and text field)
		JPanel mimePanel = new JPanel();
		mimePanel.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));

		// --- MIME TYPE LABEL
		mimetypeLabel = new JLabel(JMeterUtils.getResString("send_file_mime_label"));
		mimetypeLabel.setEnabled(true);

		// --- MIME TYPE TEXT FIELD
		mimetypeField = new JTextField(15);
		mimetypeField.setEnabled(true);
		mimetypeField.setName(MIMETYPE);

		mimePanel.add(mimetypeLabel);
		mimePanel.add(mimetypeField);

		filePanel.add(filenamePanel);
		filePanel.add(paramNamePanel);
		filePanel.add(mimePanel);

		return filePanel;
	}
}
