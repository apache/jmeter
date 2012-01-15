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

package org.apache.jmeter.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.TransferHandler;

import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.action.LoadDraggedFile;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Transfer Handler implementation that provides DnD support of Test Plans
 */
@SuppressWarnings("serial")
final class TopLevelTransferHandler extends TransferHandler {
    private static final Logger log = LoggingManager.getLoggerForClass();
    
	/**
	 * 
	 */
	@Override
	public boolean canImport(TransferHandler.TransferSupport support) {
	    if(!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
	        return false;
	    }
	    return true;
	}
	
	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean importData(TransferSupport support) {
	    if(!canImport(support)) {
	        return false;
	    }
	    try {
	        List<File> files = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
	        if(files.isEmpty()) {
	            return false;
	        }
	        
	        File file = files.get(0);
	        if(!file.getName().endsWith(".jmx")) {
	            log.warn("Importing file:" + file.getName()+ "from DnD failed because file extension does not end with .jmx");
	            return false;
	        }
	        
	        ActionEvent fakeEvent = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ActionNames.OPEN);
	        LoadDraggedFile.loadProject(fakeEvent, file);
	    } catch (IOException e) {
	        log.error("Importing data from DnD caused ", e);
	        return false;
	    } catch (UnsupportedFlavorException e) {
	        log.error("Importing data from DnD caused ", e);
	        return false;
	    }
	    return super.importData(support);
	}
}