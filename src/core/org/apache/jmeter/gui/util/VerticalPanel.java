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
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * @author ano ano
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class VerticalPanel extends JPanel
{
    private Box subPanel = Box.createVerticalBox();
    private float horizontalAlign;
    
    public VerticalPanel()
    {
        this(LEFT_ALIGNMENT);
    }
    
    public VerticalPanel(float horizontalAlign) {
        super(new BorderLayout());
        add(subPanel,BorderLayout.NORTH);
        this.horizontalAlign = horizontalAlign;
    }
    
    

    /* (non-Javadoc)
     * @see java.awt.Container#add(java.awt.Component)
     */
    public Component add(Component c)
    {
        if (c instanceof JComponent) {
            ((JComponent)c).setAlignmentX(horizontalAlign);
        }
        return subPanel.add(c);
    }

}
