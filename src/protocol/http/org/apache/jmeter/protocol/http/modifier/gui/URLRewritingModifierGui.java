package org.apache.jmeter.protocol.http.modifier.gui;

import java.awt.Font;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.jmeter.config.gui.AbstractResponseBasedModifierGui;
import org.apache.jmeter.protocol.http.modifier.URLRewritingModifier;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.gui.layout.VerticalLayout;

/**
 * @author mstover
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class URLRewritingModifierGui extends AbstractResponseBasedModifierGui {
	
	JLabeledTextField argumentName;
	JCheckBox pathExt;
	private final static String title = JMeterUtils.getResString("http_url_rewriting_modifier_title");

	/**
	 * @see JMeterGUIComponent#getStaticLabel()
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
		this.setLayout(new VerticalLayout(5, VerticalLayout.LEFT, VerticalLayout.TOP));

		// MAIN PANEL
		JPanel mainPanel = new JPanel();
		Border margin = new EmptyBorder(10, 10, 5, 10);
		mainPanel.setBorder(margin);
		mainPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));

		// TITLE
		JLabel panelTitleLabel = new JLabel(title);
		Font curFont = panelTitleLabel.getFont();
		int curFontSize = curFont.getSize();
		curFontSize += 4;
		panelTitleLabel.setFont(new Font(curFont.getFontName(), curFont.getStyle(), curFontSize));
		mainPanel.add(panelTitleLabel);

		// NAME
		mainPanel.add(getNamePanel());
		argumentName = new JLabeledTextField(JMeterUtils.getResString("session_argument_name"));
		mainPanel.add(argumentName);
		pathExt = new JCheckBox(JMeterUtils.getResString("Path_Extension_choice"));
		
		mainPanel.add(pathExt);	

		this.add(mainPanel);
	}

	/**
	 * @see JMeterGUIComponent#createTestElement()
	 */
	public TestElement createTestElement() {
		URLRewritingModifier modifier = new URLRewritingModifier();
		this.configureTestElement(modifier);
		modifier.setArgumentName(argumentName.getText());
		modifier.setPathExtension(pathExt.isSelected());
		return modifier;
	}
	
	public void configure(TestElement el)
	{
		argumentName.setText(((URLRewritingModifier)el).getArgumentName());
		pathExt.setSelected(((URLRewritingModifier)el).isPathExtension());
		super.configure(el);
	}

}
