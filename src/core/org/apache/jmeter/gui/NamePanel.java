// $Header$
/*
 * Copyright 2002-2004 The Apache Software Foundation.
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

package org.apache.jmeter.gui;

import java.awt.BorderLayout;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.TreeNode;

import org.apache.jmeter.gui.tree.NamedTreeNode;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.WorkBench;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.LocaleChangeEvent;

/**
 * 
 * @version $Revision$ Last updated: $Date$
 */

public class NamePanel extends JPanel implements JMeterGUIComponent {
	/** A text field containing the name. */
	private JTextField nameField = new JTextField(15);

	/** The label for the text field. */
	private JLabel nameLabel;

	/** The node which this component is providing the name for. */
	private TreeNode node;

	/**
	 * Create a new NamePanel with the default name.
	 */
	public NamePanel() {
		setName(getStaticLabel());
		init();
	}

	/**
	 * Initialize the GUI components and layout.
	 */
	private void init() {
		setLayout(new BorderLayout(5, 0));

		nameLabel = new JLabel(JMeterUtils.getResString("name"));
		nameLabel.setName("name");
		nameLabel.setLabelFor(nameField);

		nameField.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				updateName(nameField.getText());
			}

			public void removeUpdate(DocumentEvent e) {
				updateName(nameField.getText());
			}

			public void changedUpdate(DocumentEvent e) {
				// not for text fields
			}
		});

		add(nameLabel, BorderLayout.WEST);
		add(nameField, BorderLayout.CENTER);
	}

	public void clear() {
		setName(getStaticLabel());
	}

	/**
	 * Get the currently displayed name.
	 * 
	 * @return the current name
	 */
	public String getName() {
		if (nameField != null)
			return nameField.getText();
		else
			return "";
	}

	/**
	 * Set the name displayed in this component.
	 * 
	 * @param name
	 *            the name to display
	 */
	public void setName(String name) {
		super.setName(name);
		nameField.setText(name);
	}

	/**
	 * Get the tree node which this component provides the name for.
	 * 
	 * @return the tree node corresponding to this component
	 */
	protected TreeNode getNode() {
		return node;
	}

	/**
	 * Set the tree node which this component provides the name for.
	 * 
	 * @param node
	 *            the tree node corresponding to this component
	 */
	public void setNode(TreeNode node) {
		this.node = node;
	}

	/* Implements JMeterGUIComponent.configure(TestElement) */
	public void configure(TestElement testElement) {
		setName(testElement.getPropertyAsString(TestElement.NAME));
	}

	/* Implements JMeterGUIComponent.createPopupMenu() */
	public JPopupMenu createPopupMenu() {
		return null;
	}

	/* Implements JMeterGUIComponent.getStaticLabel() */
	public String getStaticLabel() {
		return JMeterUtils.getResString(getLabelResource());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#getLabelResource()
	 */
	public String getLabelResource() {
		return "root";
	}

	/* Implements JMeterGUIComponent.getMenuCategories() */
	public Collection getMenuCategories() {
		return null;
	}

	/* Implements JMeterGUIComponent.createTestElement() */
	public TestElement createTestElement() {
		WorkBench wb = new WorkBench();
		modifyTestElement(wb);
		return wb;
	}

	/* Implements JMeterGUIComponent.modifyTestElement(TestElement) */
	public void modifyTestElement(TestElement wb) {
		wb.setProperty(new StringProperty(TestElement.NAME, getName()));
		wb.setProperty(new StringProperty(TestElement.GUI_CLASS, this.getClass().getName()));
		wb.setProperty(new StringProperty(TestElement.TEST_CLASS, WorkBench.class.getName()));
	}

	/**
	 * Called when the name changes. The tree node which this component names
	 * will be notified of the change.
	 * 
	 * @param newValue
	 *            the new name
	 */
	private void updateName(String newValue) {
		if (getNode() != null) {
            ((NamedTreeNode)getNode()).nameChanged();
		}
	}

	/**
	 * Called when the locale is changed so that the label can be updated. This
	 * method is not currently used.
	 * 
	 * @param event
	 *            the event to be handled
	 */
	public void localeChanged(LocaleChangeEvent event) {
		nameLabel.setText(JMeterUtils.getResString(nameLabel.getName()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#getDocAnchor()
	 */
	public String getDocAnchor() {
		// TODO Auto-generated method stub
		return null;
	}
}
