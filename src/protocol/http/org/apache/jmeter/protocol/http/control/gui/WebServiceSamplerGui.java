// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.jmeter.protocol.http.control.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.WebServiceSampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.gui.util.FilePanel;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.JLabeledTextArea;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.gui.layout.VerticalLayout;
import org.apache.jmeter.protocol.http.util.WSDLHelper;
import org.apache.jmeter.protocol.http.control.AuthManager;

/**
 * This is the GUI for the webservice samplers. It extends AbstractSamplerGui
 * and is modeled after the SOAP sampler GUI. I've added instructional notes to
 * the GUI for instructional purposes. XML parsing is pretty heavy weight,
 * therefore the notes address those situations. <br>
 * Created on: Jun 26, 2003
 * 
 * @author Peter Lin
 * @version $Id: WebServiceSamplerGui.java,v 1.18 2005/06/07 02:04:31 woolfel
 *          Exp $
 */
public class WebServiceSamplerGui extends AbstractSamplerGui implements java.awt.event.ActionListener {

	JLabeledTextField domain = new JLabeledTextField(JMeterUtils.getResString("web_server_domain"));

	JLabeledTextField port = new JLabeledTextField(JMeterUtils.getResString("web_server_port"));

	JLabeledTextField path = new JLabeledTextField(JMeterUtils.getResString("path"));

	JLabeledTextField soapAction = new JLabeledTextField(JMeterUtils.getResString("webservice_soap_action"));

	JLabeledTextArea soapXml = new JLabeledTextArea(JMeterUtils.getResString("soap_data_title"), null);

	JLabeledTextField wsdlField = new JLabeledTextField(JMeterUtils.getResString("wsdl_url"));

	JButton wsdlButton = new JButton(JMeterUtils.getResString("load_wsdl"));

	JButton selectButton = new JButton(JMeterUtils.getResString("configure_wsdl"));

	JLabeledChoice wsdlMethods = null;

	WSDLHelper HELPER = null;

	FilePanel soapXmlFile = new FilePanel(JMeterUtils.getResString("get_xml_from_file"), ".xml");

	JLabeledTextField randomXmlFile = new JLabeledTextField(JMeterUtils.getResString("get_xml_from_random"));

	/**
	 * We create several JLabel objects to display usage instructions in the
	 * GUI. The reason there are multiple labels is to make sure it displays
	 * correctly.
	 */
	JLabel wsdlMessage = new JLabel(JMeterUtils.getResString("get_xml_message"));

	JLabel wsdlMessage2 = new JLabel(JMeterUtils.getResString("get_xml_message2"));

	JLabel wsdlMessage3 = new JLabel(JMeterUtils.getResString("get_xml_message3"));

	JLabel wsdlMessage4 = new JLabel(JMeterUtils.getResString("get_xml_message4"));

	JLabel wsdlMessage5 = new JLabel(JMeterUtils.getResString("get_xml_message5"));

	/**
	 * This is the font for the note.
	 */
	Font plainText = new Font("plain", Font.PLAIN, 10);

	/**
	 * checkbox for memory cache.
	 */
	JCheckBox memCache = new JCheckBox(JMeterUtils.getResString("memory_cache"), true);

	/**
	 * checkbox for reading the response
	 */
	JCheckBox readResponse = new JCheckBox(JMeterUtils.getResString("read_soap_response"));

	/**
	 * checkbox for use proxy
	 */
	JCheckBox useProxy = new JCheckBox(JMeterUtils.getResString("webservice_use_proxy"));

	/**
	 * text field for the proxy host
	 */
	JLabeledTextField proxyHost = new JLabeledTextField(JMeterUtils.getResString("webservice_proxy_host"));

	/**
	 * text field for the proxy port
	 */
	JLabeledTextField proxyPort = new JLabeledTextField(JMeterUtils.getResString("webservice_proxy_port"));

	/**
	 * Text note about read response and it's usage.
	 */
	JLabel readMessage = new JLabel(JMeterUtils.getResString("read_response_note"));

	JLabel readMessage2 = new JLabel(JMeterUtils.getResString("read_response_note2"));

	JLabel readMessage3 = new JLabel(JMeterUtils.getResString("read_response_note3"));

	/**
	 * Text note for proxy
	 */
	JLabel proxyMessage = new JLabel(JMeterUtils.getResString("webservice_proxy_note"));

	JLabel proxyMessage2 = new JLabel(JMeterUtils.getResString("webservice_proxy_note2"));

	JLabel proxyMessage3 = new JLabel(JMeterUtils.getResString("webservice_proxy_note3"));

	public WebServiceSamplerGui() {
		init();
	}

	public String getLabelResource() {
		return "webservice_sampler_title";
	}

	/**
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
	 */
	public TestElement createTestElement() {
		WebServiceSampler sampler = new WebServiceSampler();
		this.configureTestElement(sampler);
		this.modifyTestElement(sampler);
		return sampler;
	}

	/**
	 * Modifies a given TestElement to mirror the data in the gui components.
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
	 */
	public void modifyTestElement(TestElement s) {
		WebServiceSampler sampler = (WebServiceSampler) s;
		this.configureTestElement(sampler);
		sampler.setDomain(domain.getText());
        sampler.setProperty(HTTPSamplerBase.PORT,port.getText());
		sampler.setPath(path.getText());
		sampler.setWsdlURL(wsdlField.getText());
		sampler.setMethod(HTTPSamplerBase.POST);
		sampler.setSoapAction(soapAction.getText());
		sampler.setXmlData(soapXml.getText());
		sampler.setXmlFile(soapXmlFile.getFilename());
		sampler.setXmlPathLoc(randomXmlFile.getText());
		sampler.setMemoryCache(memCache.isSelected());
		sampler.setReadResponse(readResponse.isSelected());
		sampler.setUseProxy(useProxy.isSelected());
		sampler.setProxyHost(proxyHost.getText());
		sampler.setProxyPort(proxyPort.getText());
	}

	/**
	 * init() adds soapAction to the mainPanel. The class reuses logic from
	 * SOAPSampler, since it is common.
	 */
	private void init() {
		this.setLayout(new VerticalLayout(5, VerticalLayout.LEFT, VerticalLayout.TOP));
		wsdlMessage.setFont(plainText);
		wsdlMessage2.setFont(plainText);
		wsdlMessage3.setFont(plainText);
		wsdlMessage4.setFont(plainText);
		wsdlMessage5.setFont(plainText);
		readMessage.setFont(plainText);
		readMessage2.setFont(plainText);
		readMessage3.setFont(plainText);

		// MAIN PANEL
		JPanel mainPanel = new JPanel();
		Border margin = new EmptyBorder(10, 10, 5, 10);
		mainPanel.setBorder(margin);
		mainPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));

		// TITLE
		JLabel panelTitleLabel = new JLabel(getStaticLabel());
		Font curFont = panelTitleLabel.getFont();
		int curFontSize = curFont.getSize();
		curFontSize += 4;
		panelTitleLabel.setFont(new Font(curFont.getFontName(), curFont.getStyle(), curFontSize));
		mainPanel.add(panelTitleLabel);
		// NAME
		mainPanel.add(getNamePanel());

		// Button for browsing webservice wsdl
		JPanel wsdlEntry = new JPanel();
		mainPanel.add(wsdlEntry);
		wsdlEntry.add(wsdlField);
		wsdlEntry.add(wsdlButton);
		wsdlButton.addActionListener(this);

		// Web Methods
		JPanel listPanel = new JPanel();
		JLabel selectLabel = new JLabel("Web Methods");
		wsdlMethods = new JLabeledChoice();
		mainPanel.add(listPanel);
		listPanel.add(selectLabel);
		listPanel.add(wsdlMethods);
		listPanel.add(selectButton);
		selectButton.addActionListener(this);

		mainPanel.add(domain);
		mainPanel.add(port);
		mainPanel.add(path);
		mainPanel.add(soapAction);
		// OPTIONAL TASKS
		// we create a preferred size for the soap text area
		// the width is the same as the soap file browser
		Dimension pref = new Dimension(400, 200);
		soapXml.setPreferredSize(pref);
		mainPanel.add(soapXml);
		mainPanel.add(soapXmlFile);
		mainPanel.add(wsdlMessage);
		mainPanel.add(wsdlMessage2);
		mainPanel.add(wsdlMessage3);
		mainPanel.add(wsdlMessage4);
		mainPanel.add(wsdlMessage5);
		mainPanel.add(randomXmlFile);
		mainPanel.add(memCache);
		mainPanel.add(readResponse);
		mainPanel.add(readMessage);
		mainPanel.add(readMessage2);
		mainPanel.add(readMessage3);

		// add the proxy elements
		mainPanel.add(useProxy);
		useProxy.addActionListener(this);
		mainPanel.add(proxyHost);
		mainPanel.add(proxyPort);
		// add the proxy notes
		proxyMessage.setFont(plainText);
		proxyMessage2.setFont(plainText);
		proxyMessage3.setFont(plainText);
		mainPanel.add(proxyMessage);
		mainPanel.add(proxyMessage2);
		mainPanel.add(proxyMessage3);

		this.add(mainPanel);
	}

	/**
	 * the implementation loads the URL and the soap action for the request.
	 */
	public void configure(TestElement el) {
		super.configure(el);
		WebServiceSampler sampler = (WebServiceSampler) el;
		wsdlField.setText(sampler.getWsdlURL());
		domain.setText(sampler.getDomain());
        port.setText(sampler.getPropertyAsString(HTTPSamplerBase.PORT));
		path.setText(sampler.getPath());
		soapAction.setText(sampler.getSoapAction());
		soapXml.setText(sampler.getXmlData());
		soapXmlFile.setFilename(sampler.getXmlFile());
		randomXmlFile.setText(sampler.getXmlPathLoc());
		memCache.setSelected(sampler.getMemoryCache());
		readResponse.setSelected(sampler.getReadResponse());
		useProxy.setSelected(sampler.getUseProxy());
		if (sampler.getProxyHost().length() == 0) {
			proxyHost.setEnabled(false);
		} else {
			proxyHost.setText(sampler.getProxyHost());
		}
		if (sampler.getProxyPort() == 0) {
			proxyPort.setEnabled(false);
		} else {
			proxyPort.setText(String.valueOf(sampler.getProxyPort()));
		}
	}

	/**
	 * configure the sampler from the WSDL. If the WSDL did not include service
	 * node, it will use the original URL minus the querystring. That may not be
	 * correct, so we should probably add a note. For Microsoft webservices it
	 * will work, since that's how IIS works.
	 */
	public void configureFromWSDL() {
		if (HELPER.getBinding() != null) {
			this.domain.setText(HELPER.getURL().getHost());
			if (HELPER.getURL().getPort() > 0) {
				this.port.setText(String.valueOf(HELPER.getURL().getPort()));
			} else {
				this.port.setText("80");
			}
			this.path.setText(HELPER.getURL().getPath());
		}
		this.soapAction.setText(HELPER.getSoapAction(this.wsdlMethods.getText()));
	}

	/**
	 * The method uses WSDLHelper to get the information from the WSDL. Since
	 * the logic for getting the description is isolated to this method, we can
	 * easily replace it with a different WSDL driver later on.
	 * 
	 * @param url
	 * @return array of web methods
	 */
	public String[] browseWSDL(String url) {
		try {
			// We get the AuthManager and pass it to the WSDLHelper
			// once the sampler is updated to Axis, all of this stuff
			// should not be necessary. Now I just need to find the
			// time and motivation to do it.
			WebServiceSampler sampler = (WebServiceSampler) this.createTestElement();
			AuthManager manager = sampler.getAuthManager();
			HELPER = new WSDLHelper(url, manager);
			HELPER.parse();
			return HELPER.getWebMethods();
		} catch (Exception exception) {
			JOptionPane.showConfirmDialog(this, JMeterUtils.getResString("wsdl_helper_error"), "Warning",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

	/**
	 * method from ActionListener
	 * 
	 * @param event
	 *            that occurred
	 */
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == selectButton) {
			this.configureFromWSDL();
		} else if (event.getSource() == useProxy) {
			// if use proxy is checked, we enable
			// the text fields for the host and port
			boolean use = useProxy.isSelected();
			if (use) {
				proxyHost.setEnabled(true);
				proxyPort.setEnabled(true);
			} else {
				proxyHost.setEnabled(false);
				proxyPort.setEnabled(false);
			}
		} else {
			if (this.domain.getText() != null) {
				String[] wsdlData = browseWSDL(wsdlField.getText());
				if (wsdlData != null) {
					wsdlMethods.setValues(wsdlData);
					wsdlMethods.repaint();
				}
			} else {
				JOptionPane.showConfirmDialog(this, JMeterUtils.getResString("wsdl_url_error"), "Warning",
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
			}
		}
	}

}
