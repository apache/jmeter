/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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
package org.apache.jmeter.protocol.java.config.gui;

import java.awt.Font;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.gui.util.VerticalLayout;
import org.apache.jmeter.protocol.java.config.JavaConfig;
import org.apache.jmeter.protocol.java.sampler.JavaSampler;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerClient;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.ClassFinder;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;


/**
 * The <code>JavaConfigGui</code> class provides the user interface for
 * the JavaConfig object.
 * @author Brad Kiewel
 * @version $Revision$
 */

public class JavaConfigGui extends AbstractConfigGui
{
	private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor("jmeter.protocol.java");
	private static String CLASSNAMECOMBO = "classnamecombo";

	private JComboBox classnameCombo;
	protected boolean displayName = true;
	private ArgumentsPanel argsPanel;

	public JavaConfigGui()
	{
		this(true);
	}
	
	public String getStaticLabel()
	{
		return JMeterUtils.getResString("Java Request Defaults");
	}

	public JavaConfigGui(boolean displayNameField)
	{
		this.displayName = displayNameField;
		init();
	}

	protected void init()
	{
		this.setLayout(new VerticalLayout(5, VerticalLayout.LEFT, VerticalLayout.TOP));

		JPanel classnameRequestPanel = new JPanel();
		classnameRequestPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT, VerticalLayout.TOP));
		classnameRequestPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils.getResString("protocol_java_border")));
		classnameRequestPanel.add(getClassnamePanel());
		classnameRequestPanel.add(getParameterPanel());

		if (displayName)
		{
			// MAIN PANEL
			JPanel mainPanel = new JPanel();
			Border margin = new EmptyBorder(10, 10, 5, 10);
			mainPanel.setBorder(margin);
			mainPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));

			// TITLE
			JLabel panelTitleLabel = new JLabel(JMeterUtils.getResString("protocol_java_config_tile"));
			Font curFont = panelTitleLabel.getFont();
			int curFontSize = curFont.getSize();
			curFontSize += 4;
			panelTitleLabel.setFont(new Font(curFont.getFontName(), curFont.getStyle(), curFontSize));
			mainPanel.add(panelTitleLabel);

			// NAME
			mainPanel.add(getNamePanel());
			
			mainPanel.add(classnameRequestPanel);

			this.add(mainPanel);
		}
		else
		{
			this.add(classnameRequestPanel);
		}
	}


	protected JPanel getClassnamePanel()
	{
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10));
		panel.add(new JLabel(JMeterUtils.getResString("protocol_java_classname")));
		
		List possibleClasses = null;
		
		try {
			
			// Find all the classes which implement the JavaSamplerClient interface
		
			possibleClasses = ClassFinder.findClassesThatExtend(new Class[]{JavaSamplerClient.class});
			
			// Remove the JavaConfig class from the list since it only implements the interface for
			// error conditions.
			
			possibleClasses.remove("org.apache.jmeter.protocol.java.sampler.JavaSampler");
		
		} catch (Exception e) {
			log.debug("Exception getting interfaces.",e);
		}
		
		classnameCombo = new JComboBox(possibleClasses.toArray());
		classnameCombo.setName(CLASSNAMECOMBO);
		classnameCombo.setEditable(false);
		panel.add(classnameCombo);
		

		return panel;
	}

	protected JPanel getParameterPanel()
	{
		argsPanel = new ArgumentsPanel();
		return argsPanel;
	}
	
	public void configure(TestElement config)
	{
		super.configure(config);
		argsPanel.configure((Arguments)config.getProperty(JavaSampler.ARGUMENTS));
		classnameCombo.setSelectedItem(config.getPropertyAsString(JavaSampler.CLASSNAME));
	}
	
	public TestElement createTestElement()
	{
		JavaConfig config = new JavaConfig();
		this.configureTestElement(config);
		config.setArguments((Arguments)argsPanel.createTestElement());
		config.setClassname(classnameCombo.getSelectedItem().toString());
		return config;
	}		

}
