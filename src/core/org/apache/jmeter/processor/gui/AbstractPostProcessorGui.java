package org.apache.jmeter.processor.gui;

import java.util.Arrays;
import java.util.Collection;

import javax.swing.JPopupMenu;

import org.apache.jmeter.gui.AbstractJMeterGuiComponent;
import org.apache.jmeter.gui.util.MenuFactory;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public abstract class AbstractPostProcessorGui extends AbstractJMeterGuiComponent
{

    public JPopupMenu createPopupMenu()
   {
      return MenuFactory.getDefaultExtractorMenu();
   }

   public Collection getMenuCategories()
   {
      return Arrays.asList(new String[]{MenuFactory.POST_PROCESSORS});
   }

}
