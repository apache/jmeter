// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/

package org.apache.jmeter.ejb.jndi.config.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;
import javax.naming.InitialContext;
import java.awt.*;
import java.util.*;

import org.apache.jmeter.gui.ModelSupported;
import org.apache.jmeter.gui.NamePanel;
import org.apache.jmeter.gui.VerticalLayout;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.ejb.jndi.config.JndiConfig;
import org.apache.log4j.Category;

/**
 * Provides the gui interface to configure JNDI sampling
 * @author	Khor Soon Hin
 * @version	$Revision$  Last updated: $Date$
 * Created	    2001 Dec 17
 */
public class JndiConfigGui extends JPanel implements ModelSupported,
	KeyListener
{
  private static Category catClass = Category.getInstance(
	JndiConfigGui.class.getName());

  protected JTextField[] jndi_fields = new JTextField[JndiConfig.JNDI_PROPS.length];

  protected NamePanel namePanel;
  protected boolean displayName;
  protected JndiConfig model;

  public JndiConfigGui()
  {
    displayName = true;
  }

  public JndiConfigGui(boolean displayName)
  {
    this.displayName = displayName;
  }

  public void setModel(Object model)
  {
    this.model = (JndiConfig)model;
    init();
  }

  public void updateGui()
  {
    for(int i = 0; i < JndiConfig.JNDI_PROPS.length; i++)
    {
      jndi_fields[i].setText(model.getValue(i)); 
    }
  }

  protected void init()
  {
    for(int i = 0; i < JndiConfig.JNDI_PROPS.length; i++)
    {
      jndi_fields[i] = new JTextField(20);
    }
    if(displayName)
    {
      this.setLayout(new VerticalLayout(5, VerticalLayout.LEFT, 
	VerticalLayout.TOP));
      // main panel
      JPanel mainPanel = new JPanel();
      Border margin = new EmptyBorder(10, 10, 5, 10);
      mainPanel.setBorder(margin);
      mainPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));

      mainPanel.add(makeTitlePanel());

      // jndi properties
      JPanel urlJNDIPanel = new JPanel();
      urlJNDIPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));
      urlJNDIPanel.setBorder(BorderFactory.createTitledBorder(
	JMeterUtils.getResString("jndi_url_jndi_props")));

      for(int i = 0; i < JndiConfig.JNDI_PROPS.length; i++)
      {
        urlJNDIPanel.add(getPanel(i));
      }

      mainPanel.add(urlJNDIPanel);
      this.add(mainPanel);
    }
    else
    {
      this.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));

      // url and driver class
      JPanel urlJNDIPanel = new JPanel();
      urlJNDIPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));
      urlJNDIPanel.setBorder(BorderFactory.createTitledBorder(
	JMeterUtils.getResString("jndi_url_jndi_props")));

      for(int i = 0; i < JndiConfig.JNDI_PROPS.length; i++)
      {
        urlJNDIPanel.add(getPanel(i));
      }

      this.add(urlJNDIPanel);
    }
  }

  protected JPanel getPanel(int i)
  {
    catClass.info("Start : getPanel1");
    if(catClass.isDebugEnabled())
    {
      catClass.debug("getPanel1 : Panel no. - " + i);
      catClass.debug("getPanel1 : Panel name - " + JndiConfig.JNDI_PROPS[i]);
      catClass.debug("getPanel1 : Panel value - " + model.getValue(i));
    }
    JPanel panel = new JPanel();
    panel.add(new JLabel(JndiConfig.JNDI_PROPS[i]));
    jndi_fields[i].setText(model.getValue(i));
    jndi_fields[i].setName(JndiConfig.JNDI_PROPS[i]);
    jndi_fields[i].addKeyListener(this);
    panel.add(jndi_fields[i]);
    catClass.info("End : getPanel1");
    return panel;
  }

  public void keyPressed(KeyEvent e)
  {
  }

  public void keyTyped(KeyEvent e)
  {
  }

  public void keyReleased(KeyEvent e)
  {
    String name = e.getComponent().getName();
    int i = 0;
    while(i < JndiConfig.JNDI_PROPS.length) 
    {
      if(name.equals(JndiConfig.JNDI_PROPS[i]))
      {
        break;
      }
      i++;
    }
    if(i < JndiConfig.JNDI_PROPS.length)
    {
      model.setValue(i, jndi_fields[i].getText());
    }
  }
}
