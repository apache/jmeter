package org.apache.jmeter.gui;

import org.apache.jmeter.gui.tree.JMeterCellRenderer
import org.apache.jmeter.gui.tree.JMeterTreeListener
import org.apache.jmeter.gui.tree.JMeterTreeModel
import org.apache.jmeter.junit.spock.JMeterSpec

import spock.lang.IgnoreIf


@IgnoreIf({
    Boolean.valueOf(System.properties['java.awt.headless'])
})
class HtmlReportGUITest extends JMeterSpec{

    def "test HtmlReportPanel initialization should throw NullPointerException when GuiPackage is not initialized"() {
        given:
        def htmlReportPanel = new HtmlReportPanel()
        when:
        htmlReportPanel.showInputDialog()
        then:
        thrown(NullPointerException)
    }


    def "test HtmlReportPanel initialization"(){
        given:
        def HtmlReportPanel htmlReportPanel = new HtmlReportPanel();
        def JMeterTreeModel treeModel = new JMeterTreeModel();
        def JMeterTreeListener treeListener = new JMeterTreeListener(treeModel);
        GuiPackage.initInstance(treeListener, treeModel);
        GuiPackage.getInstance().setMainFrame(new MainFrame(treeModel, treeListener));
        when:
        htmlReportPanel.showInputDialog()
        htmlReportPanel.getMessageDialog().setVisible(false)
        then:
        "".equals(htmlReportPanel.getCsvFilePathTextField().getText());
        "".equals(htmlReportPanel.getUserPropertiesFilePathTextField().getText());
        "".equals(htmlReportPanel.getOutputDirectoryPathTextField().getText());
        "".equals(htmlReportPanel.getReportingArea().getText())
        1 == htmlReportPanel.getMessageDialog().getComponents().length;
    }
}