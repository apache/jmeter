/*
 * Copyright 2005 The Apache Software Foundation.
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

/*
 * Created on Feb 8, 2005
 *
 */
package org.apache.jorphan.collections;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.jorphan.util.JOrphanUtils;

/**
 * @author mike
 * TODO does not appear to be used anywhere (except by the test class)
 */
public class ConfigurationTree implements Serializable, Cloneable {
	private static final long serialVersionUID = 1;

	private static final String VALUE = "!!VALUE_][!!";

	private static final String BLOCK = "[[!";

	private static final String END_BLOCK = "!]]";

	ListedHashTree propTree;

	public ConfigurationTree() {
		propTree = new ListedHashTree();
	}

	public ConfigurationTree(Reader r) throws IOException {
		propTree = fromXML(r).propTree;
	}

	public ConfigurationTree(String value) {
		propTree = new ListedHashTree();
		setValue(value);
	}

	public ConfigurationTree(ListedHashTree data) {
		propTree = data;
	}

	public ConfigurationTree(ListedHashTree data, String value) {
		propTree = data;
		setValue(value);
	}

	/**
	 * @param keys
	 */
	public void add(Collection keys) {
		propTree.add(keys);
	}

	/**
	 * @param treePath
	 * @param values
	 */
	public void add(Collection treePath, Collection values) {
		propTree.add(treePath, values);
	}

	/**
	 * @param treePath
	 * @param value
	 * @return
	 */
	public ConfigurationTree add(Collection treePath, String value) {
		return makeSubtree((ListedHashTree) propTree.add(treePath, value));
	}

	/**
	 * @param treePath
	 * @param values
	 */
	public void add(Collection treePath, String[] values) {
		propTree.add(treePath, values);
	}

	/**
	 * @param newTree
	 */
	public void add(ConfigurationTree newTree) {
		propTree.add(newTree.propTree);
	}

	/**
	 * @param key
	 * @return
	 */
	public ConfigurationTree add(String key) {
		String[] keys = getPath(key);
		ListedHashTree tree = propTree;
		for (int i = 0; i < keys.length; i++) {
			tree = (ListedHashTree) tree.add(keys[i]);
		}
		return makeSubtree(tree);
	}

	public ConfigurationTree addRaw(String key, String value) {
		ListedHashTree tree = (ListedHashTree) propTree.add(key, value);
		return makeSubtree(tree);
	}

	public ConfigurationTree addRaw(String key) {
		ListedHashTree tree = (ListedHashTree) propTree.add(key);
		return makeSubtree(tree);
	}

	/**
	 * @param key
	 * @param values
	 */
	public void add(String key, Collection values) {
		propTree.add(getPath(key), values);
	}

	/**
	 * @param key
	 * @param subTree
	 */
	public void add(String key, ConfigurationTree subTree) {
		propTree.getTree(getPath(key)).add(subTree.propTree);
	}

	/**
	 * @param key
	 * @param value
	 * @return
	 */
	public ConfigurationTree add(String key, String value) {
		return makeSubtree((ListedHashTree) propTree.add(getPath(key), value));
	}

	public Properties getAsProperties(String key) {
		return getAsProperties(getTree(key));
	}

	public Properties getAsProperties() {
		return getAsProperties(this);
	}

	protected Properties getAsProperties(ConfigurationTree tree) {
		Properties props = new Properties();
		if (tree == null)
			return props;
		String[] propNames = tree.getPropertyNames();
		if (propNames == null)
			return props;
		for (int i = 0; i < propNames.length; i++) {
			if (tree.getProperty(propNames[i]) != null)
				props.setProperty(propNames[i], tree.getProperty(propNames[i]));
		}
		return props;
	}

	/**
	 * @param key
	 * @param values
	 */
	public void add(String key, String[] values) {
		propTree.add(getPath(key), values);
	}

	/**
	 * @param keys
	 */
	public void add(String[] keys) {
		propTree.add(keys);
	}

	/**
	 * @param treePath
	 * @param values
	 */
	public void add(String[] treePath, Collection values) {
		propTree.add(treePath, values);
	}

	/**
	 * @param treePath
	 * @param value
	 * @return
	 */
	public ConfigurationTree add(String[] treePath, String value) {
		return makeSubtree((ListedHashTree) propTree.add(treePath, value));
	}

	/**
	 * @param treePath
	 * @param values
	 */
	public void add(String[] treePath, String[] values) {
		propTree.add(treePath, values);
	}

	public void add(Properties props) {
		Iterator iter = props.keySet().iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			add(key, props.getProperty(key));
		}
	}

	/**
	 * @param treePath
	 * @return
	 */
	protected ConfigurationTree addTreePath(Collection treePath) {
		return makeSubtree((ListedHashTree) propTree.addTreePath(treePath));
	}

	/**
	 * 
	 */
	public void clear() {
		propTree.clear();
	}

	/**
	 * @param o
	 * @return
	 */
	public boolean containsKey(String o) {
		return propTree.getTree(getPath(o)) != null;
	}

	/**
	 * @param value
	 * @return
	 */
	public boolean containsValue(String value) {
		return propTree.getTree(getPath(value)) != null;
	}

	protected String[] getPath(String key) {
		if (key != null) {
            //JDK 1.4 String[] keys = key.split("/");
			String[] keys = JOrphanUtils.split(key,"/");
			return keys;
		}
		return new String[0];
	}

	public String getProperty(String key, String def) {
		return getProperty(getPath(key), def);
	}

	/**
	 * @param key
	 * @return
	 */
	public String getProperty(String key) {
		return getProperty(getPath(key), null);
	}

	public String getProperty(String[] keys, String def) {
		HashTree subTree = propTree.getTree(keys);
		if (subTree != null) {
			if (subTree.list() == null || subTree.list().size() == 0) {
				return def;
			} else if (subTree.list().size() == 1) {
				return (String) subTree.getArray()[0];
			} else {
				return def;
			}
		} else {
			return def;
		}
	}

	public String getProperty(String[] keys) {
		return getProperty(keys, null);
	}

	/**
	 * @return
	 */
	public String[] getPropertyNames() {
		return convertArray(propTree.getArray());
	}

	/**
	 * @param vals
	 * @return
	 */
	private String[] convertArray(Object[] vals) {
		if (vals != null) {
			String[] props = new String[vals.length];
			for (int i = 0; i < vals.length; i++)
				props[i] = (String) vals[i];
			return props;
		}
		return null;
	}

	/**
	 * @param treePath
	 * @return
	 */
	public String[] getPropertyNames(Collection treePath) {
		return convertArray(propTree.getArray(treePath));
	}

	/**
	 * @param key
	 * @return
	 */
	public String[] getPropertyNames(String key) {
		return convertArray(propTree.getArray(getPath(key)));
	}

	/**
	 * @param treePath
	 * @return
	 */
	public String[] getPropertyNames(String[] treePath) {
		return convertArray(propTree.getArray(treePath));
	}

	/**
	 * @param treePath
	 * @return
	 */
	public ConfigurationTree getTree(Collection treePath) {
		ListedHashTree subTree = (ListedHashTree) propTree.getTree(treePath);
		return makeSubtree(subTree);
	}

	/**
	 * @param key
	 * @return
	 */
	public ConfigurationTree getTree(String key) {
		ListedHashTree subTree = (ListedHashTree) propTree.getTree(getPath(key));
		return makeSubtree(subTree);
	}

	/**
	 * @param treePath
	 * @return
	 */
	public ConfigurationTree getTree(String[] treePath) {
		ListedHashTree subTree = (ListedHashTree) propTree.getTree(treePath);
		return makeSubtree(subTree);
	}

	/**
	 * @param subTree
	 * @return
	 */
	private ConfigurationTree makeSubtree(ListedHashTree subTree) {
		if (subTree != null)
			return new ConfigurationTree(subTree);
		else
			return null;
	}

	/**
	 * @param treePath
	 * @return
	 */
	protected ConfigurationTree getTreePath(Collection treePath) {
		ListedHashTree subTree = (ListedHashTree) propTree.getTree(treePath);
		return makeSubtree(subTree);
	}

	/**
	 * @return
	 */
	public boolean isEmpty() {
		return propTree.isEmpty();
	}

	/**
	 * @return
	 */
	public Collection listPropertyNames() {
		return propTree.list();
	}

	/**
	 * @param treePath
	 * @return
	 */
	public Collection listPropertyNames(Collection treePath) {
		return propTree.list(treePath);
	}

	/**
	 * @param key
	 * @return
	 */
	public Collection listPropertyNames(String key) {
		return propTree.list(getPath(key));
	}

	/**
	 * @param treePath
	 * @return
	 */
	public Collection listPropertyNames(String[] treePath) {
		return propTree.list(treePath);
	}

	/**
	 * @param key
	 * @param value
	 * @return
	 */
	public String put(String key, String value) {
		propTree.add(getPath(key), value);
		return value;
	}

	/**
	 * @param map
	 */
	public void putAll(Map map) {
		propTree.putAll(map);
	}

	/**
	 * @param key
	 * @return
	 */
	public String remove(String key) {
		//JDK 1.4 String[] keys = key.split("/");
        String[] keys = JOrphanUtils.split(key,"/");
		String prop = null;
		HashTree tree = propTree;
		for (int i = 0; i < keys.length && tree != null; i++) {
			if ((i + 1) == keys.length) {
				tree = (HashTree) tree.remove(keys[i]);
				if (tree.list() != null && tree.list().size() == 1) {
					prop = (String) tree.getArray()[0];
				}
			} else {
				tree = tree.getTree(keys[i]);
			}
		}
		return prop;
	}

	/**
	 * @param currentKey
	 * @param newKey
	 */
	public void replace(String currentKey, String newKey) {
		String[] currentKeys = getPath(currentKey);
		String[] newKeys = getPath(newKey);
		ListedHashTree tree = propTree;
		if (currentKeys.length == newKeys.length) {
			for (int i = 0; i < currentKeys.length; i++) {
				tree.replace(currentKeys[i], newKeys[i]);
				tree = (ListedHashTree) tree.getTree(newKeys[i]);
			}
		}
	}

	/**
	 * @param key
	 * @return
	 */
	public ConfigurationTree search(String key) {
		return makeSubtree((ListedHashTree) propTree.search(key));
	}

	/**
	 * @param values
	 */
	public void setProperty(Collection values) {
		propTree.set(values);
	}

	/**
	 * @param treePath
	 * @param values
	 */
	public void setProperty(Collection treePath, Collection values) {
		propTree.set(treePath, values);
	}

	/**
	 * @param treePath
	 * @param values
	 */
	public void setProperty(Collection treePath, String[] values) {
		propTree.set(treePath, values);
	}

	/**
	 * @param key
	 * @param values
	 */
	public void setProperty(String key, Collection values) {
		propTree.set(getPath(key), values);
	}

	/**
	 * @param key
	 * @param t
	 */
	public void setProperty(String key, ConfigurationTree t) {
		String[] keys = getPath(key);
		ListedHashTree tree = (ListedHashTree) propTree.getTree(keys);
		if (tree != null) {
			tree.clear();
			tree.add(t.propTree);
		} else {
			propTree.add(keys);
			propTree.getTree(keys).add(t.propTree);
		}
	}

	/**
	 * @param key
	 * @param value
	 */
	public void setProperty(String key, String value) {
		ListedHashTree tree = (ListedHashTree) propTree.getTree(getPath(key));
		if (tree != null) {
			tree.clear();
			tree.add(value);
		} else {
			propTree.add(getPath(key), value);
		}
	}

	/**
	 * @param key
	 * @param values
	 */
	public void setProperty(String key, String[] values) {
		propTree.set(getPath(key), values);
	}

	/**
	 * @param treePath
	 * @param values
	 */
	public void setProperty(String[] treePath, Collection values) {
		propTree.set(treePath, values);
	}

	/**
	 * @param treePath
	 * @param values
	 */
	public void setProperty(String[] treePath, String[] values) {
		propTree.set(treePath, values);
	}

	/**
	 * @return
	 */
	public int size() {
		return propTree.size();
	}

	/**
	 * @param visitor
	 */
	public void traverse(HashTreeTraverser visitor) {
		propTree.traverse(visitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	protected Object clone() throws CloneNotSupportedException {
		ConfigurationTree config = new ConfigurationTree();
		config.propTree = (ListedHashTree) propTree.clone();
		return config;
	}

	protected void getSpaces(int level, Writer buf) throws IOException {
		for (int i = 0; i < level; i++)
			buf.write("    ");
	}

	public String toString() {
		StringWriter buf = new StringWriter();
		try {
			toXML(buf);
		} catch (IOException e) {
			// this can't happen
		}
		return buf.toString();
	}

	public static ConfigurationTree fromXML(Reader buf) throws IOException {
		String[] line = readLine(buf, null);
		ConfigurationTree tree = null;
		int nameIndex = line[0].indexOf("{");
		if (nameIndex > 0) {
			tree = new ConfigurationTree(line[0].substring(0, nameIndex).trim());
		} else {
			tree = new ConfigurationTree();
		}
		fromXML(buf, tree, line);
		return tree;
	}

	/**
	 * @param buf
	 * @param tree
	 * @throws IOException
	 */
	protected static boolean fromXML(Reader buf, ConfigurationTree tree, String[] line) throws IOException {
		boolean done = false;
		try {
            //TODO BUG: readLine returns a String array - which one should be compared?
			while (!done && !(line = readLine(buf, line)).equals("}")) {
				int equals = line[0].indexOf("=");
				if (line[0].endsWith("{")) {
					line[0] = line[0].substring(0, line[0].length() - 1).trim();
					equals = line[0].indexOf("=");
					if (equals > -1) {
						ConfigurationTree newTree = tree.add(line[0].substring(0, equals));
						newTree.setValue(line[0].substring(equals + 1));
						done = fromXML(buf, newTree, line);
					} else {
						done = fromXML(buf, tree.add(line[0]), line);
					}
				} else if (equals > -1) {
					String key = line[0].substring(0, equals);
					if ((equals + 1) < line[0].length())
						tree.addRaw(key, line[0].substring(equals + 1));
					else
						tree.addRaw(key);
				} else if (line[0].equals("}")) {
					return false;
				} else if (line[0].length() > 0) {
					tree.addRaw(line[0]);
				}
			}
		} catch (IOException e) {
			if (e.getMessage().equals("End of File")) {
				return true;
			} else
				throw e;
		}
		return false;
	}

	/**
	 * @param buf
	 * @throws IOException
	 */
	protected static String[] readLine(Reader buf, String[] extra) throws IOException {
		if (extra == null) {
			extra = new String[2];
		}
		if (extra[1] != null && extra[1].length() > 0) {
			extra[0] = extra[1];
			extra[1] = null;
			return extra;
		}
		StringBuffer line = new StringBuffer();
		int c = buf.read();
		while ((c != -1) && ((char) c != '\n') && ((char) c != '\r') && ((char) c != '}') && ((char) c != '{')) {
			line.append((char) c);
			c = buf.read();
		}
		if (c == -1)
			throw new IOException("End of File");
		if (((char) c == '}'))
			extra[1] = String.valueOf((char) c);
		else if (((char) c) == '{') {
			line.append('{');
		}
		extra[0] = line.toString().trim();
		if (extra[0].endsWith(BLOCK)) {
			extra[0] = extra[0].substring(0, extra[0].length() - BLOCK.length()) + readBlock(buf);
		}
		return extra;
	}

	protected static String readBlock(Reader buf) throws IOException {
		StringBuffer line = new StringBuffer();
		int c = buf.read();
		line.append((char) c);
		while (!line.toString().endsWith(END_BLOCK)) {
			c = buf.read();
			line.append((char) c);
		}
		return line.toString().substring(0, line.length() - END_BLOCK.length()).trim();
	}

	public void toXML(Writer buf) throws IOException {
		if (getValue() != null) {
			buf.write(getValue());
			buf.write(" {\n");
		} else
			buf.write("{\n");
		int level = 1;
		toXML(this, level, buf);
		buf.write("}");
	}

	protected boolean isLeaf(String key) {
		ConfigurationTree tree = getTree(key);
		String[] vals = tree.getPropertyNames();
		if (vals == null
				|| vals.length == 0
				|| (vals.length == 1 && (tree.listPropertyNames(vals[0]) == null || tree.listPropertyNames(vals[0])
						.size() == 0))) {
			return true;
		}
		return false;
	}

	protected void toXML(ConfigurationTree tree, int level, Writer buf) throws IOException {
		String[] entries = tree.getPropertyNames();
		for (int i = 0; i < entries.length; i++) {
			if (!VALUE.equals(entries[i])) {
				if (tree.listPropertyNames(entries[i]) == null || tree.listPropertyNames(entries[i]).size() == 0) {
					getSpaces(level, buf);
					writeLeafValue(buf, entries[i], level);
					buf.write("\n");
				} else if (tree.isLeaf(entries[i])) {
					getSpaces(level, buf);
					buf.write(entries[i]);
					buf.write("=");
					writeLeafValue(buf, tree.getPropertyNames(entries[i])[0], level);
					buf.write("\n");
				} else {
					getSpaces(level, buf);
					buf.write(entries[i]);
					if (tree.getTree(entries[i]).getValue() != null) {
						buf.write("=");
						buf.write(tree.getTree(entries[i]).getValue());
					}
					buf.write(" {\n");
					toXML(tree.getTree(entries[i]), (level + 1), buf);
					getSpaces(level, buf);
					buf.write("}\n");
				}
			}
		}
	}

	protected void writeLeafValue(Writer buf, String entry, int level) throws IOException {
		if (entry.indexOf('\n') > -1 || entry.indexOf('\r') > -1) {
			buf.write(BLOCK);
			buf.write("\n");
			buf.write(entry.trim());
			buf.write("\n");
			getSpaces(level, buf);
			buf.write(END_BLOCK);
		} else {
			buf.write(entry);
		}
	}

	/**
	 * @return Returns the value.
	 */
	public String getValue() {
		return getProperty(VALUE);
	}

	/**
	 * Get the value or return the given default value if null
	 * 
	 * @param def
	 * @return
	 */
	public String getValueOr(String def) {
		String v = getValue();
		if (v == null) {
			return def;
		}
		return v;
	}

	public String getValue(String name) {
		ConfigurationTree tree = getTree(getPath(name));
		if (tree != null) {
			return tree.getValue();
		}
		return null;
	}

	public String getValue(String key, String def) {
		String v = getValue(key);
		if (v == null) {
			return def;
		}
		return v;
	}

	/**
	 * @param value
	 *            The value to set.
	 */
	public void setValue(String value) {
		setProperty(VALUE, value);
	}

	public void setValue(String name, String value) {
		ConfigurationTree tree = getTree(getPath(name));
		if (tree != null) {
			tree.setValue(value);
		} else {
			add(name).setValue(value);
		}

	}
}
