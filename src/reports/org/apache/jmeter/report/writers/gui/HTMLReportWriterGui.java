/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
package org.apache.jmeter.report.writers.gui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.apache.jmeter.gui.util.ReportFilePanel;
import org.apache.jmeter.gui.util.ReportMenuFactory;
import org.apache.jmeter.report.gui.AbstractReportGui;
import org.apache.jmeter.report.writers.HTMLReportWriter;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

public class HTMLReportWriterGui extends AbstractReportGui {

    private static final long serialVersionUID = 240L;

    private ReportFilePanel outputDirectory = new ReportFilePanel(
            JMeterUtils.getResString("report_output_directory"), "*"); // $NON-NLS-1$  // $NON-NLS-2$

    public HTMLReportWriterGui() {
        super();
        init();
    }

    @Override
    public String getLabelResource() {
        return "report_writer_html";
    }

    @Override
    public JPopupMenu createPopupMenu() {
        JPopupMenu pop = new JPopupMenu();
        ReportMenuFactory.addFileMenu(pop);
        ReportMenuFactory.addEditMenu(pop,true);
        return pop;
    }

    /**
     * init creates the necessary gui stuff.
     */
    private void init() {// called from ctor, so must not be overridable
        setLayout(new BorderLayout(10, 10));
        setBorder(makeBorder());
        setBackground(Color.white);

        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout(10,10));
        pane.setBackground(Color.white);
        pane.add(this.getNamePanel(),BorderLayout.NORTH);

        outputDirectory.setBackground(Color.white);

        pane.add(outputDirectory,BorderLayout.SOUTH);
        add(pane,BorderLayout.NORTH);
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
     */
    @Override
    public TestElement createTestElement() {
        HTMLReportWriter element = new HTMLReportWriter();
        modifyTestElement(element);
        return element;
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(org.apache.jmeter.testelement.TestElement)
     */
    @Override
    public void modifyTestElement(TestElement element) {
        this.configureTestElement(element);
        HTMLReportWriter wr = (HTMLReportWriter)element;
        wr.setTargetDirectory(outputDirectory.getFilename());
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        HTMLReportWriter wr = (HTMLReportWriter)element;
        outputDirectory.setFilename(wr.getTargetDirectory());
    }
}
