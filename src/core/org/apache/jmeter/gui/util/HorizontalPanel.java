/*
 * Created on Apr 25, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.apache.jmeter.gui.util;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.border.Border;

/**
 * @author ano ano
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class HorizontalPanel extends JPanel
{    
    Box subPanel = Box.createHorizontalBox();

    public HorizontalPanel()
    {
        super(new BorderLayout());
        add(subPanel,BorderLayout.WEST);
    }


    /* (non-Javadoc)
     * @see java.awt.Container#add(java.awt.Component)
     */
    public Component add(Component arg0)
    {
        return subPanel.add(arg0);
    }

}
