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

/**
 * @author ano ano
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class VerticalPanel extends JPanel
{
    Box subPanel = Box.createVerticalBox();
    
    public VerticalPanel()
    {
        super(new BorderLayout());
        add(subPanel,BorderLayout.NORTH);
    }
    
    

    /* (non-Javadoc)
     * @see java.awt.Container#add(java.awt.Component)
     */
    public Component add(Component arg0)
    {
        return subPanel.add(arg0);
    }

}
