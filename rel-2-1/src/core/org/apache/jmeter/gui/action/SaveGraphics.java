/*
 * Copyright 2001-2006 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *  
 */

package org.apache.jmeter.gui.action;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JFileChooser;

import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.save.SaveGraphicsService;
import org.apache.jmeter.visualizers.Printable;

/**
 * SaveGraphics action is meant to be a generic reusable Action. The class will
 * use GUIPackage to get the current gui. Once it does, it checks to see if the
 * element implements Printable interface. If it does, it call getPrintable() to
 * get the JComponent. By default, it will use SaveGraphicsService to save a PNG
 * file if no extension is provided. If either .png or .tif is in the filename,
 * it will call SaveGraphicsService to save in the format.
 */
public class SaveGraphics implements Command {

	private static Set commands = new HashSet();
	static {
		commands.add(ActionNames.SAVE_GRAPHICS);
        commands.add(ActionNames.SAVE_GRAPHICS_ALL);
	}

	private static final String[] extensions 
        = { SaveGraphicsService.TIFF_EXTENSION, SaveGraphicsService.PNG_EXTENSION };

	/**
	 * Constructor for the Save object.
	 */
	public SaveGraphics() {
	}

	/**
	 * Gets the ActionNames attribute of the Save object.
	 * 
	 * @return the ActionNames value
	 */
	public Set getActionNames() {
		return commands;
	}

	public void doAction(ActionEvent e) throws IllegalUserActionException {
		JMeterGUIComponent component = null;
		JComponent comp = null;
		if (!commands.contains(e.getActionCommand())) {
			throw new IllegalUserActionException("Invalid user command:" + e.getActionCommand());
		}
		if (e.getActionCommand().equals(ActionNames.SAVE_GRAPHICS)) {
			component = GuiPackage.getInstance().getCurrentGui();
			// get the JComponent from the visualizer
			if (component instanceof Printable) {
				comp = ((Printable) component).getPrintableComponent();
                saveImage(comp);
			}
		}
        if (e.getActionCommand().equals(ActionNames.SAVE_GRAPHICS_ALL)) {
            component = GuiPackage.getInstance().getCurrentGui();
            comp=((JComponent) component).getRootPane();
            saveImage(comp);
        }
	}

    private void saveImage(JComponent comp){

        String filename;
        JFileChooser chooser = FileDialoger.promptToSaveFile(GuiPackage.getInstance().getTreeListener()
                .getCurrentNode().getName(), extensions);
        if (chooser == null) {
            return;
        }
        // Get the string given from the choose and check
        // the file extension.
        filename = chooser.getSelectedFile().getAbsolutePath();
        if (filename != null) {
            SaveGraphicsService save = new SaveGraphicsService();
            String ext = filename.substring(filename.length() - 4);
            String name = filename.substring(0, filename.length() - 4);
            if (ext.equals(SaveGraphicsService.PNG_EXTENSION)) {
                save.saveJComponent(name, SaveGraphicsService.PNG, comp);
            } else if (ext.equals(SaveGraphicsService.TIFF_EXTENSION)) {
                save.saveJComponent(name, SaveGraphicsService.TIFF, comp);
            } else {
                save.saveJComponent(filename, SaveGraphicsService.PNG, comp);
            }
        }

    }
}
