/*
 * $Header$
 * $Revision$
 * $Date$
 * 
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002,2003 The Apache Software Foundation.  All rights
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

package org.apache.jmeter.protocol.java.control.gui;

import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane; 
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.jmeter.protocol.java.sampler.BeanShellSampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

/**
 * @author sebb AT apache DOT org
 * @version   $Revision$ $Date$
 */
public class BeanShellSamplerGui extends AbstractSamplerGui
{
	
	private JTextField filename;// script file name (if present)
	private JTextField parameters;// parameters to pass to script file (or script)
	private JTextArea scriptField;// script area


	public BeanShellSamplerGui()
    {
        init();
    }

    public void configure(TestElement element)
    {
    	scriptField.setText(element.getProperty(BeanShellSampler.SCRIPT).toString());
		filename.setText(element.getProperty(BeanShellSampler.FILENAME).toString());
		parameters.setText(element.getProperty(BeanShellSampler.PARAMETERS).toString());
        super.configure(element);
    }

    public TestElement createTestElement()
    {
        BeanShellSampler sampler = new BeanShellSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    public void modifyTestElement(TestElement te)
    {
        te.clear();
        this.configureTestElement(te);
		te.setProperty(BeanShellSampler.SCRIPT, scriptField.getText());
		te.setProperty(BeanShellSampler.FILENAME, filename.getText());
		te.setProperty(BeanShellSampler.PARAMETERS, parameters.getText());
    }

    public String getStaticLabel()
    {
        return JMeterUtils.getResString("bsh_sampler_title");
    }

    

	private JPanel createFilenamePanel()//TODO ought to be a FileChooser ...
	{
		JLabel label = new JLabel(JMeterUtils.getResString("bsh_script_file"));
		
		filename = new JTextField(10);
		filename.setName(BeanShellSampler.FILENAME);
		label.setLabelFor(filename);

		JPanel filenamePanel = new JPanel(new BorderLayout(5, 0));
		filenamePanel.add(label, BorderLayout.WEST);
		filenamePanel.add(filename, BorderLayout.CENTER);
		return filenamePanel;
	}

    private JPanel createParameterPanel()
    {
    	JLabel label = new JLabel(JMeterUtils.getResString("bsh_script_parameters"));

		parameters = new JTextField(10);
		parameters.setName(BeanShellSampler.PARAMETERS);
		label.setLabelFor(parameters);

    	JPanel parameterPanel = new JPanel(new BorderLayout(5,0));
    	parameterPanel.add(label,BorderLayout.WEST);
		parameterPanel.add(parameters, BorderLayout.CENTER);
    	return parameterPanel;
    }

    private void init()
    {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

		Box box = Box.createVerticalBox();
		box.add(makeTitlePanel());
		box.add(createParameterPanel());
		box.add(createFilenamePanel());
		add(box,BorderLayout.NORTH);

		JPanel panel = createScriptPanel();
		add(panel, BorderLayout.CENTER);
		// Don't let the input field shrink too much
		add(
			Box.createVerticalStrut(panel.getPreferredSize().height),
			BorderLayout.WEST);
    }


	private JPanel createScriptPanel()
	{
		scriptField = new JTextArea();
		scriptField.setRows(4);
		scriptField.setLineWrap(true);
		scriptField.setWrapStyleWord(true);

		JLabel label = new JLabel(JMeterUtils.getResString("bsh_script"));
		label.setLabelFor(scriptField);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(label, BorderLayout.NORTH);
		panel.add(new JScrollPane(scriptField), BorderLayout.CENTER);
		return panel;
	}
}
