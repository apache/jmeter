//$Header:
/*
 * Copyright 2005 The Apache Software Foundation.
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
package org.apache.jmeter.report.gui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.ReportPage;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;

/**
 * @author pete
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ReportPageGui extends AbstractReportGui {
    
    private JLabeledTextField pageTitle = new JLabeledTextField(JMeterUtils.getResString("report_page_title"));;

    private JCheckBox makeIndex = new JCheckBox(JMeterUtils.getResString("report_page_index"));
    
    private JLabeledTextField cssURL = 
        new JLabeledTextField(JMeterUtils.getResString("report_page_style_url"));
    
    private JLabeledTextField headerURL = 
        new JLabeledTextField(JMeterUtils.getResString("report_page_header"));
        
    private JLabeledTextField footerURL = 
        new JLabeledTextField(JMeterUtils.getResString("report_page_footer"));

    /**
	 * 
	 */
	public ReportPageGui() {
		init();
	}

    /**
     * Initialize the components and layout of this component.
     */
    private void init() {
        setLayout(new BorderLayout(10, 10));
        setBorder(makeBorder());
        setBackground(Color.white);

        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout(10,10));
        pane.setBackground(Color.white);
        pane.add(this.getNamePanel(),BorderLayout.NORTH);
        
        VerticalPanel options = new VerticalPanel(Color.white);
        pageTitle.setBackground(Color.white);
        makeIndex.setBackground(Color.white);
        cssURL.setBackground(Color.white);
        headerURL.setBackground(Color.white);
        footerURL.setBackground(Color.white);
        options.add(pageTitle);
        options.add(makeIndex);
        options.add(cssURL);
        options.add(headerURL);
        options.add(footerURL);
        add(pane,BorderLayout.NORTH);
        add(options,BorderLayout.CENTER);
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
     */
    public TestElement createTestElement() {
        ReportPage element = new ReportPage();
        modifyTestElement(element);
        return element;
    }

	/* (non-Javadoc)
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(org.apache.jmeter.testelement.TestElement)
	 */
	public void modifyTestElement(TestElement element) {
        super.configureTestElement(element);
	}

}
