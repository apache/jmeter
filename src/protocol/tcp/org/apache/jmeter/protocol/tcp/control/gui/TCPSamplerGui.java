package org.apache.jmeter.protocol.tcp.control.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import org.apache.jmeter.config.gui.LoginConfigGui;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.tcp.config.gui.TCPConfigGui;
import org.apache.jmeter.protocol.tcp.sampler.TCPSampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

/**
 * @version   $Revision$ $Date$
 */
public class TCPSamplerGui extends AbstractSamplerGui
{
	
	private LoginConfigGui loginPanel;

    private TCPConfigGui TcpDefaultPanel;

    public TCPSamplerGui()
    {
        init();
    }

    public void configure(TestElement element)
    {
        super.configure(element);
		loginPanel.configure(element);
        TcpDefaultPanel.configure(element);
    }

    public TestElement createTestElement()
    {
        TCPSampler sampler = new TCPSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    public void modifyTestElement(TestElement sampler)
    {
        sampler.clear();
        ((TCPSampler) sampler).addTestElement(
            TcpDefaultPanel.createTestElement());
		((TCPSampler) sampler).addTestElement(loginPanel.createTestElement());
        this.configureTestElement(sampler);
    }

    public String getStaticLabel()
    {
        return JMeterUtils.getResString("tcp_sample_title")+" (ALPHA CODE)";
    }

    private void init()
    {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);

        VerticalPanel mainPanel = new VerticalPanel();

        TcpDefaultPanel = new TCPConfigGui(false);
        mainPanel.add(TcpDefaultPanel);

		loginPanel = new LoginConfigGui(false);
		loginPanel.setBorder(
			BorderFactory.createTitledBorder(
				JMeterUtils.getResString("login_config")));
		mainPanel.add(loginPanel);

        add(mainPanel, BorderLayout.CENTER);
    }
}
