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

package org.apache.jmeter.control.gui;

import java.awt.FlowLayout;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.ModuleController;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.WorkBench;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;

/**
 * ModuleController Gui.
 * 
 * @version $Revision$ on $Date$
 */
public class ModuleControllerGui extends AbstractControllerGui /*
																 * implements
																 * UnsharedComponent
																 */
{

	private JMeterTreeNode selected = null;

	private JComboBox nodes;

	private DefaultComboBoxModel nodesModel;

	private JLabel warningLabel;

	public static final String CONTROLLER = "Module To Run";

	// TODO should be a resource, and probably ought to be resolved at run-time
	// (to allow language change)

	/**
	 * Initializes the gui panel for the ModuleController instance.
	 */
	public ModuleControllerGui() {
		init();
	}

	public String getLabelResource() {
		return "module_controller_title";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#configure(TestElement)
	 */
	public void configure(TestElement el) {
		super.configure(el);
		ModuleController controller = (ModuleController) el;
		this.selected = controller.getSelectedNode();
		if (selected == null && controller.getNodePath() != null)
			warningLabel.setText(JMeterUtils.getResString("module_controller_warning")
					+ renderPath(controller.getNodePath()));
		else
			warningLabel.setText("");
		reinitialize();
	}

	private String renderPath(Collection path) {
		Iterator iter = path.iterator();
		StringBuffer buf = new StringBuffer();
		boolean first = true;
		while (iter.hasNext()) {
			if (first) {
				first = false;
				iter.next();
				continue;
			}
			buf.append(iter.next());
			if (iter.hasNext())
				buf.append(" > ");
		}
		return buf.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
	 */
	public TestElement createTestElement() {
		ModuleController mc = new ModuleController();
		configureTestElement(mc);
		if (selected != null) {
			mc.setSelectedNode(selected);
		}
		return mc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
	 */
	public void modifyTestElement(TestElement element) {
		configureTestElement(element);
		TreeNodeWrapper tnw = (TreeNodeWrapper) nodesModel.getSelectedItem();
		if (tnw != null && tnw.getTreeNode() != null) {
			selected = tnw.getTreeNode();
			if (selected != null) {
				((ModuleController) element).setSelectedNode(selected);
			}
		}
	}

	public JPopupMenu createPopupMenu() {
		JPopupMenu menu = new JPopupMenu();
		JMenu addMenu = MenuFactory.makeMenus(new String[] { MenuFactory.CONFIG_ELEMENTS, MenuFactory.ASSERTIONS,
				MenuFactory.TIMERS, MenuFactory.LISTENERS, }, JMeterUtils.getResString("Add"), "Add");
		menu.add(addMenu);
		MenuFactory.addEditMenu(menu, true);
		MenuFactory.addFileMenu(menu);
		return menu;
	}

	private void init() {
		setLayout(new VerticalLayout(5, VerticalLayout.LEFT, VerticalLayout.TOP));
		setBorder(makeBorder());
		add(makeTitlePanel());

		// DROP-DOWN MENU
		JPanel modulesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
		modulesPanel.add(new JLabel(CONTROLLER));
		nodesModel = new DefaultComboBoxModel();
		nodes = new JComboBox(nodesModel);
		reinitialize();
		modulesPanel.add(nodes);
		warningLabel = new JLabel("");
		modulesPanel.add(warningLabel);
		add(modulesPanel);
	}

	private void reinitialize() {
		TreeNodeWrapper current;
		nodesModel.removeAllElements();
		GuiPackage gp = GuiPackage.getInstance();
		JMeterTreeNode root;
		if (gp != null) {
			root = (JMeterTreeNode) GuiPackage.getInstance().getTreeModel().getRoot();
			buildNodesModel(root, "", 0);
		}
		if (selected != null) {
			for (int i = 0; i < nodesModel.getSize(); i++) {
				current = (TreeNodeWrapper) nodesModel.getElementAt(i);
				if ((current.getTreeNode() == null && selected == null)
						|| (current.getTreeNode() != null && current.getTreeNode().equals(selected))) {
					nodesModel.setSelectedItem(current);
					break;
				}
			}
		}
	}

	private void buildNodesModel(JMeterTreeNode node, String parent_name, int level) {
		if (level == 0 && (parent_name == null || parent_name.length() == 0)) {
			nodesModel.addElement(new TreeNodeWrapper(null, ""));
		}
		String seperator = " > ";
		if (node != null) {
			for (int i = 0; i < node.getChildCount(); i++) {
				StringBuffer name = new StringBuffer();
				JMeterTreeNode cur = (JMeterTreeNode) node.getChildAt(i);
				TestElement te = cur.getTestElement();
				if (te instanceof ThreadGroup) {
					name.append(parent_name);
					name.append(cur.getName());
					name.append(seperator);
					buildNodesModel(cur, name.toString(), level);
				} else if (te instanceof Controller && !(te instanceof ModuleController)) {
					name.append(spaces(level));
					name.append(parent_name);
					name.append(cur.getName());
					TreeNodeWrapper tnw = new TreeNodeWrapper(cur, name.toString());
					nodesModel.addElement(tnw);
					name = new StringBuffer();
					name.append(cur.getName());
					name.append(seperator);
					buildNodesModel(cur, name.toString(), level + 1);
				} else if (te instanceof TestPlan || te instanceof WorkBench) {
					name.append(cur.getName());
					name.append(seperator);
					buildNodesModel(cur, name.toString(), 0);
				}
			}
		}
	}

	private String spaces(int level) {
		int multi = 4;
		StringBuffer spaces = new StringBuffer(level * multi);
		for (int i = 0; i < level * multi; i++) {
			spaces.append(" ");
		}
		return spaces.toString();
	}
}

class TreeNodeWrapper {

	private JMeterTreeNode tn;

	private String label;

	private TreeNodeWrapper() {
	};

	public TreeNodeWrapper(JMeterTreeNode tn, String label) {
		this.tn = tn;
		this.label = label;
	}

	public JMeterTreeNode getTreeNode() {
		return tn;
	}

	public String toString() {
		return label;
	}

}
