/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.jmeter.util;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.jmeter.gui.tree.JMeterTreeNode;

/**
 * Simple implementation of a transferable for {@link JMeterTreeNode} arrays based on serialization.
 * @since 2.9
 */
public class JMeterTreeNodeTransferable implements Transferable {
    
    public final static DataFlavor JMETER_TREE_NODE_ARRAY_DATA_FLAVOR = new DataFlavor(JMeterTreeNode[].class, JMeterTreeNode[].class.getName());
    
    private final static DataFlavor[] DATA_FLAVORS = new DataFlavor[]{JMETER_TREE_NODE_ARRAY_DATA_FLAVOR};
    
    private byte[] data = null;

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return DATA_FLAVORS;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.match(JMETER_TREE_NODE_ARRAY_DATA_FLAVOR);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if(!isDataFlavorSupported(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }
        if(data != null) {
            ObjectInput ois = null;
            try {
                ois = new ObjectInputStream(new ByteArrayInputStream(data));
                JMeterTreeNode[] nodes = (JMeterTreeNode[]) ois.readObject();
                return nodes;
            } catch (ClassNotFoundException cnfe) {
                throw new IOException("Failed to read object stream.", cnfe);
            } finally {
                if(ois != null) {
                    try {
                        ois.close();
                    } catch (Exception e) {
                        // NOOP
                    }
                }
            }
        }
        return null;
    }
    
    public void setTransferData(JMeterTreeNode[] nodes) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(bos);
            oos.writeObject(nodes);
            data = bos.toByteArray();
        } finally {
            if(oos != null) {
                try {
                    oos.close();
                } catch (Exception e) {
                    // NOOP
                }
            }
        }
    }
}
