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
 * @version $Revision$
 */
public class VerticalPanel extends JPanel
{
    private Box subPanel = Box.createVerticalBox();
    private float horizontalAlign;
    private int vgap;
    
    public VerticalPanel()
    {
        this(5, LEFT_ALIGNMENT);
    }
    
    public VerticalPanel(int vgap, float horizontalAlign)
    {
        super(new BorderLayout());
        add(subPanel,BorderLayout.NORTH);
        this.vgap = vgap;
        this.horizontalAlign = horizontalAlign;
    }
    
    /* (non-Javadoc)
     * @see java.awt.Container#add(java.awt.Component)
     */
    public Component add(Component c)
    {
        // This won't work right if we remove components.  But we don't, so I'm
        // not going to worry about it right now.
        if (vgap > 0 && subPanel.getComponentCount() > 0)
        {
            subPanel.add(Box.createVerticalStrut(vgap));
        }
        
        if (c instanceof JComponent)
        {
            ((JComponent)c).setAlignmentX(horizontalAlign);
        }
        
        return subPanel.add(c);
    }
}
