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

import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JScrollPane;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.util.EscapeDialog;
import org.apache.jmeter.swing.HtmlPane;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.ComponentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the Help menu item.
 */
public class Help extends AbstractAction {
    private static final Logger log = LoggerFactory.getLogger(Help.class);

    private static final Set<String> commands = new HashSet<>();

    private static final String HELP_DOCS = "file:///"  // $NON-NLS-1$
        + JMeterUtils.getJMeterHome()
        + "/printable_docs/usermanual/"; // $NON-NLS-1$

    private static final String HELP_PAGE = HELP_DOCS + "component_reference.html"; // $NON-NLS-1$

    public static final String HELP_FUNCTIONS = HELP_DOCS + "functions.html"; // $NON-NLS-1$

    private static JDialog helpWindow;

    private static final HtmlPane helpDoc;

    private static final JScrollPane scroller;

    static {
        commands.add(ActionNames.HELP);
        helpDoc = new HtmlPane();
        scroller = new JScrollPane(helpDoc);
        helpDoc.setEditable(false);
    }

    /**
     * @see org.apache.jmeter.gui.action.Command#doAction(ActionEvent)
     */
    @Override
    public void doAction(ActionEvent e) {
        JDialog dialog = initHelpWindow();
        dialog.setVisible(true); // set the window visible immediately

        /*
         * This means that a new page will be shown before rendering is complete,
         * however the correct location will be displayed.
         * Attempts to use a "page" PropertyChangeListener to detect when the page
         * has been loaded failed to work any better. 
         */
        StringBuilder url=new StringBuilder();
        if (e.getSource() instanceof String[]) {
            String[] source = (String[]) e.getSource();
            url.append(source[0]).append('#').append(source[1]);
        } else {
            url.append(HELP_PAGE).append('#').append(GuiPackage.getInstance().getTreeListener().getCurrentNode().getDocAnchor());
        }
        try {
            helpDoc.setPage(url.toString()); // N.B. this only reloads if necessary (ignores the reference)
        } catch (IOException ioe) {
            log.error("Error setting page for url, {}", url, ioe);
            helpDoc.setText("<html><head><title>Problem loading help page</title>"
                    + "<style><!--"
                    + ".note { background-color: #ffeeee; border: 1px solid brown; }"
                    + "div { padding: 10; margin: 10; }"
                    + "--></style></head>"
                    + "<body><div class='note'>"
                    + "<h1>Problem loading help page</h1>"
                    + "<div>Can't load url: &quot;<em>"
                    + url.toString() + "</em>&quot;</div>"
                    + "<div>See log for more info</div>"
                    + "</body>");
        }
    }

    /**
     * @return {@link JDialog} Help window and initializes it if necessary
     */
    private static JDialog initHelpWindow() {
        if (helpWindow == null) {
            helpWindow = new EscapeDialog(new Frame(),// independent frame to
                                                    // allow it to be overlaid
                                                    // by the main frame
                    JMeterUtils.getResString("help"),//$NON-NLS-1$
                    false);
            helpWindow.getContentPane().setLayout(new GridLayout(1, 1));
            helpWindow.getContentPane().removeAll();
            helpWindow.getContentPane().add(scroller);
            ComponentUtil.centerComponentInWindow(helpWindow, 60);
        }
        return helpWindow;
    }

    /**
     * @see org.apache.jmeter.gui.action.Command#getActionNames()
     */
    @Override
    public Set<String> getActionNames() {
        return commands;
    }
}
