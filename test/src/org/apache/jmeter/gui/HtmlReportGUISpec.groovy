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

package org.apache.jmeter.gui;

import org.apache.jmeter.gui.tree.JMeterTreeListener
import org.apache.jmeter.gui.tree.JMeterTreeModel
import org.apache.jmeter.junit.spock.JMeterSpec

import spock.lang.IgnoreIf

@IgnoreIf({ JMeterSpec.isHeadless() })
class HtmlReportGUISpec extends JMeterSpec{

    def "test HtmlReportUI initialization"(){
        given:
            def HtmlReportUI htmlReportPanel = new HtmlReportUI();
            def JMeterTreeModel treeModel = new JMeterTreeModel();
            def JMeterTreeListener treeListener = new JMeterTreeListener(treeModel);
            GuiPackage.initInstance(treeListener, treeModel);
            GuiPackage.getInstance().setMainFrame(new MainFrame(treeModel, treeListener));
        when:
            htmlReportPanel.showInputDialog(GuiPackage.getInstance().getMainFrame())
            Thread.sleep(50) // https://bugs.openjdk.java.net/browse/JDK-5109571
            htmlReportPanel.messageDialog.setVisible(false)
        then:
            "" == htmlReportPanel.csvFilePathTextField.getText()
            "" == htmlReportPanel.userPropertiesFilePathTextField.getText()
            "" == htmlReportPanel.outputDirectoryPathTextField.getText()
            "" == htmlReportPanel.reportArea.getText()
            1 == htmlReportPanel.messageDialog.getComponents().length;
    }
}
