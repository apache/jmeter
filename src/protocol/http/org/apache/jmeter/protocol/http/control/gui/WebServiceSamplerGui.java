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

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

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

/**
 * Title:		JMeter Access Log utilities<br>
 * Copyright:	Apache.org<br>
 * Company:		nobody<br>
 * License:<br>
 * <br>
 * Look at the apache license at the top.<br>
 * <br>
 * Description:<br>
 * This is the GUI for the webservice samplers. It extends
 * AbstractSamplerGui and is modeled after the SOAP sampler
 * GUI. I've added instructional notes to the GUI for
 * instructional purposes. XML parsing is pretty heavy
 * weight, therefore the notes address those situations.
 * <br>
 * Author:	Peter Lin<br>
 * Version: 	0.1<br>
 * Created on:	Jun 26, 2003<br>
 *
 * @author Peter Lin
 * @version $Id: 
 */
public class WebServiceSamplerGui extends AbstractSamplerGui
	implements java.awt.event.ActionListener {
		
	private static final String label = JMeterUtils.getResString("webservice_sampler_title");
	JLabeledTextField urlField = new JLabeledTextField(JMeterUtils.getResString("url"));
	JLabeledTextField soapAction = new JLabeledTextField(JMeterUtils.getResString("webservice_soap_action"));
	JLabeledTextArea soapXml = new JLabeledTextArea(JMeterUtils.getResString("soap_data_title"),null);
	JLabeledTextField wsdlField = new JLabeledTextField(JMeterUtils.getResString("wsdl_url"));
	JButton wsdlButton = new JButton(JMeterUtils.getResString("load_wsdl"));
	JButton selectButton = new JButton(JMeterUtils.getResString("configure_wsdl"));
	JLabeledChoice wsdlMethods = null;
	WSDLHelper HELPER = null;
	FilePanel soapXmlFile = new FilePanel(JMeterUtils.getResString("get_xml_from_file"),".xml");
	JLabeledTextField randomXmlFile = new JLabeledTextField(JMeterUtils.getResString("get_xml_from_random"));
	/**
	 * We create several JLabel objects to display
	 * usage instructions in the GUI. The reason
	 * there are multiple labels is to make sure
	 * it displays correctly.
	 */
	JLabel wsdlMessage = new JLabel(JMeterUtils.getResString("get_xml_message"));
	JLabel wsdlMessage2 = new JLabel(JMeterUtils.getResString("get_xml_message2"));
	JLabel wsdlMessage3 = new JLabel(JMeterUtils.getResString("get_xml_message3"));
	JLabel wsdlMessage4 = new JLabel(JMeterUtils.getResString("get_xml_message4"));
	JLabel wsdlMessage5 = new JLabel(JMeterUtils.getResString("get_xml_message5"));
	/**
	 * This is the font for the note.
	 */
	Font plainText = new Font("plain",Font.PLAIN,10);
	/**
	 * checkbox for memory cache.
	 */
	JCheckBox memCache = new JCheckBox(JMeterUtils.getResString("memory_cache"),true);
	/**
	 * checkbox for reading the response
	 */
	JCheckBox readResponse = new JCheckBox(JMeterUtils.getResString("read_soap_response"));
	/**
	 * Text note about read response and it's usage.
	 */
	JLabel readMessage = new JLabel(JMeterUtils.getResString("read_response_note"));
	JLabel readMessage2 = new JLabel(JMeterUtils.getResString("read_response_note2"));
	JLabel readMessage3 = new JLabel(JMeterUtils.getResString("read_response_note3"));
	
	public WebServiceSamplerGui()
	{
		init();
	}
	
	/**
	 * @see JMeterGUIComponent#getStaticLabel()
	 */
	public String getStaticLabel() {
		return label;
	}

	/**
	 * @see JMeterGUIComponent#createTestElement()
	 */
	public TestElement createTestElement() {
		WebServiceSampler sampler = new WebServiceSampler();
		this.configureTestElement(sampler);
		try {
			URL url = new URL(urlField.getText());
			sampler.setDomain(url.getHost());
			if (url.getPort() != -1){
				sampler.setPort(url.getPort());
			} else {
				sampler.setPort(80);
			}
			sampler.setProtocol(url.getProtocol());
			sampler.setMethod(WebServiceSampler.POST);
			sampler.setPath(url.getPath());
			sampler.setSoapAction(soapAction.getText());
			sampler.setXmlData(soapXml.getText());
			sampler.setXmlFile(soapXmlFile.getFilename());
			sampler.setXmlPathLoc(randomXmlFile.getText());
			sampler.setMemoryCache(memCache.isSelected());
			sampler.setReadResponse(readResponse.isSelected());
		} catch(MalformedURLException e) {
		}
		return sampler;
	}

	/**
	 * Modifies a given TestElement to mirror the data in the gui components.
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
	 */
	public void modifyTestElement(TestElement s)
	{
		WebServiceSampler sampler = (WebServiceSampler)s;
		this.configureTestElement(sampler);
		try {
			URL url = new URL(urlField.getText());
			sampler.setDomain(url.getHost());
			if (url.getPort() != -1){
				sampler.setPort(url.getPort());
			} else {
				sampler.setPort(80);
			}
			sampler.setProtocol(url.getProtocol());
			sampler.setMethod(WebServiceSampler.POST);
			sampler.setPath(url.getPath());
			sampler.setSoapAction(soapAction.getText());
			sampler.setXmlData(soapXml.getText());
			sampler.setXmlFile(soapXmlFile.getFilename());
			sampler.setXmlPathLoc(randomXmlFile.getText());
			sampler.setMemoryCache(memCache.isSelected());
			sampler.setReadResponse(readResponse.isSelected());
		} catch(MalformedURLException e) {
		}
	}

	/**
	 * init() adds soapAction to the mainPanel. The class
	 * reuses logic from SOAPSampler, since it is common.
	 */	
	private void init()
	{
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
		JLabel panelTitleLabel = new JLabel(label);
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
		
		mainPanel.add(urlField);
		mainPanel.add(soapAction);
		// OPTIONAL TASKS
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

		this.add(mainPanel);
	}
	
	/**
	 * the implementation loads the URL and the soap
	 * action for the request.
	 */
	public void configure(TestElement el)
	{
		super.configure(el);
		WebServiceSampler sampler = (WebServiceSampler)el;
		try {
			// only set the URL if the host is not null
			if (sampler.getUrl() != null && sampler.getUrl().getHost() != null){
				urlField.setText(sampler.getUrl().toString());
			}
			soapAction.setText(sampler.getSoapAction());
		} catch(MalformedURLException e) {
		}
		// we build the string URL
		int port = sampler.getPort();
		String strUrl = sampler.getProtocol() + "://" + sampler.getDomain();
		if (port != -1 && port != 80){
			strUrl += ":" + sampler.getPort();
		}
		strUrl += sampler.getPath() + "?" + sampler.getQueryString();
		urlField.setText(strUrl);
		soapXml.setText(sampler.getXmlData());
		soapXmlFile.setFilename(sampler.getXmlFile());
		randomXmlFile.setText(sampler.getXmlPathLoc());
		memCache.setSelected(sampler.getMemoryCache());
		readResponse.setSelected(sampler.getReadResponse());
	}
	
	/**
	 * configure the sampler from the WSDL. If the
	 * WSDL did not include service node, it will
	 * use the original URL minus the querystring.
	 * That may not be correct, so we should
	 * probably add a note. For Microsoft webservices
	 * it will work, since that's how IIS works.
	 */
	public void configureFromWSDL(){
		if (HELPER.getBinding() != null){
			this.urlField.setText(HELPER.getBinding());
		} else {
			StringBuffer buf = new StringBuffer();
			buf.append("http://" + HELPER.getURL().getHost());
			if (HELPER.getURL().getPort() != -1){
				buf.append(":" + HELPER.getURL().getPort());
			} else {
				buf.append(":" + 80);
			}
			buf.append(HELPER.getURL().getPath());
			this.urlField.setText(buf.toString());
		}
		this.soapAction.setText(HELPER.getSoapAction((String)this.wsdlMethods.getText()));
	}
	
	/**
	 * The method uses WSDLHelper to get the information
	 * from the WSDL. Since the logic for getting the
	 * description is isolated to this method, we can
	 * easily replace it with a different WSDL driver
	 * later on.
	 * @param String url
	 * @return String[] 
	 */
	public String[] browseWSDL(String url){
		try {
			HELPER = new WSDLHelper(url);
			HELPER.parse();
			return HELPER.getWebMethods();
		} catch (Exception exception){
			JOptionPane.showConfirmDialog(this,JMeterUtils.getResString("wsdl_helper_error"),"Warning",JOptionPane.OK_CANCEL_OPTION,JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

	/**
	 * method from ActionListener
	 * @param ActionEvent event
	 */
	public void actionPerformed(ActionEvent event){
		if (event.getSource() == selectButton){
			this.configureFromWSDL();
		} else {
			if (this.urlField.getText() != null){
				String[] wsdlData = browseWSDL(wsdlField.getText());
				if (wsdlData != null) {
					wsdlMethods.setValues(wsdlData);
					wsdlMethods.repaint();
				}
			} else {
				JOptionPane.showConfirmDialog(this,JMeterUtils.getResString("wsdl_url_error"),"Warning",JOptionPane.OK_CANCEL_OPTION,JOptionPane.ERROR_MESSAGE);
			}
		}
	}

}
