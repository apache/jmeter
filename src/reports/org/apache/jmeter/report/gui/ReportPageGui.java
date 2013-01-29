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
package org.apache.jmeter.report.gui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JCheckBox;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.apache.jmeter.gui.util.ReportMenuFactory;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.ReportPage;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextArea;
import org.apache.jorphan.gui.JLabeledTextField;

public class ReportPageGui extends AbstractReportGui {

    private static final long serialVersionUID = 240L;

    private JLabeledTextField pageTitle = new JLabeledTextField(JMeterUtils.getResString("report_page_title")); // $NON-NLS-1$

    private JCheckBox makeIndex = new JCheckBox(JMeterUtils.getResString("report_page_index")); // $NON-NLS-1$

    private JLabeledTextField cssURL =
        new JLabeledTextField(JMeterUtils.getResString("report_page_style_url")); // $NON-NLS-1$

    private JLabeledTextField headerURL =
        new JLabeledTextField(JMeterUtils.getResString("report_page_header")); // $NON-NLS-1$

    private JLabeledTextField footerURL =
        new JLabeledTextField(JMeterUtils.getResString("report_page_footer")); // $NON-NLS-1$

    private JLabeledTextArea introduction =
        new JLabeledTextArea(JMeterUtils.getResString("report_page_intro")); // $NON-NLS-1$

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
        introduction.setBackground(Color.white);
        options.add(pageTitle);
        options.add(makeIndex);
        options.add(cssURL);
        options.add(headerURL);
        options.add(footerURL);
        options.add(introduction);
        add(pane,BorderLayout.NORTH);
        add(options,BorderLayout.CENTER);
    }

    @Override
    public JPopupMenu createPopupMenu() {
        JPopupMenu pop = new JPopupMenu();
        JMenu addMenu = new JMenu(JMeterUtils.getResString("add")); // $NON-NLS-1$
        addMenu.add(ReportMenuFactory.makeMenuItem(new TableGui().getStaticLabel(),
                TableGui.class.getName(),
                "Add"));
        addMenu.add(ReportMenuFactory.makeMenuItem(new BarChartGui().getStaticLabel(),
                BarChartGui.class.getName(),
                "Add"));
        addMenu.add(ReportMenuFactory.makeMenuItem(new LineGraphGui().getStaticLabel(),
                LineGraphGui.class.getName(),
                "Add"));
        pop.add(addMenu);
        ReportMenuFactory.addFileMenu(pop);
        ReportMenuFactory.addEditMenu(pop,true);
        return pop;
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
     */
    @Override
    public TestElement createTestElement() {
        ReportPage element = new ReportPage();
        modifyTestElement(element);
        return element;
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(org.apache.jmeter.testelement.TestElement)
     */
    @Override
    public void modifyTestElement(TestElement element) {
        super.configureTestElement(element);
        ReportPage page = (ReportPage)element;
        page.setCSS(cssURL.getText());
        page.setFooterURL(footerURL.getText());
        page.setHeaderURL(headerURL.getText());
        page.setIndex(String.valueOf(makeIndex.isSelected()));
        page.setIntroduction(introduction.getText());
        page.setTitle(pageTitle.getText());
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        ReportPage page = (ReportPage)element;
        cssURL.setText(page.getCSS());
        footerURL.setText(page.getFooterURL());
        headerURL.setText(page.getHeaderURL());
        makeIndex.setSelected(page.getIndex());
        introduction.setText(page.getIntroduction());
        pageTitle.setText(page.getTitle());
    }
}
