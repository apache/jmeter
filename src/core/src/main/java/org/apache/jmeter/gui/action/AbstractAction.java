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

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.MessageFormat;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.SearchByClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractAction implements Command {
    private static final Logger log = LoggerFactory.getLogger(AbstractAction.class);

    private enum ActionOnFile {
        APPEND,
        DELETE,
        ASK
    }

    private static final ActionOnFile actionOnFile =
            ActionOnFile.valueOf(
                    JMeterUtils.getPropDefault(
                            "resultcollector.action_if_file_exists",
                            ActionOnFile.ASK.name()));

    /**
     * @see Command#doAction(ActionEvent)
     */
    @Override
    public void doAction(ActionEvent e) throws IllegalUserActionException {
    }

    /**
     * Check if we should save before run
     *
     * @param e
     *            the event that led to the call of this method
     */
    protected void popupShouldSave(ActionEvent e) {
        log.debug("popupShouldSave");
        if (GuiPackage.getInstance().getTestPlanFile() == null) {
            if (JOptionPane.showConfirmDialog(GuiPackage.getInstance().getMainFrame(),
                    JMeterUtils.getResString("should_save"), //$NON-NLS-1$
                    JMeterUtils.getResString("warning"), //$NON-NLS-1$
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                ActionRouter.getInstance().doActionNow(new ActionEvent(e.getSource(), e.getID(), ActionNames.SAVE));
            }
        } else if (GuiPackage.getInstance().shouldSaveBeforeRun()) {
            ActionRouter.getInstance().doActionNow(new ActionEvent(e.getSource(), e.getID(), ActionNames.SAVE));
        }
    }

    /**
     * @param tree where check if listener has existing file
     * @return true if continue test, false otherwise
     */
    protected boolean popupCheckExistingFileListener(HashTree tree) {

        SearchByClass<ResultCollector> resultListeners = new SearchByClass<>(ResultCollector.class);
        tree.traverse(resultListeners);
        for (ResultCollector rc : resultListeners.getSearchResults()) {
            File f = new File(rc.getFilename());
            if (f.exists()) {
                switch (actionOnFile) {
                    case APPEND:
                        break;
                    case DELETE:
                        if (f.delete()) {
                            break;
                        } else {
                            log.error("Could not delete existing file {}", f.getAbsolutePath());
                            return false;
                        }
                    case ASK:
                    default:
                        String[] option = new String[]{JMeterUtils.getResString("concat_result"),
                                JMeterUtils.getResString("dont_start"), JMeterUtils.getResString("replace_file")};
                        String question = MessageFormat.format(
                                JMeterUtils.getResString("ask_existing_file"), // $NON-NLS-1$
                                rc.getFilename());
                        // Interactive question
                        int response = JOptionPane.showOptionDialog(GuiPackage.getInstance().getMainFrame(),
                                question, JMeterUtils.getResString("warning"),
                                JOptionPane.YES_NO_CANCEL_OPTION,
                                JOptionPane.WARNING_MESSAGE,
                                null,
                                option,
                                option[0]);

                        switch (response) {
                            case JOptionPane.CANCEL_OPTION:
                                // replace_file so delete the existing one
                                if (f.delete()) {
                                    break;
                                } else {
                                    log.error("Could not delete existing file {}", f.getAbsolutePath());
                                    return false;
                                }
                            case JOptionPane.YES_OPTION:
                                // append is the default behaviour, so nothing to do
                                break;
                            case JOptionPane.NO_OPTION:
                            default:
                                // Exit without start the test
                                return false;
                        }
                        break;
                }
            }
        }
        return true;
    }

    /**
     * @param event {@link ActionEvent}
     * @return parent Window
     */
    protected final JFrame getParentFrame(ActionEvent event) {
        JFrame parent = null;
        Object source = event.getSource();
        if (source instanceof JMenuItem) {
            JMenuItem item = (JMenuItem) source;
            Component comp = item.getParent();
            if (comp instanceof JPopupMenu) {
                JPopupMenu popup = (JPopupMenu) comp;
                comp = popup.getInvoker();
                Window window = SwingUtilities.windowForComponent((Component) comp);
                if (window instanceof JFrame) {
                    parent = (JFrame) window;
                }
            }
        } else {
            parent = GuiPackage.getInstance().getMainFrame();
        }
        return parent;
    }
}
