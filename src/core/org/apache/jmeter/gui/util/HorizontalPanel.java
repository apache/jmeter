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
public class HorizontalPanel extends JPanel
{    
    private Box subPanel = Box.createHorizontalBox();
    private float verticalAlign;
    private int hgap;
    
    public HorizontalPanel()
    {
        this(5, CENTER_ALIGNMENT);
    }
    
    public HorizontalPanel(int hgap, float verticalAlign) {
        super(new BorderLayout());
        add(subPanel,BorderLayout.WEST);
        this.hgap = hgap;
        this.verticalAlign = verticalAlign;
    }

    /* (non-Javadoc)
     * @see java.awt.Container#add(java.awt.Component)
     */
    public Component add(Component c)
    {
        // This won't work right if we remove components.  But we don't, so I'm
        // not going to worry about it right now.
        if (hgap > 0 && subPanel.getComponentCount() > 0) {
            subPanel.add(Box.createHorizontalStrut(hgap));
        }
        
        if (c instanceof JComponent) {
            ((JComponent)c).setAlignmentY(verticalAlign);
        }
        return subPanel.add(c);
    }
}
