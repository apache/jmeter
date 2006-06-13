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

package org.apache.jmeter.ejb.jndi.control.gui;


import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.jmeter.ejb.jndi.config.JndiConfig;
import org.apache.jmeter.ejb.jndi.config.gui.JndiConfigGui;
import org.apache.jmeter.ejb.jndi.control.JndiTestSample;
import org.apache.jmeter.gui.ModelSupported;
import org.apache.jmeter.gui.NamePanel;
import org.apache.jmeter.gui.VerticalLayout;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Provides the gui to configure JNDI sampling
 *
 * @author	Khor Soon Hin
 * Created	20 Dec 2001
 * @version $Revision$ Last Updated: $Date$
 */
public class JndiTestSampleGui extends JPanel implements ModelSupported
{
  JndiTestSample model;
  NamePanel namePanel;

  JndiConfigGui jndiConfigGui;

  public JndiTestSampleGui()
  {
  }

  public void updateGui()
  {
    namePanel.updateGui();
    jndiConfigGui.updateGui();
  }

  public void setModel(Object model)
  {
    this.model = (JndiTestSample)model;
    init();
  }

  protected void init()
  {
    this.setLayout(new VerticalLayout(5, VerticalLayout.LEFT, 
	VerticalLayout.TOP));
    
    // main panel
    JPanel mainPanel = new JPanel();
    Border margin = new EmptyBorder(10, 10, 5, 10);
    mainPanel.setBorder(margin);
    mainPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));

 
    mainPanel.add(makeTitlePanel());

    // jndi
    jndiConfigGui = new JndiConfigGui(false);
    jndiConfigGui.setModel(model.getDefaultJndiConfig());
    mainPanel.add(jndiConfigGui);

    this.add(mainPanel);
  }
}
