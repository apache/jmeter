/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2004 The Apache Software Foundation.  All rights
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
 * 
 * @author <a href="mailto:jsalvata@apache.org">Jordi Salvat i Alabart</a>
 * @version $Id$
 */
package org.apache.jmeter.testbeans.gui;

import java.awt.Component;
import java.beans.PropertyEditorSupport;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Helper class to support using a TestElement GUI as a PropertyEditor when an
 * element is embedded as a property of another (e.g. Arguments on TestPlan or
 * on HTTPSampler).
 */
public abstract class TestElementEditor extends PropertyEditorSupport {
    private static Logger log = LoggingManager.getLoggerForClass();

	private Class guiClass;
    private JMeterGUIComponent guiComponent;
    private TestElement element;
    
	/**
	 * Create a property editor from a given Component subclass implementing
	 * JMeterGuiComponent -- most often an AbstractJMeterGuiComponent subclass.
	 * 
	 * @param guiClass the JMeterGuiComponent class on which to build the editor.
	 */
	public TestElementEditor(Class guiClass) {
		super();
		
		this.guiClass= guiClass;
		
		guiComponent= GuiPackage.getInstance().getGui(null, guiClass, null);
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyEditor#getCustomEditor()
	 */
	public Component getCustomEditor() {
		return (Component)guiComponent;
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyEditor#getValue()
	 */
	public Object getValue() {
        TestElement newElement= guiComponent.createTestElement();
		guiComponent.modifyTestElement(newElement);
        log.debug("Got "+newElement);
        return newElement;
	}

	/**
	 * @param value the TestElementProperty to edit 
	 * @see java.beans.PropertyEditor#setValue(java.lang.Object)
	 */
	public void setValue(Object value) {
        log.debug("Set to "+value);
		if (value != null)
		{
            element= (TestElement)value;
			guiComponent.configure(element);
			firePropertyChange();
		}
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyEditor#supportsCustomEditor()
	 */
	public boolean supportsCustomEditor() {
		return true;
	}
}
