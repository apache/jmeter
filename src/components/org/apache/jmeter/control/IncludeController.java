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

package org.apache.jmeter.control;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * 
 * 
 * author Peter Lin
 * @version $Revision$
 */
public class IncludeController extends GenericController implements ReplaceableController {
	private static final Logger log = LoggingManager.getLoggerForClass();

    private static final String INCLUDE_PATH = "IncludeController.includepath"; //$NON-NLS-1$

    private static  final String prefix =
        JMeterUtils.getPropDefault(
                "includecontroller.prefix", //$NON-NLS-1$ 
                ""); //$NON-NLS-1$
    
    private HashTree SUBTREE = null;
    private TestElement SUB = null;

	/**
	 * No-arg constructor
	 * 
	 * @see java.lang.Object#Object()
	 */
	public IncludeController() {
		super();
	}

	public Object clone() {
        // TODO - fix so that this is only called once per test, instead of at every clone
        // Perhaps save previous filename, and only load if it has changed?
        this.resolveReplacementSubTree(null);
		IncludeController clone = (IncludeController) super.clone();
        clone.setIncludePath(this.getIncludePath());
        if (this.SUBTREE != null) {
            if (this.SUBTREE.keySet().size() == 1) {
                Iterator itr = this.SUBTREE.keySet().iterator();
                while (itr.hasNext()) {
                    this.SUB = (TestElement)itr.next();
                }
            }
            clone.SUBTREE = (HashTree)this.SUBTREE.clone();
            clone.SUB = (TestElement)this.SUB.clone();
        }
		return clone;
	}

    /**
     * In the event an user wants to include an external JMX test plan
     * the GUI would call this.
     * @param jmxfile
     */
    public void setIncludePath(String jmxfile) {
        this.setProperty(INCLUDE_PATH,jmxfile);
    }
    
    /**
     * return the JMX file path.
     * @return
     */
    public String getIncludePath() {
        return this.getPropertyAsString(INCLUDE_PATH);
    }
    
    /**
     * The way ReplaceableController works is clone is called first,
     * followed by replace(HashTree) and finally getReplacement().
     */
    public HashTree getReplacementSubTree() {
        return SUBTREE;
    }

    public void resolveReplacementSubTree(Object context) {
        this.SUBTREE = this.loadIncludedElements();
    }

    /**
     * load the included elements using SaveService
     * @param node
     */
    protected HashTree loadIncludedElements() {
        // only try to load the JMX test plan if there is one
        final String includePath = getIncludePath();
        InputStream reader = null;
        HashTree tree = null;
        if (includePath != null && includePath.length() > 0) {
            try {
            	String file=prefix+includePath;
                log.info("loadIncludedElements -- try to load included module: "+file);
                reader = new FileInputStream(file);
                tree = SaveService.loadTree(reader);
                removeDisabledItems(tree);
                return tree;
            } catch (NoClassDefFoundError ex) // Allow for missing optional jars
            {
                String msg = ex.getMessage();
                if (msg == null) {
                    msg = "Missing jar file - see log for details";
                }
                log.warn("Missing jar file", ex);
                JMeterUtils.reportErrorToUser(msg);
            } catch (FileNotFoundException ex) {
                String msg = ex.getMessage();
                JMeterUtils.reportErrorToUser(msg);
                log.warn(msg);
            } catch (Exception ex) {
                String msg = ex.getMessage();
                if (msg == null) {
                    msg = "Unexpected error - see log for details";
                }
                JMeterUtils.reportErrorToUser(msg);
                log.warn("Unexpected error", ex);
            }
            finally{
                JOrphanUtils.closeQuietly(reader);
            }
        }
        return tree;
    }

    private void removeDisabledItems(HashTree tree) {
        Iterator iter = new LinkedList(tree.list()).iterator();
        while (iter.hasNext()) {
            TestElement item = (TestElement) iter.next();
            if (!item.isEnabled()) {
                //log.info("Removing "+item.toString());
                tree.remove(item);
            } else {
                //log.info("Keeping "+item.toString());
                removeDisabledItems(tree.getTree(item));// Recursive call
            }
        }
    }
    
}
