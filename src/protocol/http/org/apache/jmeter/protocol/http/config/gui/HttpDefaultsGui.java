package org.apache.jmeter.protocol.http.config.gui;
import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.protocol.http.gui.HTTPArgumentsPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.gui.layout.VerticalLayout;
/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class HttpDefaultsGui extends AbstractConfigGui
{
	JLabeledTextField protocol;
	JLabeledTextField domain;
	JLabeledTextField path;
	JLabeledTextField port;
	HTTPArgumentsPanel argPanel;
	
	public HttpDefaultsGui()
	{
		super();
		init();
	}
	
	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public String getStaticLabel()
	{
		return JMeterUtils.getResString("url_config_title");
	}
	
	/**
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
	 */
	public TestElement createTestElement()
	{
		ConfigTestElement config = new ConfigTestElement();
		super.configureTestElement(config);
		config.setProperty(HTTPSampler.PROTOCOL,protocol.getText());
		config.setProperty(HTTPSampler.DOMAIN,domain.getText());
		config.setProperty(HTTPSampler.PATH,path.getText());
		config.setProperty(HTTPSampler.ARGUMENTS,argPanel.createTestElement());
		config.setProperty(HTTPSampler.PORT,port.getText());
		return config;
	}
	
	public void configure(TestElement el)
	{
		super.configure(el);
		protocol.setText(el.getPropertyAsString(HTTPSampler.PROTOCOL));
		domain.setText(el.getPropertyAsString(HTTPSampler.DOMAIN));
		path.setText(el.getPropertyAsString(HTTPSampler.PATH));
		port.setText(el.getPropertyAsString(HTTPSampler.PORT));
		argPanel.configure((TestElement)el.getProperty(HTTPSampler.ARGUMENTS));
	}
	
	private void init()
	{
		Border margin = new EmptyBorder(10, 10, 5, 10);
		this.setBorder(margin);
		this.setLayout(new BorderLayout());
		argPanel = new HTTPArgumentsPanel();
		this.add(argPanel,BorderLayout.CENTER);
		protocol = new JLabeledTextField(JMeterUtils.getResString("url_config_protocol"));
		domain = new JLabeledTextField(JMeterUtils.getResString("web_server_domain"));
		path = new JLabeledTextField(JMeterUtils.getResString("path"));
		port = new JLabeledTextField(JMeterUtils.getResString("web_server_port"));
		JPanel topPanel = new JPanel(new VerticalLayout(5,VerticalLayout.LEFT));
		JLabel title = new JLabel(JMeterUtils.getResString("url_config_title"));
		Font curFont = title.getFont();
		int curFontSize = curFont.getSize();
		curFontSize += 4;
		title.setFont(new Font(curFont.getFontName(), curFont.getStyle(), curFontSize));
		
		topPanel.add(title);
		topPanel.add(getNamePanel());
		topPanel.add(protocol);
		topPanel.add(domain);
		topPanel.add(path);
		topPanel.add(port);
		this.add(topPanel,BorderLayout.NORTH);
	}
}
