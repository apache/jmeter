package org.apache.jmeter.reporters.gui;

import java.awt.BorderLayout;

import javax.swing.Box;

import org.apache.jmeter.reporters.ResultAction;
import org.apache.jmeter.gui.OnErrorPanel;
import org.apache.jmeter.processor.gui.AbstractPostProcessorGui;
import org.apache.jmeter.testelement.OnErrorTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Create a Result Action Test Element
 * 
 * @author sebb AT apache DOT org
 * @version $Revision$ Last updated: $Date$
 */
public class ResultActionGui extends AbstractPostProcessorGui
{
	
	private OnErrorPanel errorPanel;
   
	public ResultActionGui()
    {
        super();
        init();
    }

    /**
     * @see org.apache.jmeter.gui.JMeterGUIComponent#getStaticLabel()
     */
    public String getStaticLabel()
    {
        return JMeterUtils.getResString("resultaction_title");
    }
    
	/**
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#configure(TestElement)
	 */
    public void configure(TestElement el)
    {
        super.configure(el);
        errorPanel.configure(((OnErrorTestElement)el).getErrorAction());
    }

    /**
     * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
     */
    public TestElement createTestElement()
    {
        ResultAction resultAction = new ResultAction();
        modifyTestElement(resultAction);
        return resultAction;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    public void modifyTestElement(TestElement te)
    {
        super.configureTestElement(te);
        ((OnErrorTestElement) te).setErrorAction(errorPanel.getOnErrorSetting());
    }
    
    private void init()
    {
        setLayout(new BorderLayout());
        setBorder(makeBorder());
		Box box = Box.createVerticalBox();
		box.add(makeTitlePanel());
		errorPanel = new OnErrorPanel(); 
		box.add(errorPanel);
		add(box,BorderLayout.NORTH);
    }
}
