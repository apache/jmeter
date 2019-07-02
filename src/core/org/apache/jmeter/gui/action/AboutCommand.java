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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.util.EscapeDialog;
import org.apache.jmeter.util.JMeterUtils;

/**
 * About Command. It may be extended in the future to add a list of installed
 * protocols, config options, etc.
 */
public class AboutCommand extends AbstractAction {
    private static final Set<String> commandSet;

    private static JDialog about;

    static {
        Set<String> commands = new HashSet<>();
        commands.add(ActionNames.ABOUT);
        commandSet = Collections.unmodifiableSet(commands);
    }

    /**
     * Handle the "about" action by displaying the "About Apache JMeter..."
     * dialog box. The Dialog Box is NOT modal, because those should be avoided
     * if at all possible.
     */
    @Override
    public void doAction(ActionEvent e) {
        if (e.getActionCommand().equals(ActionNames.ABOUT)) {
            this.about();
        }
    }

    /**
     * Provide the list of Action names that are available in this command.
     */
    @Override
    public Set<String> getActionNames() {
        return AboutCommand.commandSet;
    }

    /**
     * Called by about button. Raises about dialog. Currently the about box has
     * the product image and the copyright notice. The dialog box is centered
     * over the MainFrame.
     */
    private void about() {
        JFrame mainFrame = GuiPackage.getInstance().getMainFrame();
        JDialog dialog = initDialog(mainFrame);

        // NOTE: these lines center the about dialog in the current window.
        Point p = mainFrame.getLocationOnScreen();
        Dimension d1 = mainFrame.getSize();
        Dimension d2 = dialog.getSize();
        dialog.setLocation(p.x + (d1.width - d2.width) / 2, p.y + (d1.height - d2.height) / 2);
        dialog.setVisible(true);
    }

    /**
     * @param mainFrame {@link JFrame}
     * @return {@link JDialog} initializing it if necessary
     */
    private JDialog initDialog(JFrame mainFrame) {
        if (about != null) {
            return about;
        }
        about = new EscapeDialog(mainFrame, "About Apache JMeter", false);
        about.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                about.setVisible(false);
            }
        });

        JLabel jmeterLogo = new JLabel(JMeterUtils.getImage("jmeter.png"));
        JLabel copyright = new JLabel(JMeterUtils.getJMeterCopyright(), SwingConstants.CENTER);
        JLabel rights = new JLabel("All Rights Reserved.", SwingConstants.CENTER);
        JLabel version = new JLabel("Apache JMeter Version " + JMeterUtils.getJMeterVersion(), SwingConstants.CENTER);
        JLabel releaseNotes = new JLabel("<html><a href=\"https://jmeter.apache.org/changes.html\">Release notes</a></html>", SwingConstants.CENTER);
        releaseNotes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 0) {
                    ActionRouter.getInstance().doActionNow(
                            new ActionEvent(e.getSource(), e.getID(), ActionNames.LINK_RELEASE_NOTES));
                }
            }
        });
        JPanel infos = new JPanel();
        infos.setOpaque(false);
        infos.setLayout(new GridLayout(0, 1));
        infos.setBorder(new EmptyBorder(5, 5, 5, 5));
        infos.add(copyright);
        infos.add(rights);
        infos.add(version);
        infos.add(releaseNotes);
        Container panel = about.getContentPane();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.white);
        panel.add(jmeterLogo, BorderLayout.NORTH);
        panel.add(infos, BorderLayout.SOUTH);
        about.pack();
        return about;
    }
}
