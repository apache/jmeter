package org.apache.jmeter.timers.gui;

import java.util.Arrays;
import java.util.Collection;

import javax.swing.JPopupMenu;

import org.apache.jmeter.gui.AbstractJMeterGuiComponent;
import org.apache.jmeter.gui.util.MenuFactory;

/**
 * Title:        JMeter
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      Apache
 * @author Michael Stover
 * @version 1.0
 */
public abstract class AbstractTimerGui extends AbstractJMeterGuiComponent
{
    public JPopupMenu createPopupMenu()
    {
        return MenuFactory.getDefaultTimerMenu();
    }

    public Collection getMenuCategories()
    {
        return Arrays.asList(new String[]{MenuFactory.TIMERS});
    }
    
}