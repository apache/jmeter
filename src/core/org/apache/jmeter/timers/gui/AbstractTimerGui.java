package org.apache.jmeter.timers.gui;

import java.util.Arrays;
import java.util.Collection;

import javax.swing.JPopupMenu;

import org.apache.jmeter.gui.AbstractJMeterGuiComponent;
import org.apache.jmeter.gui.util.MenuFactory;

/**
 * This is the base class for JMeter GUI components which manage timers.
 * Copyright: 2003
 *
 * @author    Michael Stover
 * @version   $Revision$
 */
public abstract class AbstractTimerGui extends AbstractJMeterGuiComponent
{
    /**
     * When a user right-clicks on the component in the test tree, or
     * selects the edit menu when the component is selected, the 
     * component will be asked to return a JPopupMenu that provides
     * all the options available to the user from this component.
     * <p>
     * This implementation returns menu items appropriate for most
     * timer components.
     *
     * @return   a JPopupMenu appropriate for the component.
     */
    public JPopupMenu createPopupMenu()
    {
        return MenuFactory.getDefaultTimerMenu();
    }

    /**
     * This is the list of menu categories this gui component will be available
     * under. This implementation returns
     * {@link org.apache.jmeter.gui.util.MenuFactory#TIMERS}, which
     * is appropriate for most timer components.
     *
     * @return   a Collection of Strings, where each element is one of the
     *           constants defined in MenuFactory
     */
    public Collection getMenuCategories()
    {
        return Arrays.asList(new String[]{MenuFactory.TIMERS});
    }    
}