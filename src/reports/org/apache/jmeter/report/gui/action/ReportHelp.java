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

import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JScrollPane;

import org.apache.jmeter.gui.ReportGuiPackage;
import org.apache.jmeter.swing.HtmlPane;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.gui.action.Command;
import org.apache.jorphan.gui.ComponentUtil;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class ReportHelp implements Command {
    private static final Logger log = LoggingManager.getLoggerForClass();

    public static final String HELP = "help";

    private static final Set<String> commands = new HashSet<String>();

    public static final String HELP_DOCS = "file:///" + JMeterUtils.getJMeterHome() + "/printable_docs/usermanual/";

    public static final String HELP_PAGE = HELP_DOCS + "component_reference.html";

    public static final String HELP_FUNCTIONS = HELP_DOCS + "functions.html";

    private static JDialog helpWindow;

    private static HtmlPane helpDoc;

    private static JScrollPane scroller;

    private static String currentPage;

    static {
        commands.add(HELP);
        helpDoc = new HtmlPane();
        scroller = new JScrollPane(helpDoc);
        helpDoc.setEditable(false);
        try {
            helpDoc.setPage(HELP_PAGE);
            currentPage = HELP_PAGE;
        } catch (IOException err) {
            String msg = "Couldn't load help file " + err.toString();
            log.error(msg);
            currentPage = "";// Avoid NPE in resetPage()
        }
    }

    /**
     * @see org.apache.jmeter.gui.action.Command#doAction(ActionEvent)
     */
    public void doAction(ActionEvent e) {
        if (helpWindow == null) {
            helpWindow = new JDialog(new Frame(),// independent frame to
                                                    // allow it to be overlaid
                                                    // by the main frame
                    JMeterUtils.getResString("help"),//$NON-NLS-1$
                    false);
            helpWindow.getContentPane().setLayout(new GridLayout(1, 1));
            ComponentUtil.centerComponentInWindow(helpWindow, 60);
        }
        helpWindow.getContentPane().removeAll();
        helpWindow.getContentPane().add(scroller);
        helpWindow.setVisible(true);
        if (e.getSource() instanceof String[]) {
            String[] source = (String[]) e.getSource();
            resetPage(source[0]);
            helpDoc.scrollToReference(source[1]);
        } else {
            resetPage(HELP_PAGE);
            helpDoc.scrollToReference(ReportGuiPackage.getInstance().getTreeListener().getCurrentNode().getDocAnchor());

        }
    }

    private void resetPage(String source) {
        if (!currentPage.equals(source)) {
            try {
                helpDoc.setPage(source);
                currentPage = source;
            } catch (IOException err) {
                log.error(err.toString());
                JMeterUtils.reportErrorToUser("Problem loading a help page - see log for details");
                currentPage = "";
            }
        }
    }

    /**
     * @see org.apache.jmeter.gui.action.Command#getActionNames()
     */
    public Set<String> getActionNames() {
        return commands;
    }
}
