/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001,2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
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
import javax.swing.border.EmptyBorder;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.util.JMeterUtils;

/**
 * About Command.  It may be extended in the future to add a list of
 * installed protocols, config options, etc.
 *
 * @author <a href="bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision$ $Date$
 */
public class AboutCommand implements Command
{
    private static Set commandSet;
    private static JDialog about;

    static
    {
        HashSet commands = new HashSet();
        commands.add("about");
        AboutCommand.commandSet = Collections.unmodifiableSet(commands);
    }

    /**
     * Handle the "about" action by displaying the "About Apache JMeter..."
     * dialog box.  The Dialog Box is NOT modal, because those should be avoided
     * if at all possible.
     */
    public void doAction(ActionEvent e)
    {
        if (e.getActionCommand().equals("about"))
        {
            this.about();
        }
    }

    /**
     * Provide the list of Action names that are available in this command.
     */
    public Set getActionNames()
    {
        return AboutCommand.commandSet;
    }

    /**
     * Called by about button. Raises about dialog.  Currently the about box has
     * the product image and the copyright notice.  The dialog box is centered
     * over the MainFrame.
     */
    void about()
    {
        JFrame mainFrame = GuiPackage.getInstance().getMainFrame();
        if (about == null)
        {
            about = new JDialog(mainFrame, "About Apache JMeter...", false);
            about.addMouseListener(new MouseAdapter()
            {
                public void mouseClicked(MouseEvent e)
                {
                    about.setVisible(false);
                }
            });

            JLabel jmeter = new JLabel(JMeterUtils.getImage("jmeter.jpg"));
            JLabel copyright =
                new JLabel(
                    "Copyright (c) 1998-2003 The Apache Software Foundation",
                    JLabel.CENTER);
            JLabel rights = new JLabel("All Rights Reserved.", JLabel.CENTER);
            JLabel version =
                new JLabel(
                    "Apache JMeter Version " + JMeterUtils.getJMeterVersion(),
                    JLabel.CENTER);
            JPanel infos = new JPanel();
            infos.setOpaque(false);
            infos.setLayout(new GridLayout(0, 1));
            infos.setBorder(new EmptyBorder(5, 5, 5, 5));
            infos.add(copyright);
            infos.add(rights);
            infos.add(version);
            Container panel = about.getContentPane();
            panel.setLayout(new BorderLayout());
            panel.setBackground(Color.white);
            panel.add(jmeter, BorderLayout.NORTH);
            panel.add(infos, BorderLayout.SOUTH);
        }

        // NOTE: these lines center the about dialog in the
        // current window. Some older Swing versions have
        // a bug in getLocationOnScreen() and they may not
        // make this behave properly.
        Point p = mainFrame.getLocationOnScreen();
        Dimension d1 = mainFrame.getSize();
        Dimension d2 = about.getSize();
        about.setLocation(
            p.x + (d1.width - d2.width) / 2,
            p.y + (d1.height - d2.height) / 2);
        about.pack();
        about.setVisible(true);
    }
}
