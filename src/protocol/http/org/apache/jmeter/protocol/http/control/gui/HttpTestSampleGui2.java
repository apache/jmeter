// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/

package org.apache.jmeter.protocol.http.control.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import junit.framework.TestCase;

import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.protocol.http.config.gui.MultipartUrlConfigGui;
import org.apache.jmeter.protocol.http.config.gui.UrlConfigGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler2;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

/**
 * @version   $Revision$ on $Date$
 */
public class HttpTestSampleGui2 extends AbstractSamplerGui
{
    private UrlConfigGui urlConfigGui;
    private JCheckBox getImages;
    private JCheckBox isMon;

    public HttpTestSampleGui2()
    {
        init();
    }

    public void configure(TestElement element)
    {
        super.configure(element);
        urlConfigGui.configure(element);
        //NOTUSED String testClass = element.getPropertyAsString(TestElement.TEST_CLASS);
        getImages.setSelected(((HTTPSampler2) element).isImageParser());
        isMon.setSelected(((HTTPSampler2) element).isMonitor());
    }

    public TestElement createTestElement()
    {
        HTTPSampler2 sampler = new HTTPSampler2();
        modifyTestElement(sampler);
        return sampler;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    public void modifyTestElement(TestElement sampler)
    {
        TestElement el = urlConfigGui.createTestElement();
        sampler.clear();
        sampler.addTestElement(el);
        if (getImages.isSelected())
        {
            ((HTTPSampler2)sampler).setImageParser(true);
        }
        else
        {
            ((HTTPSampler2)sampler).setImageParser(false);
        }
        if (isMon.isSelected()){
			((HTTPSampler2)sampler).setMonitor("true");
        } else {
			((HTTPSampler2)sampler).setMonitor("false");
        }
        this.configureTestElement(sampler);
    }

    public String getStaticLabel()
	{
    	return super.getStaticLabel()+" HTTPCLient (ALPHA)";
    }
    public String getLabelResource()
    {
        return "web_testing_title";
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
        HorizontalPanel optionalTasksPanel = new HorizontalPanel();
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
        JPanel isMonitorPanel = new JPanel();
        isMon = new JCheckBox(
            JMeterUtils.getResString("monitor_is_title"));
        isMonitorPanel.add(isMon);
        optionalTasksPanel.add(retrieveImagesPanel);
		optionalTasksPanel.add(isMonitorPanel);
        return optionalTasksPanel;
    }
        
    public Dimension getPreferredSize()
    {
        return getMinimumSize();
    }

    public static class Test extends TestCase
    {
        HttpTestSampleGui2 gui;
        
        public Test(String name)
        {
            super(name);
        }
        
        public void setUp()
        {
            gui = new HttpTestSampleGui2();
        }
        
        public void testCloneSampler() throws Exception
        {
            HTTPSampler2 sampler = (HTTPSampler2)gui.createTestElement();
            sampler.addArgument("param","value");
            HTTPSampler2 clonedSampler = (HTTPSampler2)sampler.clone();
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
