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
package org.apache.jmeter.protocol.java.control.gui;

import java.awt.BorderLayout;

import org.apache.jmeter.protocol.java.config.JavaConfig;
import org.apache.jmeter.protocol.java.config.gui.JavaConfigGui;
import org.apache.jmeter.protocol.java.sampler.JavaSampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;


/**
 * The <code>JavaTestSamplerGui</code> class provides the user interface 
 * for the JavaTestSampler.
 * 
 * @author Brad Kiewel
 * @version $Revision$
 */

public class JavaTestSamplerGui extends AbstractSamplerGui {
	
	private JavaConfigGui javaPanel = null;

	/**
	 * Constructor for JavaTestSamplerGui
	 *
	public JavaTestSamplerGui(LayoutManager arg0, boolean arg1) {
		super(arg0, arg1);
	}

	/**
	 * Constructor for JavaTestSamplerGui
	 *
	public JavaTestSamplerGui(LayoutManager arg0) {
		super(arg0);
	}

	/**
	 * Constructor for JavaTestSamplerGui
	 *
	public JavaTestSamplerGui(boolean arg0) {
		super(arg0);
	}

	/**
	 * Constructor for JavaTestSamplerGui
	 */
	public JavaTestSamplerGui() {
		super();
		init();
	}
	
	public String getStaticLabel()
	{
		return JMeterUtils.getResString("Java Request");
	}

	private void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        
		add(makeTitlePanel(), BorderLayout.NORTH);

		javaPanel = new JavaConfigGui(false);
		
		add(javaPanel, BorderLayout.CENTER);
	}
	
	public TestElement createTestElement()
	{
		JavaSampler sampler = new JavaSampler();
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
        JavaConfig config = (JavaConfig)javaPanel.createTestElement();
        this.configureTestElement(sampler);
        sampler.addTestElement(config);
    }
    

	
	public void configure(TestElement el)
	{
		super.configure(el);
		javaPanel.configure(el);
	}
}

