package org.apache.jmeter.config.gui;

import java.util.Arrays;
import java.util.Collection;

import javax.swing.JPopupMenu;
import org.apache.jmeter.gui.AbstractJMeterGuiComponent;
import org.apache.jmeter.gui.NamePanel;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.testelement.TestElement;

/**
 * Title:        JMeter
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      Apache
 * @author Michael Stover
 * @version 1.0
 */

public abstract class AbstractModifierGui extends AbstractJMeterGuiComponent
{


  public JPopupMenu createPopupMenu()
  {
	 return MenuFactory.getDefaultModifierMenu();
  }

  public Collection getMenuCategories()
  {
	 return Arrays.asList(new String[]{MenuFactory.MODIFIERS});
  }
}