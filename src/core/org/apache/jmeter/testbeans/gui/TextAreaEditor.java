/*
 * Created on May 21, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.apache.jmeter.testbeans.gui;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyEditorSupport;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * @author mstover
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TextAreaEditor extends PropertyEditorSupport implements
      FocusListener
{
   JTextArea textUI;
   JScrollPane scroller;

   /* (non-Javadoc)
    * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
    */
   public void focusGained(FocusEvent e)
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
    */
   public void focusLost(FocusEvent e)
   {
      firePropertyChange();

   }

   public static void main(String[] args)
   {
   }
   
   protected void init()
   {
      textUI= new JTextArea();
      textUI.addFocusListener(this);
      textUI.setWrapStyleWord(true);
      textUI.setLineWrap(true);
      scroller = new JScrollPane(textUI,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
   }
   
   /**
    * 
    */
   public TextAreaEditor()
   {
      super();
      init();

		
   }
   /**
    * @param source
    */
   public TextAreaEditor(Object source)
   {
      super(source);
      init();
      setValue(source);
   }
   
   /* (non-Javadoc)
    * @see java.beans.PropertyEditor#getAsText()
    */
   public String getAsText()
   {
      return textUI.getText();
   }
   /* (non-Javadoc)
    * @see java.beans.PropertyEditor#getCustomEditor()
    */
   public Component getCustomEditor()
   {
      return scroller;
   }
   /* (non-Javadoc)
    * @see java.beans.PropertyEditor#setAsText(java.lang.String)
    */
   public void setAsText(String text) throws IllegalArgumentException
   {
      textUI.setText(text);
   }
   /* (non-Javadoc)
    * @see java.beans.PropertyEditor#setValue(java.lang.Object)
    */
   public void setValue(Object value)
   {
      if(value != null)
      {
         textUI.setText(value.toString());
      }
      else
      {
         textUI.setText("");
      }
   }
   /* (non-Javadoc)
    * @see java.beans.PropertyEditor#getValue()
    */
   public Object getValue()
   {
      return textUI.getText();
   }
   /* (non-Javadoc)
    * @see java.beans.PropertyEditor#supportsCustomEditor()
    */
   public boolean supportsCustomEditor()
   {
      // TODO Auto-generated method stub
      return true;
   }
}
