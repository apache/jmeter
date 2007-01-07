/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import org.apache.jmeter.ejb.jndi.config.LookupConfig;
import org.apache.log4j.Category;

/**
 * Provides the gui interface to configure JNDI lookup
 * @author	     Khor Soon Hin
 * @version	$Revision$ Last updated: $Date$
 * Created	    2001 Dec 18
 */
public class LookupConfigGui extends JPanel implements ModelSupported,
	KeyListener
{
  private static Category catClass = Category.getInstance(
	LookupConfigGui.class.getName());

  protected JTextField lookupField;

  protected NamePanel namePanel;
  protected boolean displayName;
  protected LookupConfig model;

  public LookupConfigGui()
  {
    displayName = true;
  }

  public LookupConfigGui(boolean displayName)
  {
    this.displayName = displayName;
  }

  public void setModel(Object model)
  {
    this.model = (LookupConfig)model;
    init();
  }

  public void updateGui()
  {
    lookupField.setText(model.getLookupName());
    if(displayName)
    {
      namePanel.updateGui();
    }
  }

  protected void init()
  {
    lookupField = new JTextField(20);
    if(displayName)
    {
      this.setLayout(new VerticalLayout(5, VerticalLayout.LEFT, 
	VerticalLayout.TOP));
      // main panel
      JPanel mainPanel = new JPanel();
      Border margin = new EmptyBorder(10, 10, 5, 10);
      mainPanel.setBorder(margin);
      mainPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));

      // title
      JLabel panelTitleLabel = new JLabel(
	JMeterUtils.getResString("jndi_lookup_title"));
      Font curFont = panelTitleLabel.getFont();
      int curFontSize = curFont.getSize();
      curFontSize += 4;
      panelTitleLabel.setFont(new Font(curFont.getFontName(), curFont.getStyle(), curFontSize));
      mainPanel.add(panelTitleLabel);

      // name
      namePanel = new NamePanel(model);
      mainPanel.add(namePanel);

      // jndi properties
      JPanel jndiPanel = new JPanel();
      jndiPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));
      jndiPanel.setBorder(BorderFactory.createTitledBorder(
	JMeterUtils.getResString("jndi_lookup_name")));

      jndiPanel.add(getLookupNamePanel());

      mainPanel.add(jndiPanel);
      this.add(mainPanel);
    }
    else
    {
      this.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));

      // url and driver class
      JPanel jndiPanel = new JPanel();
      jndiPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));
      jndiPanel.setBorder(BorderFactory.createTitledBorder(
	JMeterUtils.getResString("jndi_lookup_name")));

      jndiPanel.add(getLookupNamePanel());

      this.add(jndiPanel);
    }
  }

  protected JPanel getLookupNamePanel()
  {
    catClass.info("Start : getLookupNamePanel1");
    JPanel panel = new JPanel();
    panel.add(new JLabel(JMeterUtils.getResString("jndi_lookup_name")));
    lookupField.setText(model.getLookupName());
    lookupField.setName(JMeterUtils.getResString("jndi_lookup_name"));
    lookupField.addKeyListener(this);
    panel.add(lookupField);
    catClass.info("End : getLookupNamePanel1");
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
    if(name.equals(JMeterUtils.getResString("jndi_lookup_name")))
    {
      model.setLookupName(lookupField.getText());
    }
  }
}
