package org.apache.jmeter.protocol.http.modifier.gui;

import java.awt.BorderLayout;

import javax.swing.JCheckBox;

import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.processor.gui.AbstractPreProcessorGui;
import org.apache.jmeter.protocol.http.modifier.URLRewritingModifier;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;

/**
 * @author mstover
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class URLRewritingModifierGui extends AbstractPreProcessorGui {
	
	JLabeledTextField argumentName;
	JCheckBox pathExt;
	JCheckBox pathExtNoEquals;
	private final static String title = JMeterUtils.getResString("http_url_rewriting_modifier_title");

    /* (non-Javadoc)
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#getStaticLabel()
	 */
	public String getStaticLabel() {
		return title;
	}
	
	public URLRewritingModifierGui()
	{
		init();
	}
	
	private void init()
	{
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        
        add(makeTitlePanel(), BorderLayout.NORTH);

        VerticalPanel mainPanel = new VerticalPanel();

		argumentName = new JLabeledTextField(JMeterUtils.getResString("session_argument_name"), 10);
		mainPanel.add(argumentName);

		pathExt = new JCheckBox(JMeterUtils.getResString("Path_Extension_choice"));		
		mainPanel.add(pathExt);	

		pathExtNoEquals = new JCheckBox(JMeterUtils.getResString("path_extension_dont_use_equals"));		
		mainPanel.add(pathExtNoEquals);	

		add(mainPanel, BorderLayout.CENTER);
	}

    /* (non-Javadoc)
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
	 */
	public TestElement createTestElement() {
		URLRewritingModifier modifier = new URLRewritingModifier();
		modifyTestElement(modifier);
		return modifier;
	}

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    public void modifyTestElement(TestElement modifier)
    {
        this.configureTestElement(modifier);
        ((URLRewritingModifier)modifier).setArgumentName(argumentName.getText());
        ((URLRewritingModifier)modifier).setPathExtension(pathExt.isSelected());
        ((URLRewritingModifier)modifier).setPathExtensionNoEquals(pathExtNoEquals.isSelected());
    }
	
	public void configure(TestElement el)
	{
		argumentName.setText(((URLRewritingModifier)el).getArgumentName());
		pathExt.setSelected(((URLRewritingModifier)el).isPathExtension());
		pathExtNoEquals.setSelected(((URLRewritingModifier)el).isPathExtensionNoEquals());
		
		super.configure(el);
	}

}
