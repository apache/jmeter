/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001,2003 The Apache Software Foundation.  All rights
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
package org.apache.jmeter.protocol.http.control.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import junit.framework.TestCase;

import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.http.config.gui.MultipartUrlConfigGui;
import org.apache.jmeter.protocol.http.config.gui.UrlConfigGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

/**
 * @author    Michael Stover
 * @version   $Revision$
 */
public class HttpTestSampleGui extends AbstractSamplerGui
{
    private UrlConfigGui urlConfigGui;
    private JCheckBox getImages;

    public HttpTestSampleGui()
    {
        init();
    }

    public void configure(TestElement element)
    {
        super.configure(element);
        urlConfigGui.configure(element);
        String testClass = element.getPropertyAsString(TestElement.TEST_CLASS);
        getImages.setSelected(((HTTPSampler) element).isImageParser());
    }

    public TestElement createTestElement()
    {
        HTTPSampler sampler = new HTTPSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     * @see JMeterGUIComponent#modifyTestElement(TestElement)
     */
    public void modifyTestElement(TestElement sampler)
    {
        TestElement el = urlConfigGui.createTestElement();
        sampler.clear();
        sampler.addTestElement(el);
        if (getImages.isSelected())
        {
            ((HTTPSampler)sampler).setImageParser(true);
        }
        else
        {
            ((HTTPSampler)sampler).setImageParser(false);
        }
        this.configureTestElement(sampler);
    }

    /**
     * Gets the ClassLabel attribute of the HttpTestSample object.
     *
     * @return   the ClassLabel value
     */
    public String getStaticLabel()
    {
        return JMeterUtils.getResString("web_testing_title");
    }

    private void init()
    {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);

        // URL CONFIG
        urlConfigGui = new MultipartUrlConfigGui();
        add(urlConfigGui, BorderLayout.CENTER);

        // OPTIONAL TASKS
        add(createOptionalTasksPanel(), BorderLayout.SOUTH);
    }

    private JPanel createOptionalTasksPanel()
    {
        // OPTIONAL TASKS
        VerticalPanel optionalTasksPanel = new VerticalPanel();
        optionalTasksPanel.setBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("optional_tasks")));

        // RETRIEVE IMAGES
        JPanel retrieveImagesPanel = new JPanel();
        getImages =
            new JCheckBox(
                JMeterUtils.getResString("web_testing_retrieve_images"));
        retrieveImagesPanel.add(getImages);

        optionalTasksPanel.add(retrieveImagesPanel);
        return optionalTasksPanel;
    }
        
    public Dimension getPreferredSize()
    {
        return getMinimumSize();
    }

    public static class Test extends TestCase
    {
        HttpTestSampleGui gui;
        
        public Test(String name)
        {
            super(name);
        }
        
        public void setUp()
        {
            gui = new HttpTestSampleGui();
        }
        
        public void testCloneSampler() throws Exception
        {
            HTTPSampler sampler = (HTTPSampler)gui.createTestElement();
            sampler.addArgument("param","value");
            HTTPSampler clonedSampler = (HTTPSampler)sampler.clone();
            clonedSampler.setRunningVersion(true);
            sampler.getArguments().getArgument(0).setValue("new value");
            assertEquals(
                "Sampler didn't clone correctly",
                "new value",
                sampler.getArguments().getArgument(0).getValue());
        }
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.gui.JMeterGUIComponent#clear()
     */
    public void clear()
    {
        super.clear();
        getImages.setSelected(false);
        urlConfigGui.clear();
    }
}
