package org.apache.jmeter.gui.util;
import java.awt.Component;

import javax.swing.SwingUtilities;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/**
 * Note: This helper class appeared in JavaWorld in June 2001
 * (http://www.javaworld.com) and was written by Michael Daconta.
 *
 * @author    Kevin Hammond
 * @version   $Revision$
 */
public class FocusRequester implements Runnable
{
    transient private static Logger log =
        Hierarchy.getDefaultHierarchy().getLoggerFor("jmeter.gui");
    private Component comp;

    public FocusRequester(Component comp)
    {
        this.comp = comp;
        try
        {
            SwingUtilities.invokeLater(this);
        }
        catch (Exception e)
        {
            log.error("", e);
        }
    }

    public void run()
    {
        comp.requestFocus();
    }
}
