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

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.util.JMeterUtils;

/**
 * @author     Brendan Burns
 * @created    October 5, 2001
 * @version    $Revision$
 */
public class ExitCommand implements Command
{

    public static final String EXIT = "exit";
    private static Set commands = new HashSet();
    static {
        commands.add(EXIT);
    }

    /**
     *  Constructor for the ExitCommand object
     */
    public ExitCommand()
    {}

    /**
     *  Gets the ActionNames attribute of the ExitCommand object
     *
     *@return    The ActionNames value
     */
    public Set getActionNames()
    {
        return commands;
    }

    /**
     *  Description of the Method
     *
     *@param  e  Description of Parameter
     */
    public void doAction(ActionEvent e)
    {
        ActionRouter.getInstance().actionPerformed(
            new ActionEvent(e.getSource(), e.getID(), CheckDirty.CHECK_DIRTY));
        if (GuiPackage.getInstance().isDirty())
        {
            int chosenOption =
                JOptionPane.showConfirmDialog(
                    GuiPackage.getInstance().getMainFrame(),
                    JMeterUtils.getResString("cancel_exit_to_save"),
                    JMeterUtils.getResString("Save?"),
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (chosenOption == JOptionPane.NO_OPTION)
            {
                System.exit(0);
            }
            else if (chosenOption == JOptionPane.YES_OPTION)
            {
                ActionRouter.getInstance().doActionNow(
                    new ActionEvent(e.getSource(), e.getID(), Save.SAVE_ALL));
                if (!GuiPackage.getInstance().isDirty())
                {
                    System.exit(0);
                }
            }
        }
        else
        {
            System.exit(0);
        }
    }
}
