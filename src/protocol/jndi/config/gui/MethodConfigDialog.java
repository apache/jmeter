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
 * @author	Khor Soon Hin
 * @version	1.0
 * @created	2002 Jan 08
 * @modified	2002 Jan 08
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
