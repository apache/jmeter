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

package org.apache.jmeter.gui.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.tree.TreePath;

import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import com.thoughtworks.xstream.XStream;

/**
 * @author    Michael Stover
 * @version   $Revision$
 */
public class Load implements Command
{
    transient private static Logger log = LoggingManager.getLoggerForClass();
    XStream loadService = new XStream();
 
    private static Set commands = new HashSet();
    static {
        commands.add("open");
		commands.add("merge");
    }

    public Load()
    {
		super();
    }

    public Set getActionNames()
    {
        return commands;
    }

    public void doAction(ActionEvent e)
    {
    	boolean merging = e.getActionCommand().equals("merge");
    	
		if (!merging)
		{
			ActionRouter.getInstance().doActionNow(
		        new ActionEvent(e.getSource(),e.getID(),"close"));
		}

        JFileChooser chooser =
            FileDialoger.promptToOpenFile(new String[] { ".jmx" });
        if (chooser == null)
        {
            return;
        }
        boolean isTestPlan = false;
        InputStream reader = null;
        File f = null;
        try
        {
            f = chooser.getSelectedFile();
            if (f != null)
            {
            	if (merging){
					log.info("Merging file: " + f);
            	} else {
					log.info("Loading file: " + f);
					FileServer.getFileServer().setBasedir(f.getAbsolutePath());
            	}
                reader = new FileInputStream(f);
                HashTree tree = SaveService.loadTree(reader);
                isTestPlan = insertLoadedTree(e.getID(), tree);
            }
        }
        catch (Exception ex)
        {
            String msg = ex.getMessage();
            if (msg == null)
            {
                msg = "Unexpected error - see log for details";
                log.warn("Unexpected error", ex);
            }
            JMeterUtils.reportErrorToUser(msg);
        }
        finally
        {
			try {
				reader.close();
			} catch (IOException e1) {
				// ignored
			}
            GuiPackage.getInstance().updateCurrentGui();
            GuiPackage.getInstance().getMainFrame().repaint();
        }
        // don't change name if merging
        if (!merging && isTestPlan && f != null)
        {
            ((Save) ActionRouter.getInstance().getAction(
                    "save",
                    "org.apache.jmeter.gui.action.Save")).setTestPlanFile(
                f.getAbsolutePath());
        }
    }

    /**
     * Returns a boolean indicating whether the loaded tree was a full test plan
     */
    public boolean insertLoadedTree(int id, HashTree tree)
        throws Exception, IllegalUserActionException
    {
        //convertTree(tree);
        if (tree == null)
        {
            throw new Exception("Error in TestPlan - see log file");
        }
        boolean isTestPlan = tree.getArray()[0] instanceof TestPlan;
        HashTree newTree = GuiPackage.getInstance().addSubTree(tree);
        GuiPackage.getInstance().updateCurrentGui();
        GuiPackage.getInstance().getMainFrame().getTree().setSelectionPath(
            new TreePath(((JMeterTreeNode) newTree.getArray()[0]).getPath()));
        tree = GuiPackage.getInstance().getCurrentSubTree();
        ActionRouter.getInstance().actionPerformed(
            new ActionEvent(
                tree.get(tree.getArray()[tree.size() - 1]),
                id,
                CheckDirty.SUB_TREE_LOADED));

        return isTestPlan;
    }
}
