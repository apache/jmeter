// $Header$
/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.apache.jmeter.protocol.http.config.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

/**
 * @author Michael Stover
 * @version $Revision$
 */
public class MultipartUrlConfigGui extends UrlConfigGui implements ActionListener {

	private JTextField filenameField;

	private JTextField paramNameField;

	private JTextField mimetypeField;

	private static String FILENAME = "filename";

	private static String BROWSE = "browse";

	private static String PARAMNAME = "paramname";

	private static String MIMETYPE = "mimetype";

	public MultipartUrlConfigGui() {
		super();
	}

	public TestElement createTestElement() {
		TestElement ce = super.createTestElement();

		configureTestElement(ce);
		ce.setProperty(HTTPSamplerBase.MIMETYPE, mimetypeField.getText());
		ce.setProperty(HTTPSamplerBase.FILE_NAME, filenameField.getText());
		ce.setProperty(HTTPSamplerBase.FILE_FIELD, paramNameField.getText());
		return ce;
	}

	// does not appear to be used
	// public void configureSampler(HTTPSamplerBase sampler)
	// {
	// sampler.setMimetype(mimetypeField.getText());
	// sampler.setFileField(paramNameField.getText());
	// sampler.setFilename(filenameField.getText());
	// super.configureSampler(sampler);
	// }

	public void configure(TestElement el) {
		super.configure(el);
		mimetypeField.setText(el.getPropertyAsString(HTTPSamplerBase.MIMETYPE));
		filenameField.setText(el.getPropertyAsString(HTTPSamplerBase.FILE_NAME));
		paramNameField.setText(el.getPropertyAsString(HTTPSamplerBase.FILE_FIELD));
	}

	public String getLabelResource() {
		return "url_multipart_config_title";
	}

	public void updateGui() {
	}

	public void actionPerformed(ActionEvent e) {
		String name = e.getActionCommand();

		if (name.equals(BROWSE)) {
			JFileChooser chooser = FileDialoger.promptToOpenFile();

			if (chooser == null) {
				return;
			}
			File file = chooser.getSelectedFile();

			if (file != null) {
				filenameField.setText(file.getPath());
			}
		}
	}

	protected void init() {
		this.setLayout(new BorderLayout());

		// WEB SERVER PANEL
		VerticalPanel webServerPanel = new VerticalPanel();
		webServerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
				.getResString("web_server")));
		webServerPanel.add(getDomainPanel());
		webServerPanel.add(getPortPanel());

		JPanel northPanel = new JPanel();
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
		northPanel.add(getProtocolAndMethodPanel());
		northPanel.add(getPathPanel());

		// WEB REQUEST PANEL
		JPanel webRequestPanel = new JPanel();
		webRequestPanel.setLayout(new BorderLayout());
		webRequestPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
				.getResString("web_request")));

		webRequestPanel.add(northPanel, BorderLayout.NORTH);
		webRequestPanel.add(getParameterPanel(), BorderLayout.CENTER);
		webRequestPanel.add(getFilePanel(), BorderLayout.SOUTH);

		this.add(webServerPanel, BorderLayout.NORTH);
		this.add(webRequestPanel, BorderLayout.CENTER);
	}

	protected JPanel getFilePanel() {
		JPanel filePanel = new VerticalPanel();
		filePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
				.getResString("send_file")));

		filePanel.add(createFilenamePanel());
		filePanel.add(createFileParamNamePanel());
		filePanel.add(createFileMimeTypePanel());

		return filePanel;
	}

	private JPanel createFileMimeTypePanel() {
		mimetypeField = new JTextField(15);
		mimetypeField.setName(MIMETYPE);

		JLabel mimetypeLabel = new JLabel(JMeterUtils.getResString("send_file_mime_label"));
		mimetypeLabel.setLabelFor(mimetypeField);
		JPanel mimePanel = new JPanel(new BorderLayout(5, 0));
		mimePanel.add(mimetypeLabel, BorderLayout.WEST);
		mimePanel.add(mimetypeField, BorderLayout.CENTER);
		return mimePanel;
	}

	private JPanel createFileParamNamePanel() {
		paramNameField = new JTextField(15);
		paramNameField.setName(PARAMNAME);

		JLabel paramNameLabel = new JLabel(JMeterUtils.getResString("send_file_param_name_label"));
		paramNameLabel.setLabelFor(paramNameField);

		JPanel paramNamePanel = new JPanel(new BorderLayout(5, 0));
		paramNamePanel.add(paramNameLabel, BorderLayout.WEST);
		paramNamePanel.add(paramNameField, BorderLayout.CENTER);
		return paramNamePanel;
	}

	private JPanel createFilenamePanel() {
		filenameField = new JTextField(15);
		filenameField.setName(FILENAME);

		JLabel filenameLabel = new JLabel(JMeterUtils.getResString("send_file_filename_label"));
		filenameLabel.setLabelFor(filenameField);

		JButton browseFileButton = new JButton(JMeterUtils.getResString("send_file_browse"));
		browseFileButton.setActionCommand(BROWSE);
		browseFileButton.addActionListener(this);

		JPanel filenamePanel = new JPanel(new BorderLayout(5, 0));
		filenamePanel.add(filenameLabel, BorderLayout.WEST);
		filenamePanel.add(filenameField, BorderLayout.CENTER);
		filenamePanel.add(browseFileButton, BorderLayout.EAST);
		return filenamePanel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.protocol.http.config.gui.UrlConfigGui#clear()
	 */
	public void clear() {
		// TODO Auto-generated method stub
		super.clear();
		filenameField.setText("");
		mimetypeField.setText("");
		paramNameField.setText("");
	}
}
