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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.apache.jmeter.gui.AbstractJMeterGuiComponent;
import org.apache.jmeter.gui.NamePanel;
import org.apache.jmeter.gui.util.ReportMenuFactory;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

/**
 *
 * This is the abstract base for report gui's
 */
public abstract class AbstractReportGui extends AbstractJMeterGuiComponent
{
    private static final long serialVersionUID = 240L;

    /**
     *
     */
    public AbstractReportGui() {
        this.namePanel = new NamePanel();
        this.namePanel.setBackground(Color.white);
        setName(getStaticLabel());
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.gui.JMeterGUIComponent#getLabelResource()
     */
    public String getLabelResource() {
        return "report_page";
    }

    @Override
    public void configureTestElement(TestElement element) {
        super.configureTestElement(element);
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.gui.JMeterGUIComponent#createPopupMenu()
     */
    public JPopupMenu createPopupMenu() {
        JPopupMenu pop = new JPopupMenu();
        JMenu addMenu = new JMenu(JMeterUtils.getResString("Add"));
        addMenu.add(ReportMenuFactory.makeMenu(ReportMenuFactory.CONFIG_ELEMENTS, "Add"));
        addMenu.add(ReportMenuFactory.makeMenu(ReportMenuFactory.PRE_PROCESSORS, "Add"));
        addMenu.add(ReportMenuFactory.makeMenu(ReportMenuFactory.POST_PROCESSORS, "Add"));
        pop.add(addMenu);
        ReportMenuFactory.addFileMenu(pop);
        return pop;
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.gui.JMeterGUIComponent#getMenuCategories()
     */
    public Collection<String> getMenuCategories() {
        return Arrays.asList(new String[] { ReportMenuFactory.TABLES });
    }

    /**
     * This implementaion differs a bit from the normal jmeter gui. it uses
     * a white background instead of the default grey.
     * @return a panel containing the component title and name panel
     */
    @Override
    protected Container makeTitlePanel() {
        VerticalPanel titlePanel = new VerticalPanel();
        titlePanel.setBackground(Color.white);
        titlePanel.add(createTitleLabel());
        titlePanel.add(getNamePanel());
        return titlePanel;
    }

    /**
     * This implementaion differs a bit from the normal jmeter gui. it uses
     * a white background instead of the default grey.
     */
    @Override
    protected Component createTitleLabel() {
        JLabel titleLabel = new JLabel(getStaticLabel());
        Font curFont = titleLabel.getFont();
        titleLabel.setFont(curFont.deriveFont((float) curFont.getSize() + 4));
        titleLabel.setBackground(Color.white);
        return titleLabel;
    }
}
