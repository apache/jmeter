/*
 * Created on Sep 15, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.apache.jmeter.gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.reflect.Functor;
import org.apache.log.Logger;

/**
 * @author mstover
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class SavePropertyDialog extends JDialog implements ActionListener
{
   protected static transient Logger log = LoggingManager.getLoggerForClass();
   static Map functors = new HashMap();
   
   static final long serialVersionUID = 1;
   SampleSaveConfiguration saveConfig;
   
   /**
    * @param owner
    * @param title
    * @param modal
    * @throws java.awt.HeadlessException
    */
   public SavePropertyDialog(Frame owner, String title, boolean modal,SampleSaveConfiguration s)
         throws HeadlessException
   {
      super(owner, title, modal);
      saveConfig = s;
      log.info("SampleSaveConfiguration = " + saveConfig);
      dialogInit();
   }
   
   private int countMethods(Method[] m)
   {
      int count = 0;
      for (int i = 0; i < m.length; i++)
      {
         if(m[i].getName().startsWith("save")) count++;
      }
      return count;
   }
   /* (non-Javadoc)
    * @see javax.swing.JDialog#dialogInit()
    */
   protected void dialogInit()
   {
      if(saveConfig != null)
      {
	      super.dialogInit();
	      this.getContentPane().setLayout(new BorderLayout());
	      Method[] methods = SampleSaveConfiguration.class.getMethods();
	      int x = (countMethods(methods) / 3) + 1;
	      log.info("grid panel is " + 3 + " by " + x);
	      JPanel checkPanel = new JPanel(new GridLayout(x,3));
	      for (int i = 0; i < methods.length; i++)
	      {
	         String name = methods[i].getName();
	         if(name.startsWith("save"))
	         {
	            try
	            {
	               name = name.substring(4);
	               JCheckBox check = new JCheckBox(JMeterUtils.getResString("save " + name),
	                     ((Boolean)methods[i].invoke(saveConfig,new Object[0])).booleanValue());
	               checkPanel.add(check,BorderLayout.NORTH);
	               check.addActionListener(this);
	               check.setActionCommand("set" + name);
	               if(!functors.containsKey(check.getActionCommand()))
	               {
	                  functors.put(check.getActionCommand(),new Functor(
	                        check.getActionCommand()));
	               }
	            }
	            catch (Exception e)
	            {
	               log.warn("Problem creating save config dialog",e);
	            }
	         }
	      }
	      getContentPane().add(checkPanel,BorderLayout.NORTH);
	      JButton exit = new JButton(JMeterUtils.getResString("done"));
	      this.getContentPane().add(exit,BorderLayout.SOUTH);
	      exit.addActionListener(new ActionListener()
	            {
	         		public void actionPerformed(ActionEvent e)
	         		{
	         		   dispose();
	         		}
	            });
      }
   }
   
   /* (non-Javadoc)
    * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
    */
   public void actionPerformed(ActionEvent e)
   {
      String action = e.getActionCommand();
      Functor f = (Functor)functors.get(action);
      f.invoke(saveConfig,new Object[]{new Boolean(((JCheckBox)e.getSource()).isSelected())});
   }
   /**
    * @return Returns the saveConfig.
    */
   public SampleSaveConfiguration getSaveConfig()
   {
      return saveConfig;
   }
   /**
    * @param saveConfig The saveConfig to set.
    */
   public void setSaveConfig(SampleSaveConfiguration saveConfig)
   {
      this.saveConfig = saveConfig;
   }
}
