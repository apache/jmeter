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

package org.apache.jmeter.save.converters;

import java.util.Iterator;

import org.apache.jorphan.collections.HashTree;

import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.AbstractCollectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * @author mstover
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class HashTreeConverter extends AbstractCollectionConverter {

	/**
	 * Returns the converter version; used to check for possible
	 * incompatibilities
	 */
	public static String getVersion() {
		return "$Revision$";  //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thoughtworks.xstream.converters.Converter#canConvert(java.lang.Class)
	 */
	public boolean canConvert(Class arg0) {
		return HashTree.class.isAssignableFrom(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thoughtworks.xstream.converters.Converter#marshal(java.lang.Object,
	 *      com.thoughtworks.xstream.io.HierarchicalStreamWriter,
	 *      com.thoughtworks.xstream.converters.MarshallingContext)
	 */
	public void marshal(Object arg0, HierarchicalStreamWriter writer, MarshallingContext context) {
		HashTree tree = (HashTree) arg0;
		Iterator iter = tree.list().iterator();
		while (iter.hasNext()) {
			Object item = iter.next();
			writeItem(item, context, writer);
			writeItem(tree.getTree(item), context, writer);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thoughtworks.xstream.converters.Converter#unmarshal(com.thoughtworks.xstream.io.HierarchicalStreamReader,
	 *      com.thoughtworks.xstream.converters.UnmarshallingContext)
	 */
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		boolean isKey = true;
		Object current = null;
		HashTree tree = (HashTree) createCollection(context.getRequiredType());
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			Object item = readItem(reader, context, tree);
			if (isKey) {
				tree.add(item);
				current = item;
				isKey = false;
			} else {
				tree.set(current, (HashTree) item);
				isKey = true;
			}
			reader.moveUp();
		}
		return tree;
	}

	/**
	 * @param arg0
	 */
	public HashTreeConverter(Mapper arg0) {
		super(arg0);
	}
}
