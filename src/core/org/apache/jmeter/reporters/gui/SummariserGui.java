package org.apache.jmeter.reporters.gui;

import java.awt.BorderLayout;

import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.processor.gui.AbstractPostProcessorGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Create a summariser test element.
 * 
 * Note:
 * This is not really a PostProcessor, but that seems to be the closest
 * of the existing types.
 * 
 * @author sebb AT apache DOT org
 * @version $Revision$ Last updated: $date$
 */
public class SummariserGui extends AbstractPostProcessorGui
{
   
    public SummariserGui()
    {
        super();
        init();
    }

    /**
     * @see org.apache.jmeter.gui.JMeterGUIComponent#getStaticLabel()
     */
    public String getStaticLabel()
    {
        return JMeterUtils.getResString("summariser_title");
    }
    
    public void configure(TestElement el)
    {
        super.configure(el);
    }

    /**
     * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
     */
    public TestElement createTestElement()
    {
        Summariser summariser = new Summariser();
        modifyTestElement(summariser);
        return summariser;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    public void modifyTestElement(TestElement summariser)
    {
        super.configureTestElement(summariser);
    }
    
    private void init()
    {
        setLayout(new BorderLayout());
        setBorder(makeBorder());
        
        add(makeTitlePanel(),BorderLayout.NORTH);
    }
}
