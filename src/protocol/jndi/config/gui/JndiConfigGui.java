/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
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
 * @version	1.0
 * @created	2001 Dec 17
 * @modified	2001 Dec 17
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

      // title
      JLabel panelTitleLabel = new JLabel(
	JMeterUtils.getResString("jndi_config_title"));
      Font curFont = panelTitleLabel.getFont();
      int curFontSize = curFont.getSize();
      curFontSize += 4;
      panelTitleLabel.setFont(new Font(curFont.getFontName(), curFont.getStyle(), curFontSize));
      mainPanel.add(panelTitleLabel);

      // name
      namePanel = new NamePanel(model);
      mainPanel.add(namePanel);

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
