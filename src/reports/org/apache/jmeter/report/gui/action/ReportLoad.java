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

package org.apache.jmeter.report.gui.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.tree.TreePath;

import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.ReportGuiPackage;
import org.apache.jmeter.gui.action.Command;
import org.apache.jmeter.gui.util.ReportFileDialoger;
import org.apache.jmeter.report.gui.tree.ReportTreeNode;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testelement.ReportPlan;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

public class ReportLoad implements Command {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final Set<String> commands = new HashSet<String>();
    static {
        commands.add("open");
        commands.add("merge");
    }

    public ReportLoad() {
        super();
    }

    public Set<String> getActionNames() {
        return commands;
    }

    public void doAction(ActionEvent e) {
        boolean merging = e.getActionCommand().equals("merge");

        if (!merging) {
            ReportActionRouter.getInstance().doActionNow(
                    new ActionEvent(e.getSource(), e.getID(), "close"));
        }

        JFileChooser chooser = ReportFileDialoger
                .promptToOpenFile(new String[] { ".jmr" });
        if (chooser == null) {
            return;
        }
        boolean isTestPlan = false;
        InputStream reader = null;
        File f = null;
        try {
            f = chooser.getSelectedFile();
            if (f != null) {
                if (merging) {
                    log.info("Merging file: " + f);
                } else {
                    log.info("Loading file: " + f);
                    FileServer.getFileServer().setBaseForScript(f);
                }
                reader = new FileInputStream(f);
                HashTree tree = SaveService.loadTree(reader);
                isTestPlan = insertLoadedTree(e.getID(), tree);
            }
        } catch (NoClassDefFoundError ex) // Allow for missing optional jars
        {
            String msg = ex.getMessage();
            if (msg == null) {
                msg = "Missing jar file - see log for details";
                log.warn("Missing jar file", ex);
            }
            JMeterUtils.reportErrorToUser(msg);
        } catch (Exception ex) {
            String msg = ex.getMessage();
            if (msg == null) {
                msg = "Unexpected error - see log for details";
                log.warn("Unexpected error", ex);
            }
            JMeterUtils.reportErrorToUser(msg);
        } finally {
            JOrphanUtils.closeQuietly(reader);
            ReportGuiPackage.getInstance().updateCurrentGui();
            ReportGuiPackage.getInstance().getMainFrame().repaint();
        }
        // don't change name if merging
        if (!merging && isTestPlan && f != null) {
            ReportGuiPackage.getInstance().setReportPlanFile(f.getAbsolutePath());
        }
    }

    /**
     * Returns a boolean indicating whether the loaded tree was a full test plan
     */
    public boolean insertLoadedTree(int id, HashTree tree) throws Exception,
            IllegalUserActionException {
        // convertTree(tree);
        if (tree == null) {
            throw new Exception("Error in TestPlan - see log file");
        }
        boolean isTestPlan = tree.getArray()[0] instanceof ReportPlan;
        HashTree newTree = ReportGuiPackage.getInstance().addSubTree(tree);
        ReportGuiPackage.getInstance().updateCurrentGui();
        ReportGuiPackage.getInstance().getMainFrame().getTree()
                .setSelectionPath(
                        new TreePath(((ReportTreeNode) newTree.getArray()[0])
                                .getPath()));
        tree = ReportGuiPackage.getInstance().getCurrentSubTree();
        ReportActionRouter.getInstance().actionPerformed(
                new ActionEvent(tree.get(tree.getArray()[tree.size() - 1]), id,
                        ReportCheckDirty.SUB_TREE_LOADED));

        return isTestPlan;
    }
}
