package org.apache.jmeter.protocol.http.control.gui;

import java.awt.Font;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.jmeter.protocol.http.sampler.SoapSampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextArea;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.gui.layout.VerticalLayout;

/**
 * @author mstover
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class SoapSamplerGui extends AbstractSamplerGui {
	private static final String label = JMeterUtils.getResString("soap_sampler_title");
	JLabeledTextField urlField = new JLabeledTextField(JMeterUtils.getResString("url"));
	JLabeledTextArea soapXml = new JLabeledTextArea(JMeterUtils.getResString("soap_data_title"),
			null);

	public SoapSamplerGui()
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
		SoapSampler sampler = new SoapSampler();
		this.configureTestElement(sampler);
		try {
			URL url = new URL(urlField.getText());
			sampler.setDomain(url.getHost());
			sampler.setPort(url.getPort());
			sampler.setProtocol(url.getProtocol());
			sampler.setMethod(SoapSampler.POST);
			sampler.setPath(url.getPath());
			sampler.setXmlData(soapXml.getText());
		} catch(MalformedURLException e) {
		}
		return sampler;
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
		JLabel panelTitleLabel = new JLabel(label);
		Font curFont = panelTitleLabel.getFont();
		int curFontSize = curFont.getSize();
		curFontSize += 4;
		panelTitleLabel.setFont(new Font(curFont.getFontName(), curFont.getStyle(), curFontSize));
		mainPanel.add(panelTitleLabel);
		// NAME
		mainPanel.add(getNamePanel());

		mainPanel.add(urlField);

		// OPTIONAL TASKS
		mainPanel.add(soapXml);

		this.add(mainPanel);
	}
	
	public void configure(TestElement el)
	{
		super.configure(el);
		SoapSampler sampler = (SoapSampler)el;
		try {
			urlField.setText(sampler.getUrl().toString());
		} catch(MalformedURLException e) {
		}
		soapXml.setText(sampler.getXmlData());
	}

}
