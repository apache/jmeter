package org.apache.jmeter.protocol.http.control.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JPanel;

import org.apache.jmeter.protocol.http.sampler.SoapSampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextArea;
import org.apache.jorphan.gui.JLabeledTextField;

/**
 * @author mstover
 */
public class SoapSamplerGui extends AbstractSamplerGui
{
    private static final String label = JMeterUtils.getResString("soap_sampler_title");
    private JLabeledTextField urlField;
    private JLabeledTextArea soapXml;

    public SoapSamplerGui()
    {
        init();
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.gui.JMeterGUIComponent#getStaticLabel()
     */
    public String getStaticLabel()
    {
        return label;
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
     */
    public TestElement createTestElement()
    {
        SoapSampler sampler = new SoapSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    public void modifyTestElement(TestElement s)
    {
        this.configureTestElement(s);
        if (s instanceof SoapSampler)
        {
            SoapSampler sampler = (SoapSampler) s;
            sampler.setURLData(urlField.getText());
            sampler.setXmlData(soapXml.getText());
        }
    }

    private void init()
    {
        setLayout(new BorderLayout());
        setBorder(makeBorder());
        
        add(makeTitlePanel(), BorderLayout.NORTH);

        urlField = new JLabeledTextField(JMeterUtils.getResString("url"), 10);
        soapXml = new JLabeledTextArea(JMeterUtils.getResString("soap_data_title"), null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(urlField, BorderLayout.NORTH);
        mainPanel.add(soapXml, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
    }

    public void configure(TestElement el)
    {
        super.configure(el);
        SoapSampler sampler = (SoapSampler) el;
        urlField.setText(sampler.getURLData());
        soapXml.setText(sampler.getXmlData());
    }
    
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }
}
