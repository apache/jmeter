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
package org.apache.jmeter.gui.util;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.UIManager;

import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.action.ChangeLanguage;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.LocaleChangeEvent;
import org.apache.jmeter.util.LocaleChangeListener;
import org.apache.jmeter.util.SSLManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/****************************************
 * Title: Apache JMeter Description: Copyright: Copyright (c) 2000 Company:
 * Apache Foundation
 *
 *@author    Michael Stover
 *@created   $Date$
 *@version   1.0
 ***************************************/
public class JMeterMenuBar extends JMenuBar implements LocaleChangeListener
{
	transient private static Logger log =
		Hierarchy.getDefaultHierarchy().getLoggerFor("jmeter.gui");
	JMenu fileMenu;
	JMenuItem file_save_all;
	JMenuItem file_load;
	JMenuItem file_exit;
	JMenuItem file_new;
	JMenu editMenu;
	JMenu edit_add;
	JMenu edit_add_submenu;
	JMenuItem edit_remove;
	JMenu runMenu;
	JMenuItem run_start;
	JMenu remote_start;
	JMenuItem remote_start_all;
	Collection remote_engine_start;
	JMenuItem run_stop;
	JMenu remote_stop;
	JMenuItem remote_stop_all;
	Collection remote_engine_stop;
	JMenuItem run_clear;
	JMenuItem run_clearAll;
	JMenu reportMenu;
	JMenuItem analyze;
	JMenu optionsMenu;
	JMenu lafMenu;
	JMenuItem sslManager;
	JMenu helpMenu;
	JMenuItem help_about;
	String[] remoteHosts;
	/****************************************
	 * !ToDo (Constructor description)
	 ***************************************/
	public JMeterMenuBar()
	{
		remote_engine_start = new LinkedList();
		remote_engine_stop = new LinkedList();
		remoteHosts =
			JOrphanUtils.split(JMeterUtils.getPropDefault("remote_hosts", ""), ",");
		if (remoteHosts.length == 1 && remoteHosts[0].equals(""))
		{
			remoteHosts = new String[0];
		}
		this.getRemoteItems();
		createMenuBar();
	}
	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param enabled  !ToDo (Parameter description)
	 ***************************************/
	public void setFileSaveEnabled(boolean enabled)
	{
		file_save_all.setEnabled(enabled);
	}
	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param enabled  !ToDo (Parameter description)
	 ***************************************/
	public void setFileLoadEnabled(boolean enabled)
	{
		if (file_load != null)
		{
			file_load.setEnabled(enabled);
		}
	}
	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param enabled  !ToDo (Parameter description)
	 ***************************************/
	public void setEditEnabled(boolean enabled)
	{
		if (editMenu != null)
		{
			editMenu.setEnabled(enabled);
		}
	}
	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param menu  !ToDo (Parameter description)
	 ***************************************/
	public void setEditAddMenu(JMenu menu)
	{
		// If the Add menu already exists, remove it.
		if (edit_add != null)
		{
			editMenu.remove(edit_add);
		}
		// Insert the Add menu as the first menu item in the Edit menu.
		edit_add = menu;
		editMenu.insert(edit_add, 0);
	}
	public void setEditMenu(JPopupMenu menu)
	{
		if (menu != null)
		{
			editMenu.removeAll();
			Component[] comps = menu.getComponents();
			for (int i = 0; i < comps.length; i++)
			{
				editMenu.add(comps[i]);
			}
			editMenu.setEnabled(true);
		}
		else
		{
			editMenu.setEnabled(false);
		}
	}
	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param enabled  !ToDo (Parameter description)
	 ***************************************/
	public void setEditAddEnabled(boolean enabled)
	{
		// There was a NPE being thrown without the null check here.. JKB
		if (edit_add != null)
		{
			edit_add.setEnabled(enabled);
		}
		// If we are enabling the Edit-->Add menu item, then we also need to
		// enable the Edit menu. The Edit menu may already be enabled, but
		// there's no harm it trying to enable it again.
		if (enabled)
		{
			setEditEnabled(true);
		}
		else
		{
			// If we are disabling the Edit-->Add menu item and the Edit-->Remove
			// menu item is disabled, then we also need to disable the Edit menu.
			// The Java Look and Feel Guidelines say to disable a menu if all
			// menu items are disabled.
			if (!edit_remove.isEnabled())
			{
				editMenu.setEnabled(false);
			}
		}
	}
	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param enabled  !ToDo (Parameter description)
	 ***************************************/
	public void setEditRemoveEnabled(boolean enabled)
	{
		edit_remove.setEnabled(enabled);
		// If we are enabling the Edit-->Remove menu item, then we also need to
		// enable the Edit menu. The Edit menu may already be enabled, but
		// there's no harm it trying to enable it again.
		if (enabled)
		{
			setEditEnabled(true);
		}
		else
		{
			// If we are disabling the Edit-->Remove menu item and the Edit-->Add
			// menu item is disabled, then we also need to disable the Edit menu.
			// The Java Look and Feel Guidelines say to disable a menu if all
			// menu items are disabled.
			if (!edit_add.isEnabled())
			{
				editMenu.setEnabled(false);
			}
		}
	}
	/****************************************
	 * Creates the MenuBar for this application. I believe in my heart that this
	 * should be defined in a file somewhere, but that is for later.
	 ***************************************/
	public void createMenuBar()
	{
		createFileMenu();
		makeEditMenu();
		makeRunMenu();
		makeOptionsMenu();
		makeHelpMenu();
		this.add(fileMenu);
		this.add(editMenu);
		this.add(runMenu);
		this.add(optionsMenu);
		this.add(helpMenu);
	}
	private void makeHelpMenu()
	{
		// HELP MENU
		helpMenu = new JMenu(JMeterUtils.getResString("help"));
		helpMenu.setMnemonic('H');
		JMenuItem contextHelp = new JMenuItem(JMeterUtils.getResString("help"),'H');
		contextHelp.setActionCommand("help");
		contextHelp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H,KeyEvent.CTRL_MASK));
		contextHelp.addActionListener(ActionRouter.getInstance());
		help_about = new JMenuItem(JMeterUtils.getResString("about"), 'A');
		help_about.setActionCommand("about");
		help_about.addActionListener(ActionRouter.getInstance());
		helpMenu.add(contextHelp);
		helpMenu.add(help_about);
	}
	private void makeOptionsMenu()
	{
		// OPTIONS MENU
		optionsMenu = new JMenu(JMeterUtils.getResString("option"));
		JMenuItem functionHelper =
			new JMenuItem(JMeterUtils.getResString("function_dialog_menu_item"), 'F');
		functionHelper.addActionListener(ActionRouter.getInstance());
		functionHelper.setActionCommand("functions");
		functionHelper.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_MASK));
		lafMenu = new JMenu(JMeterUtils.getResString("appearance"));
		UIManager.LookAndFeelInfo lafs[] = UIManager.getInstalledLookAndFeels();
		for (int i = 0; i < lafs.length; ++i)
		{
			JMenuItem laf = new JMenuItem(lafs[i].getName());
			laf.addActionListener(ActionRouter.getInstance());
			laf.setActionCommand("laf:" + lafs[i].getClassName());
			lafMenu.setMnemonic('L');
			lafMenu.add(laf);
		}
		optionsMenu.setMnemonic('O');
		optionsMenu.add(functionHelper);
		optionsMenu.add(lafMenu);
		if (SSLManager.isSSLSupported())
		{
			sslManager = new JMenuItem(JMeterUtils.getResString("sslManager"));
			sslManager.addActionListener(ActionRouter.getInstance());
			sslManager.setActionCommand("sslManager");
			sslManager.setMnemonic('S');
			sslManager.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_MASK));
			optionsMenu.add(sslManager);
		}
		optionsMenu.add(makeLanguageMenu());
	}
	private JMenu makeLanguageMenu()
	{
		JMenu languageMenu = new JMenu(JMeterUtils.getResString("choose_language"));
		languageMenu.setMnemonic('C');
		//add english
		JMenuItem english = new JMenuItem(JMeterUtils.getResString("en"),'E');
		english.addActionListener(ActionRouter.getInstance());
		english.setActionCommand(ChangeLanguage.CHANGE_LANGUAGE);
		english.setName("en");
		languageMenu.add(english);
		//add Japanese
		JMenuItem japanese = new JMenuItem(JMeterUtils.getResString("jp"),'J');
		japanese.addActionListener(ActionRouter.getInstance());
		japanese.setActionCommand(ChangeLanguage.CHANGE_LANGUAGE);
		japanese.setName("ja");
		languageMenu.add(japanese);
		//add Norwegian
		JMenuItem norway = new JMenuItem(JMeterUtils.getResString("no"),'N');
		norway.addActionListener(ActionRouter.getInstance());
		norway.setActionCommand(ChangeLanguage.CHANGE_LANGUAGE);
		norway.setName("no");
		languageMenu.add(norway);
		//add German
		JMenuItem german = new JMenuItem(JMeterUtils.getResString("de"),'G');
		german.addActionListener(ActionRouter.getInstance());
		german.setActionCommand(ChangeLanguage.CHANGE_LANGUAGE);
		german.setName("de");
		languageMenu.add(german);
		return languageMenu;
	}
	
	private void makeRunMenu()
	{
		// RUN MENU
		runMenu = new JMenu(JMeterUtils.getResString("run"));
		runMenu.setMnemonic('R');
		run_start = new JMenuItem(JMeterUtils.getResString("start"), 'S');
		run_start.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK));
		run_start.addActionListener(ActionRouter.getInstance());
		run_start.setActionCommand("start");
		run_stop = new JMenuItem(JMeterUtils.getResString("stop"), 'T');
		run_stop.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, KeyEvent.CTRL_MASK));
		run_stop.setEnabled(false);
		run_stop.addActionListener(ActionRouter.getInstance());
		run_stop.setActionCommand("stop");
		run_clear = new JMenuItem(JMeterUtils.getResString("clear"), 'C');
		run_clear.addActionListener(ActionRouter.getInstance());
		run_clear.setActionCommand(org.apache.jmeter.gui.action.Clear.CLEAR);
		run_clearAll = new JMenuItem(JMeterUtils.getResString("clear_all"), 'a');
		run_clearAll.addActionListener(ActionRouter.getInstance());
		run_clearAll.setActionCommand(org.apache.jmeter.gui.action.Clear.CLEAR_ALL);
		runMenu.add(run_start);
		if (remote_start != null)
		{
			runMenu.add(remote_start);
		}
		remote_start_all = new JMenuItem(JMeterUtils.getResString("remote_start_all"), 'Z');
		remote_start_all.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_MASK));
		remote_start_all.addActionListener(ActionRouter.getInstance());
		remote_start_all.setActionCommand("remote_start_all");
		runMenu.add(remote_start_all);
		runMenu.add(run_stop);
		if (remote_stop != null)
		{
			runMenu.add(remote_stop);
		}
		remote_stop_all = new JMenuItem(JMeterUtils.getResString("remote_stop_all"), 'X');
		remote_stop_all.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_MASK));
		remote_stop_all.addActionListener(ActionRouter.getInstance());
		remote_stop_all.setActionCommand("remote_stop_all");
		runMenu.add(remote_stop_all);
		runMenu.addSeparator();
		runMenu.add(run_clear);
		runMenu.add(run_clearAll);
	}
	private void makeEditMenu()
	{
		// EDIT MENU
		editMenu = new JMenu(JMeterUtils.getResString("edit"));
		// From the Java Look and Feel Guidelines: If all items in a menu
		// are disabled, then disable the menu.  Makes sense.
		editMenu.setEnabled(false);
	}
	private void createFileMenu()
	{
		// FILE MENU
		fileMenu = new JMenu(JMeterUtils.getResString("file"));
		fileMenu.setMnemonic('F');
		JMenuItem file_save = new JMenuItem(JMeterUtils.getResString("save_all"), 'S');
		file_save.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
		file_save.setActionCommand("save");
		file_save.addActionListener(ActionRouter.getInstance());
		file_save.setEnabled(true);
		
		file_save_all = new JMenuItem(JMeterUtils.getResString("save_all_as"), 'A');
		file_save_all.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_MASK));
		file_save_all.setActionCommand("save_all");
		file_save_all.addActionListener(ActionRouter.getInstance());
		file_save_all.setEnabled(true);
		file_load = new JMenuItem(JMeterUtils.getResString("open"), 'O');
		file_load.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
		file_load.addActionListener(ActionRouter.getInstance());
		// Set default SAVE menu item to disabled since the default node that is selected
		// is ROOT, which does not allow items to be inserted.
		file_load.setEnabled(false);
		file_load.setActionCommand("open");
		file_new = new JMenuItem(JMeterUtils.getResString("new"), 'N');
		file_new.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK));
		file_new.setActionCommand("new");
		file_new.addActionListener(ActionRouter.getInstance());
		file_exit = new JMenuItem(JMeterUtils.getResString("exit"), 'X');
		file_exit.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK));
		file_exit.setActionCommand("exit");
		file_exit.addActionListener(ActionRouter.getInstance());
		fileMenu.add(file_new);
		fileMenu.add(file_load);
		fileMenu.add(file_save);
		fileMenu.add(file_save_all);
		fileMenu.addSeparator();
		fileMenu.add(file_exit);
	}
	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param running  !ToDo (Parameter description)
	 *@param host     !ToDo (Parameter description)
	 ***************************************/
	public void setRunning(boolean running, String host)
	{
		Iterator iter = remote_engine_start.iterator();
		Iterator iter2 = remote_engine_stop.iterator();
		while (iter.hasNext() && iter2.hasNext())
		{
			JMenuItem start = (JMenuItem) iter.next();
			JMenuItem stop = (JMenuItem) iter2.next();
			log.info("host = " + host + " start = " + start.getText());
			if (start.getText().equals(host))
			{
				start.setEnabled(!running);
			}
			if (stop.getText().equals(host))
			{
				stop.setEnabled(running);
			}
		}
	}
	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param enable  !ToDo (Parameter description)
	 ***************************************/
	public void setEnabled(boolean enable)
	{
		run_start.setEnabled(!enable);
		run_stop.setEnabled(enable);
	}
	private void getRemoteItems()
	{
		if (remoteHosts.length > 0)
		{
			remote_start = new JMenu(JMeterUtils.getResString("remote_start"));
			remote_stop = new JMenu(JMeterUtils.getResString("remote_stop"));
			
			for (int i = 0; i < remoteHosts.length; i++)
			{
                remoteHosts[i] = remoteHosts[i].trim();
				JMenuItem item = new JMenuItem(remoteHosts[i]);
				item.setActionCommand("remote_start");
				item.setName(remoteHosts[i]);
				item.addActionListener(ActionRouter.getInstance());
				remote_engine_start.add(item);
				remote_start.add(item);
				item = new JMenuItem(remoteHosts[i]);
				item.setActionCommand("remote_stop");
				item.setName(remoteHosts[i]);
				item.addActionListener(ActionRouter.getInstance());
				item.setEnabled(false);
				remote_engine_stop.add(item);
				remote_stop.add(item);
			}
		}
	}

	/**
	 * Processes a locale change notification. Changes the texts in all
	 * menus to the new language.
	 */
        public void localeChanged(LocaleChangeEvent event) {
            updateMenuElement(fileMenu);
            updateMenuElement(editMenu);
            updateMenuElement(runMenu);
            updateMenuElement(optionsMenu);
            updateMenuElement(helpMenu);
        }
   
   
	/**
	 * Refreshes all texts in the menu and all submenus to a new locale.
	 */
        private void updateMenuElement(MenuElement menu)
	{
            Component component = menu.getComponent();
   
            if (component.getName() != null)
	    {
                if (component instanceof JMenu)
		{
                    ((JMenu)component).setText(JMeterUtils.getResString(component.getName()));
                }
		else
		{
                    ((JMenuItem)component).setText(JMeterUtils.getResString(component.getName()));
                }
            }
   
            MenuElement[] subelements = menu.getSubElements();
   
            for (int i = 0; i < subelements.length; i++)
	    {
                updateMenuElement(subelements[i]);
            }
        }
}
