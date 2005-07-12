// $Header$
/*
 * ====================================================================
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

// The developers of JMeter and Apache are greatful to the developers
// of HTMLParser for giving Apache Software Foundation a non-exclusive
// license. The performance benefits of HTMLParser are clear and the
// users of JMeter will benefit from the hard work the HTMLParser
// team. For detailed information about HTMLParser, the project is
// hosted on sourceforge at http://htmlparser.sourceforge.net/.
//
// HTMLParser was originally created by Somik Raha in 2000. Since then
// a healthy community of users has formed and helped refine the
// design so that it is able to tackle the difficult task of parsing
// dirty HTML. Derrick Oswald is the current lead developer and was kind
// enough to assist JMeter.
package org.htmlparser.beans;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

/**
 * Demo of beans. Created on December 30, 2002, 7:54 PM
 * 
 * @author Derrick Oswald
 */
public class BeanyBaby extends javax.swing.JFrame implements java.beans.PropertyChangeListener,
		java.awt.event.ActionListener, java.awt.event.MouseListener {
	/**
	 * Bread crumb trail of visited URLs.
	 */
	java.util.Vector mTrail;

	/**
	 * Current position on the bread crumb trail.
	 */
	int mCrumb;

	/** Creates new form BeanyBaby */
	public BeanyBaby() {
		initComponents();
		mTrail = new java.util.Vector(25);
		mCrumb = -1;

		// shenanigans to get the splitter bar at the midpoint
		show();
		mSplitPane.setDividerLocation(0.5);
		hide();

		// set up twinning
		mLinkBean.addPropertyChangeListener(this);
		mStringBean.addPropertyChangeListener(this);
		// set up user input
		mTextField.addActionListener(this);
		mLinkBean.addMouseListener(this);

		// set initial checkbox states
		mLinks.setSelected(mStringBean.getLinks());
		mCollapse.setSelected(mStringBean.getCollapse());
		mNobreak.setSelected(mStringBean.getReplaceNonBreakingSpaces());
	}

	//
	// PropertyChangeListener interface
	//

	/**
	 * This method ties the two beans together on the same connection. Whenever
	 * a property changes on one bean, make sure the URL properties agree by
	 * setting the connection from one to the other.
	 * 
	 * @param event
	 *            The event describing the event source and the property that
	 *            has changed.
	 */
	public void propertyChange(java.beans.PropertyChangeEvent event) {
		Object source;
		String name;

		source = event.getSource();
		if (source == mLinkBean) {
			if (!mLinkBean.getURL().equals(mStringBean.getURL()))
				mStringBean.setConnection(mLinkBean.getConnection());
		} else if (source == mStringBean) {
			if (!mStringBean.getURL().equals(mLinkBean.getURL()))
				mLinkBean.setConnection(mStringBean.getConnection());
			// check for menu status changes
			name = event.getPropertyName();
			if (name.equals(StringBean.PROP_LINKS_PROPERTY))
				mLinks.setSelected(((Boolean) event.getNewValue()).booleanValue());
			else if (name.equals(StringBean.PROP_COLLAPSE_PROPERTY))
				mCollapse.setSelected(((Boolean) event.getNewValue()).booleanValue());
			else if (name.equals(StringBean.PROP_REPLACE_SPACE_PROPERTY))
				mNobreak.setSelected(((Boolean) event.getNewValue()).booleanValue());
		}
	}

	//
	// ActionListener interface
	//

	public void actionPerformed(java.awt.event.ActionEvent event) {
		Object source;
		String url;
		String name;
		JMenuItem item;

		source = event.getSource();
		if (source == mTextField) {
			url = mTextField.getText();
			mTextField.selectAll();
			setURL(url);
		} else if (source instanceof JCheckBoxMenuItem) {
			item = (JMenuItem) source;
			name = item.getName();
			if ("Links".equals(name))
				mStringBean.setLinks(item.isSelected());
			else if ("Collapse".equals(name))
				mStringBean.setCollapse(item.isSelected());
			else if ("Nobreak".equals(name))
				mStringBean.setReplaceNonBreakingSpaces(item.isSelected());
		} else if (source instanceof JMenuItem) {
			name = ((JMenuItem) source).getName();
			if ("Back".equals(name)) {
				if (mCrumb > 0) {
					mCrumb--;
					url = (String) mTrail.elementAt(mCrumb);
					mCrumb--;
					setURL(url);
				}
			} else if ("Forward".equals(name)) {
				if (mCrumb < mTrail.size()) {
					mCrumb++;
					url = (String) mTrail.elementAt(mCrumb);
					mCrumb--;
					setURL(url);
				}
			}
		}

	}

	//
	// MouseListener interface
	//
	/**
	 * Invoked when the mouse button has been clicked (pressed and released) on
	 * a component.
	 */
	public void mouseClicked(java.awt.event.MouseEvent event) {
		int index;
		String url;

		if (2 == event.getClickCount()) {
			index = mLinkBean.locationToIndex(event.getPoint());
			url = mLinkBean.getModel().getElementAt(index).toString();
			setURL(url);
		}
	}

	/**
	 * Invoked when the mouse enters a component.
	 */
	public void mouseEntered(java.awt.event.MouseEvent event) {
	}

	/**
	 * Invoked when the mouse exits a component.
	 */
	public void mouseExited(java.awt.event.MouseEvent event) {
	}

	/**
	 * Invoked when a mouse button has been pressed on a component.
	 */
	public void mousePressed(java.awt.event.MouseEvent event) {
	}

	/**
	 * Invoked when a mouse button has been released on a component.
	 */
	public void mouseReleased(java.awt.event.MouseEvent event) {
	}

	//
	// API control
	//
	public void setURL(String url) {
		mTextField.setText(url);
		mCrumb++;
		if (mTrail.size() <= mCrumb)
			mTrail.addElement(url);
		else
			mTrail.setElementAt(url, mCrumb);
		mLinkBean.setURL(url);

		// update navigation menu
		mBack.setEnabled(mCrumb > 0);
		mForward.setEnabled(mCrumb + 1 < mTrail.size());
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() // GEN-BEGIN:initComponents
	{
		javax.swing.JMenuBar menubar;
		javax.swing.JScrollPane pane1;
		javax.swing.JScrollPane pane2;
		javax.swing.JMenu go;
		javax.swing.JMenu options;
		javax.swing.JPanel panel;

		menubar = new javax.swing.JMenuBar();
		setJMenuBar(menubar);
		go = new javax.swing.JMenu();
		mBack = new javax.swing.JMenuItem();
		mForward = new javax.swing.JMenuItem();
		options = new javax.swing.JMenu();
		mLinks = new javax.swing.JCheckBoxMenuItem();
		mCollapse = new javax.swing.JCheckBoxMenuItem();
		mNobreak = new javax.swing.JCheckBoxMenuItem();
		panel = new javax.swing.JPanel();
		mSplitPane = new javax.swing.JSplitPane();
		pane1 = new javax.swing.JScrollPane();
		mLinkBean = new org.htmlparser.beans.HTMLLinkBean();
		pane2 = new javax.swing.JScrollPane();
		mStringBean = new org.htmlparser.beans.HTMLTextBean();
		mTextField = new javax.swing.JTextField();

		go.setMnemonic('G');
		go.setText("Go");
		go.setToolTipText("crude URL navigation");
		mBack.setMnemonic('B');
		mBack.setText("Back");
		mBack.setToolTipText("back one URL");
		mBack.setName("Back");
		mBack.addActionListener(this);
		go.add(mBack);

		mForward.setMnemonic('F');
		mForward.setText("Forward");
		mForward.setToolTipText("forward one URL");
		mForward.setName("Forward");
		mForward.addActionListener(this);
		go.add(mForward);

		menubar.add(go);

		options.setMnemonic('O');
		options.setText("Options");
		options.setToolTipText("Bean settings");
		mLinks.setMnemonic('L');
		mLinks.setText("Links");
		mLinks.setToolTipText("show/hide links in text");
		mLinks.setName("Links");
		mLinks.addActionListener(this);
		options.add(mLinks);

		mCollapse.setMnemonic('C');
		mCollapse.setText("Collapse");
		mCollapse.setToolTipText("collapse/retain whitespace sequences");
		mCollapse.setName("Collapse");
		mCollapse.addActionListener(this);
		options.add(mCollapse);

		mNobreak.setMnemonic('N');
		mNobreak.setText("Non-breaking Spaces");
		mNobreak.setToolTipText("replace/retain non-breaking spaces");
		mNobreak.setName("Nobreak");
		mNobreak.addActionListener(this);
		options.add(mNobreak);

		menubar.add(options);

		setTitle("BeanyBaby");
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				exitForm(evt);
			}
		});

		panel.setLayout(new java.awt.BorderLayout());

		pane1.setViewportView(mLinkBean);

		mSplitPane.setLeftComponent(pane1);

		pane2.setViewportView(mStringBean);

		mSplitPane.setRightComponent(pane2);

		panel.add(mSplitPane, java.awt.BorderLayout.CENTER);

		mTextField.setToolTipText("Enter the URL to view");
		panel.add(mTextField, java.awt.BorderLayout.SOUTH);

		getContentPane().add(panel, java.awt.BorderLayout.CENTER);

		pack();
		java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		setSize(new java.awt.Dimension(640, 480));
		setLocation((screenSize.width - 640) / 2, (screenSize.height - 480) / 2);
	} // GEN-END:initComponents

	/** Exit the Application */
	private void exitForm(java.awt.event.WindowEvent evt) {
		// GEN-FIRST:event_exitForm
		System.exit(0);
	} // GEN-LAST:event_exitForm

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private org.htmlparser.beans.HTMLLinkBean mLinkBean;

	private javax.swing.JMenuItem mForward;

	private javax.swing.JMenuItem mBack;

	private javax.swing.JCheckBoxMenuItem mCollapse;

	private javax.swing.JTextField mTextField;

	private javax.swing.JSplitPane mSplitPane;

	private javax.swing.JCheckBoxMenuItem mLinks;

	private org.htmlparser.beans.HTMLTextBean mStringBean;

	private javax.swing.JCheckBoxMenuItem mNobreak;

	// End of variables declaration//GEN-END:variables

	/**
	 * Unit test.
	 */
	public static void main(String[] args) {
		BeanyBaby bb = new BeanyBaby();
		bb.show();
		bb.setURL("http://www.netbeans.org");
	}
}
