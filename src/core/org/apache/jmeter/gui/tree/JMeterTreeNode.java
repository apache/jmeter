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

package org.apache.jmeter.gui.tree;

import java.awt.Image;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.util.Collection;

import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.jmeter.gui.GUIFactory;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author Michael Stover
 * @version $Revision$
 */
public class JMeterTreeNode extends DefaultMutableTreeNode implements NamedTreeNode {
	private static final Logger log = LoggingManager.getLoggerForClass();

	private JMeterTreeModel treeModel;

	public JMeterTreeNode() {// Allow serializable test to work
		// TODO: is the serializable test necessary now that JMeterTreeNode is
		// no longer a GUI component?
		this(null, null);
	}

	public JMeterTreeNode(TestElement userObj, JMeterTreeModel treeModel) {
		super(userObj);
		this.treeModel = treeModel;
	}

	public boolean isEnabled() {
		return ((AbstractTestElement) getTestElement()).getPropertyAsBoolean(TestElement.ENABLED);
	}

	public void setEnabled(boolean enabled) {
		getTestElement().setProperty(new BooleanProperty(TestElement.ENABLED, enabled));
		treeModel.nodeChanged(this);
	}

	public ImageIcon getIcon() {
		return getIcon(true);
	}

	public ImageIcon getIcon(boolean enabled) {
		try {
			if (getTestElement() instanceof TestBean) {
				try {
					Image img = Introspector.getBeanInfo(getTestElement().getClass())
							.getIcon(BeanInfo.ICON_COLOR_16x16);
					// If icon has not been defined, then use GUI_CLASS property
					if (img == null) {//
						Object clazz = Introspector.getBeanInfo(getTestElement().getClass()).getBeanDescriptor()
								.getValue(TestElement.GUI_CLASS);
						if (clazz == null) {
							log.error("Can't obtain GUI class for " + getTestElement().getClass().getName());
							return null;
						}
						return GUIFactory.getIcon(Class.forName((String) clazz), enabled);
					}
					return new ImageIcon(img);
				} catch (IntrospectionException e1) {
					log.error("Can't obtain icon", e1);
					throw new org.apache.jorphan.util.JMeterError(e1);
				}
			}
			return GUIFactory.getIcon(Class.forName(getTestElement().getPropertyAsString(TestElement.GUI_CLASS)),
						enabled);
		} catch (ClassNotFoundException e) {
			log.warn("Can't get icon for class " + getTestElement(), e);
			return null;
		}
	}

	public Collection getMenuCategories() {
		try {
			return GuiPackage.getInstance().getGui(getTestElement()).getMenuCategories();
		} catch (Exception e) {
			log.error("Can't get popup menu for gui", e);
			return null;
		}
	}

	public JPopupMenu createPopupMenu() {
		try {
			return GuiPackage.getInstance().getGui(getTestElement()).createPopupMenu();
		} catch (Exception e) {
			log.error("Can't get popup menu for gui", e);
			return null;
		}
	}

	public TestElement getTestElement() {
		return (TestElement) getUserObject();
	}

	public String getStaticLabel() {
		return GuiPackage.getInstance().getGui((TestElement) getUserObject()).getStaticLabel();
	}

	public String getDocAnchor() {
		return GuiPackage.getInstance().getGui((TestElement) getUserObject()).getDocAnchor();
	}

	public void setName(String name) {
		((TestElement) getUserObject()).setProperty(new StringProperty(TestElement.NAME, name));
	}

	public String getName() {
		return ((TestElement) getUserObject()).getPropertyAsString(TestElement.NAME);
	}

	public void nameChanged() {
		treeModel.nodeChanged(this);
	}
}
