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

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;
import javax.swing.event.*;

import org.apache.jmeter.ejb.jndi.config.MethodConfigUserObject;
import org.apache.jmeter.ejb.jndi.config.MethodConfigUserObjectException;
import org.apache.log4j.Category;

/**
 * Dialog to allow user to key in value for their method parameters
 *
 * @author	  Khor Soon Hin
 * @version  $Revision$ Last Updated: $Date$
 * Created	  2002 Jan 08
 */
public class MethodConfigDialog extends JDialog
{
  private static Category catClass = Category.getInstance(
	MethodConfigDialog.class.getName());

  protected JOptionPane optionPane;
  protected JTextField textField;
  protected JLabel classLabel;
  protected MethodConfigUserObject userObject;

  public MethodConfigDialog(Frame aFrame, final Class type)
  {
    super(aFrame, true);
    classLabel = new JLabel(type.getName());
    textField = new JTextField(10);
    Object[] array = {classLabel, textField};

    final String btnString1 = "Ok";
    final String btnString2 = "Cancel";
    Object[] options = {btnString1, btnString2};

    optionPane = new JOptionPane(
			array,
			JOptionPane.QUESTION_MESSAGE,
			JOptionPane.OK_CANCEL_OPTION,
			null,
			options,
			options[0]);
    setContentPane(optionPane);
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter()
	{
	}
	);
    textField.addActionListener(new ActionListener()
	{
          public void actionPerformed(ActionEvent e)
	  {
	    optionPane.setValue(btnString1);
	  }
	}
	);
    optionPane.addPropertyChangeListener(new PropertyChangeListener()
	{
	  public void propertyChange(PropertyChangeEvent e)
	  {
	    catClass.debug("Start : propertyChange1");
	    String prop = e.getPropertyName();
	    if(catClass.isDebugEnabled())
	    {
	      catClass.debug("propertyChange1 : property name - " + prop);
	    }
catClass.debug("JOptionPane.INPUT_VALUE_PROPERTY - " + JOptionPane.INPUT_VALUE_PROPERTY);
catClass.debug("optionPane.getValue() - " + optionPane.getValue());
catClass.debug("optionPane.getInputValue() - " + optionPane.getInputValue());
	    Object value = null;
	    if(isVisible()
		&& (e.getSource() == optionPane)
		&& prop.equals(JOptionPane.VALUE_PROPERTY))
            {
              value = optionPane.getValue();
	      if(catClass.isDebugEnabled())
	      {
	        catClass.debug("propertyChange1 : optionPane value - " + value);
	      }

              // reset the JOptionPane's value
	      // If you don't don this then the next time the user
	      // presses ths same button no event will be fired
              optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

	      String input = null;
              if(value.equals(btnString1))
	      {
	        try
	        {
	          input = textField.getText();
	          if(catClass.isDebugEnabled())
	          {
	            catClass.debug("MethodConfigDialog1 : input - " + input);
	          }
	          userObject = new MethodConfigUserObject(type, input);
	          setVisible(false);
	        }
	        catch(MethodConfigUserObjectException ex)
	        {
	          // the input is not compatible with the class
		  catClass.debug(
			"propertyChange1 : input incompatible with class");
	          textField.selectAll();
	          JOptionPane.showMessageDialog(
  			MethodConfigDialog.this,
  			"Sorry, \"" + input + "\" "
  			+ "is not valid for Class "
  			+ type,
  			"Try again",
  			JOptionPane.ERROR_MESSAGE);
  		  input = null;
  		  userObject = null;
	        }
  	      }
              else
              {
                setVisible(false);
              }
	    }
	    catClass.debug("End - propertyChange1");
	  }
	}
	);
  }

  public MethodConfigUserObject getValidatedInput()
  {
    return userObject;
  }
}
