package org.apache.jmeter.visualizers;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.jmeter.samplers.SampleResult;

public class TreeNodeRenderer extends DefaultTreeCellRenderer {

	public TreeNodeRenderer() {
		super();
	}
	
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
			boolean leaf, int row, boolean focus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, focus);
		Object obj = ((DefaultMutableTreeNode) value).getUserObject();
		if(obj instanceof SampleResult)
		{
			if (!((SampleResult) obj).isSuccessful()) {
				this.setForeground(Color.red);
			}
		}
		return this;
	}

}
