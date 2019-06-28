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

package org.apache.jmeter.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.MenuElement;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.plugin.MenuCreator;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Schematic view of Test Plan
 * @since 5.1
 */
public class SchematicView extends AbstractAction implements MenuCreator {
    private static final Logger log = LoggerFactory.getLogger(SchematicView.class);
    private static final String DEFAULT_XSL_FILE =
            JMeterUtils.getProperty("docgeneration.schematic_xsl"); //$NON-NLS-1$

    private static final MessageFormat GENERATION_SUCCESS_MSG = new MessageFormat(JMeterUtils.getResString("schematic_view_generation_ok"));

    @FunctionalInterface
    public interface SchematicViewGenerator {
        void generate(HashTree testPlan, File testPlanFile, OutputStream outputStream) throws Exception;
    }

    private static final class XslSchematicViewGenerator implements SchematicViewGenerator {
        @Override
        public void generate(HashTree testPlan, File testPlanFile, OutputStream outputStream)
                throws Exception {
            TransformerFactory factory = TransformerFactory.newInstance(
                    "net.sf.saxon.BasicTransformerFactory", Thread.currentThread().getContextClassLoader());
            Source xslt;
            if (!StringUtils.isEmpty(DEFAULT_XSL_FILE)) {
                log.info("Will use file {} for Schematic View generation", DEFAULT_XSL_FILE);
                xslt = new StreamSource(new File(DEFAULT_XSL_FILE));
            } else {
                xslt = new StreamSource(SchematicView.class.getResourceAsStream("/org/apache/jmeter/gui/action/schematic.xsl"));
            }
            Transformer transformer = factory.newTransformer(xslt);
            Source text = new StreamSource(testPlanFile);
            transformer.transform(text, new StreamResult(outputStream));
        }

    }

    private static final Set<String> commands = new HashSet<>();

    static {
        commands.add(ActionNames.SCHEMATIC_VIEW);
    }

    public SchematicView() {
        super();
    }

    /**
     * @see Command#doAction(ActionEvent)
     */
    @Override
    public void doAction(ActionEvent e) {
        try {
            String updateFile = GuiPackage.getInstance().getTestPlanFile();
            if (updateFile != null) {
                ActionRouter.getInstance().doActionNow(new ActionEvent(e.getSource(), e.getID(), ActionNames.CHECK_DIRTY));
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setDialogTitle(JMeterUtils.getResString("schematic_view_outputfile"));
                jFileChooser.setCurrentDirectory(new File(updateFile).getParentFile());
                jFileChooser.setSelectedFile(new File(updateFile+".html"));
                int retVal = jFileChooser.showSaveDialog(GuiPackage.getInstance().getMainFrame());
                if (retVal == JFileChooser.APPROVE_OPTION) {
                    File outputFile = jFileChooser.getSelectedFile();
                    if (outputFile.exists()) {
                        int response = JOptionPane.showConfirmDialog(GuiPackage.getInstance().getMainFrame(),
                                JMeterUtils.getResString("save_overwrite_existing_file"), // $NON-NLS-1$
                                JMeterUtils.getResString("save?"),  // $NON-NLS-1$
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE);
                        if (response == JOptionPane.CLOSED_OPTION || response == JOptionPane.NO_OPTION) {
                            return; // Do not save, user does not want to overwrite
                        }
                    }
                    try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
                            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {
                        new XslSchematicViewGenerator().generate(GuiPackage.getInstance().getCurrentSubTree(),
                            new File(updateFile), bufferedOutputStream);
                    }
                    JMeterUtils.reportInfoToUser(
                            GENERATION_SUCCESS_MSG.format(new Object[]{outputFile.getAbsolutePath()}),
                            JMeterUtils.getResString("schematic_view_info"));
                }
            } else {
                JMeterUtils.reportInfoToUser(JMeterUtils.getResString("schematic_view_no_plan"), JMeterUtils.getResString("schematic_view_info"));
            }
        } catch (Exception ex) {
            JMeterUtils.reportErrorToUser(JMeterUtils.getResString("schematic_view_errors"), ex);
        }
    }


    /**
     * @see Command#getActionNames()
     */
    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    @Override
    public JMenuItem[] getMenuItemsAtLocation(MENU_LOCATION location) {
        if (location == MENU_LOCATION.TOOLS) {

            JMenuItem menuItem = new JMenuItem(
                    JMeterUtils.getResString("schematic_view_menu"), KeyEvent.VK_UNDEFINED);
            menuItem.setName(ActionNames.SCHEMATIC_VIEW);
            menuItem.setActionCommand(ActionNames.SCHEMATIC_VIEW);
            menuItem.setAccelerator(null);
            menuItem.addActionListener(ActionRouter.getInstance());

            return new JMenuItem[]{menuItem};
        }
        return new JMenuItem[0];
    }

    @Override
    public JMenu[] getTopLevelMenus() {
        return new JMenu[0];
    }

    @Override
    public boolean localeChanged(MenuElement menu) {
        return false;
    }

    @Override
    public void localeChanged() {
        // NOOP
    }
}
