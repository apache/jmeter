// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.jmeter.ejb.jndi.config.gui;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.NamePanel;
import org.apache.jmeter.gui.util.VerticalLayout;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
/**
 * Provides the gui interface to configure remote method execution
 * @author	Khor Soon Hin
 * @version	$Revision$ Last Updated: $Date$
 * Created	2001 Dec 24
 */
public class MethodConfigGui
	extends JPanel
	implements ModelSupported, ActionListener, TreeSelectionListener
{
	transient private static Logger log = LoggingManager.getLoggerForClass();
	protected static final String REFLECT = "MethodConfigGui.reflect";
	protected static final String INVOKE = "MethodConfigGui.invoke";
	protected static final String STRING_CLASS = "java.lang.String";
	protected JComboBox methodHomeNameBox;
	protected JComboBox methodRemoteNameBox;
	protected JComboBox remoteInterfaceBox;
	protected DefaultMutableTreeNode rootHome;
	protected DefaultMutableTreeNode rootRemote;
	protected DefaultTreeModel treeModelHome;
	protected DefaultTreeModel treeModelRemote;
	protected NamePanel namePanel;
	protected JPanel methodHomeNamePanel;
	protected JPanel methodHomeParmsPanel;
	protected JPanel remoteInterfaceListPanel;
	protected JPanel methodRemoteNamePanel;
	protected JPanel methodRemoteParmsPanel;
	protected JPanel controlPanel;
	protected JButton actionButton;
	protected JTree jTreeHome;
	protected JTree jTreeRemote;
	protected boolean displayName;
	protected MethodConfig model;
	protected Frame frame;
	// The frame is required because the JDialog we create must be modal
	// and to be modal it must be modal relative to a frame.  We want
	// the dialog boxes to be modal so that user must fill them up before
	// they can do anything with jmeter
	public MethodConfigGui()
	{
		displayName = true;
	}
	public MethodConfigGui(boolean displayName)
	{
		this.displayName = displayName;
	}
	//----- ModelSupported interface : start -----
	public void setModel(Object model)
	{
		this.model = (MethodConfig) model;
		init();
	}
	public void updateGui()
	{
		log.debug("Start : updateGui1");
		log.info("updateGui1");
		// the method name box will always be displayed regardless of the state
		// of the MethodConfig of this gui
		String methodName = model.getMethodHomeName();
		log.debug("updateGui1 : home method name - " + methodName);
		String[] strings = model.getMethodHomeList();
		setupPullDown(methodHomeNameBox, methodName, strings);
		// if methodName is null set the home method to be the first entry
		// as setup in the home method name JComboBox by setupPullDown
		// This is to ensure that if the user does not change the JComboBox
		// the first value is set anyway.
		if (methodName == null && strings != null)
		{
			model.setMethodHomeName(strings[0]);
		}
		// if the state of the MethodConfig of this gui after 
		// MethodConfig.METHOD_GET_HOME_NAMES, display panel to get
		// parms of the selected method
		int childCount = treeModelHome.getChildCount(rootHome);
		if (log.isDebugEnabled())
		{
			log.debug("updateGui1 : state - " + model.getState());
			log.debug("updateGui1 : METHOD_GET_HOME_NAMES");
			log.debug("updateGui1 : rootHome child count - " + childCount);
		}
		// if model is in state after getting home method name AND
		// rootHome(method) has no child(parms set) yet then get them
		if (model.getState() > MethodConfig.METHOD_GET_HOME_PARMS
			&& childCount == 0)
		{
			log.debug("updateGui1 : METHOD_GET_HOME_PARMS");
			rootHome.setUserObject(model.getMethodHomeName());
			//      methodParmsPanel.setVisible(true);
			// get all the parms
			Class[] parmTypes = model.getMethodHomeParms();
			// add all the parms into a JTree
			for (int i = 0; i < parmTypes.length; i++)
			{
				log.debug("updateGui1 : parmType #" + i + " - " + parmTypes[i]);
				recurseParm(parmTypes[i], rootHome, i, treeModelHome);
			}
			// if the chosen method has no parms then updating the value of
			// root node with the method name doesn't seem to trigger a redraw of
			// the tree so call treeDidChange()
			if (parmTypes.length == 0)
			{
				jTreeHome.treeDidChange();
			}
		}
		// if model is in state after getting home method name and
		// all home method parms gave been obtained then get list of
		// remote interfaces
		if (model.getState() > MethodConfig.METHOD_SELECT_REMOTE_INTERFACE)
		{
			log.debug("METHOD_SELECT_REMOTE_INTERFACE");
			// remoteInterfaceType is the remote interface selected by the user
			// remoteInterfaces contains the list of remote interfaces returned
			// by home method
			Object remoteInterfaceType = model.getRemoteInterfaceType();
			String interfaceName = null;
			if (remoteInterfaceType != null)
			{
				interfaceName = remoteInterfaceType.toString();
			}
			ArrayList remoteInterfaces = (ArrayList) model.getRemoteInterfaceList();
			if (log.isDebugEnabled())
			{
				log.debug("updateGui1 : remote interfaces - " + remoteInterfaces);
				log.debug(
					"updateGui1 : remoteInterfacesType - " + remoteInterfaces.getClass());
			}
			// prepare variable strings to contain a list of all the names of
			// the remote interfaces
			Object[] remoteInterfacesArray = remoteInterfaces.toArray();
			strings = new String[remoteInterfacesArray.length];
			for (int i = 0; i < remoteInterfacesArray.length; i++)
			{
				strings[i] = remoteInterfacesArray[i].toString();
			}
			setupPullDown(remoteInterfaceBox, interfaceName, strings);
			// if interfaceName is null set the remote interface to be the first 
			// entry as setup in the remote interface name JComboBox by setupPullDown.
			// This is to ensure that if the user does not change the JComboBox
			// the first value is set anyway.
			if (interfaceName == null && remoteInterfacesArray != null)
			{
				model.setRemoteInterfaceType(remoteInterfacesArray[0]);
			}
		}
		// if model is in state after user selects the list of remote interface
		// then get a list of remote method names
		if (model.getState() > MethodConfig.METHOD_GET_REMOTE_NAMES)
		{
			log.debug("METHOD_GET_REMOTE_NAMES");
			methodName = model.getMethodRemoteName();
			log.debug("updateGui1 : remote method name - " + methodName);
			strings = model.getMethodRemoteList();
			setupPullDown(methodRemoteNameBox, methodName, strings);
			childCount = treeModelRemote.getChildCount(rootRemote);
			if (log.isDebugEnabled())
			{
				log.debug("updateGui1 : rootRemote child count - " + childCount);
			}
		}
		// if model is in state after getting remote interface AND
		// rootRemote(method) has no child(parms set) yet then get them
		if (model.getState() > MethodConfig.METHOD_GET_REMOTE_PARMS
			&& childCount == 0)
		{
			log.debug("METHOD_GET_REMOTE_PARMS");
			rootRemote.setUserObject(model.getMethodRemoteName());
			//      methodParmsPanel.setVisible(true);
			// get all the parms
			Class[] parmTypes = model.getMethodRemoteParms();
			// add all the parms into a JTree
			for (int i = 0; i < parmTypes.length; i++)
			{
				log.debug("updateGui1 : parmType #" + i + " - " + parmTypes[i]);
				recurseParm(parmTypes[i], rootHome, i, treeModelHome);
			}
			// if the chosen method has no parms then updating the value of
			// root node with the method name doesn't seem to trigger a redraw of
			// the tree so call treeDidChange()
			if (parmTypes.length == 0)
			{
				jTreeRemote.treeDidChange();
			}
		}
		// if the state of the MethodConfig of this gui is
		// MethodConfig.METHOD_GET_HOME_PARMS, display panel
		if (displayName)
		{
			namePanel.updateGui();
		}
		log.debug("End : updateGui1");
	}
	//----- ModelSupported interface : end -----
	protected void init()
	{
		log.info("Start : init1");
		// The frame is required because the JDialog we create must be modal
		// and to be modal it must be modal relative to a frame.  We want
		// the dialog boxes to be modal so that user must fill them up before
		// they can do anything with jmeter
		GuiPackage guiPkg = GuiPackage.getInstance();
		frame = guiPkg.getMainFrame();
		model.setState(MethodConfig.METHOD_GET_HOME_NAMES);
		model.setGui(this);
		methodHomeNameBox = new JComboBox();
		methodHomeNameBox.setEditable(false);
		methodHomeNameBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		methodHomeNameBox.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				log.debug("actionPerformed1 : Home name method JComboBox changed");
				// change in method name so do the following
				JComboBox comboBox = (JComboBox) e.getSource();
				String method = (String) methodHomeNameBox.getSelectedItem();
				model.setMethodHomeName(method);
				model.setState(MethodConfig.METHOD_GET_HOME_PARMS);
				resetHomeMethodParms();
				resetRemoteInterfaceList();
				resetRemoteMethodName();
				resetRemoteMethodParms();
				//            methodParmsPanel.setVisible(false);
				actionButton.setText(
					JMeterUtils.getResString("jndi_method_button_reflect"));
				updateGui();
			}
		});
		remoteInterfaceBox = new JComboBox();
		remoteInterfaceBox.setEditable(false);
		remoteInterfaceBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		remoteInterfaceBox.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				log.debug("actionPerformed1 : Remote Interface JComboBox changed");
				JComboBox comboBox = (JComboBox) e.getSource();
				String interfaceName = (String) remoteInterfaceBox.getSelectedItem();
				// compare interface selected with the ones in the remote interface
				// list and store the corresponding object in MethodConfig
				ArrayList remoteInterfaceList =
					(ArrayList) model.getRemoteInterfaceList();
				if (remoteInterfaceList != null)
				{
					Object[] remoteInterfaceListArray = remoteInterfaceList.toArray();
					int i = 0;
					boolean found = false;
					String remoteInterfaceListName = null;
					Object selectedInterface = null;
					while (i < remoteInterfaceListArray.length && !found)
					{
						remoteInterfaceListName = remoteInterfaceListArray[i].toString();
						if (remoteInterfaceListName.equals(interfaceName))
						{
							found = true;
							selectedInterface = remoteInterfaceListArray[i];
						}
						i++;
					}
					model.setRemoteInterfaceType(selectedInterface);
					model.setState(MethodConfig.METHOD_GET_REMOTE_NAMES);
				}
				resetRemoteMethodName();
				resetRemoteMethodParms();
				//            methodParmsPanel.setVisible(false);
				actionButton.setText(
					JMeterUtils.getResString("jndi_method_button_reflect"));
				updateGui();
			}
		});
		methodRemoteNameBox = new JComboBox();
		methodRemoteNameBox.setEditable(false);
		methodRemoteNameBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		methodRemoteNameBox.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				log.debug("actionPerformed1 : Remote method name JComboBox changed");
				// change in method name so do the following
				JComboBox comboBox = (JComboBox) e.getSource();
				String method = (String) methodRemoteNameBox.getSelectedItem();
				model.setMethodRemoteName(method);
				model.setState(MethodConfig.METHOD_GET_REMOTE_PARMS);
				resetRemoteMethodParms();
				//            methodParmsPanel.setVisible(false);
				actionButton.setText(
					JMeterUtils.getResString("jndi_method_button_reflect"));
				updateGui();
			}
		});
		//rootHome = new DefaultMutableTreeNode("Root");
		//treeModelHome = new DefaultTreeModel(rootHome);
		this.setLayout(
			new VerticalLayout(5, VerticalLayout.LEFT, VerticalLayout.TOP));
		JPanel mainPanel = new JPanel();
		Border margin = new EmptyBorder(10, 10, 5, 10);
		mainPanel.setBorder(margin);
		mainPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));
		// title
		JLabel panelTitleLabel =
			new JLabel(JMeterUtils.getResString("jndi_method_title"));
		Font curFont = panelTitleLabel.getFont();
		int curFontSize = curFont.getSize();
		curFontSize += 4;
		panelTitleLabel.setFont(
			new Font(curFont.getFontName(), curFont.getStyle(), curFontSize));
		mainPanel.add(panelTitleLabel);
		// name
		namePanel = new NamePanel(model);
		mainPanel.add(namePanel);
		// method properties
		JPanel jndiPanel = new JPanel();
		jndiPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));
		jndiPanel.setBorder(
			BorderFactory.createTitledBorder(
				JMeterUtils.getResString("jndi_method_name")));
		methodHomeNamePanel = getMethodHomeNamePanel();
		methodHomeParmsPanel = getMethodHomeParmsPanel();
		remoteInterfaceListPanel = getRemoteInterfaceListPanel();
		methodRemoteNamePanel = getMethodRemoteNamePanel();
		methodRemoteParmsPanel = getMethodRemoteParmsPanel();
		jndiPanel.add(methodHomeNamePanel);
		jndiPanel.add(methodHomeParmsPanel);
		jndiPanel.add(remoteInterfaceListPanel);
		jndiPanel.add(methodRemoteNamePanel);
		jndiPanel.add(methodRemoteParmsPanel);
		controlPanel = new JPanel();
		actionButton =
			new JButton(JMeterUtils.getResString("jndi_method_button_reflect"));
		actionButton.addActionListener(this);
		actionButton.setActionCommand(REFLECT);
		controlPanel.add(actionButton);
		jndiPanel.add(controlPanel);
		mainPanel.add(jndiPanel);
		this.add(mainPanel);
		//methodHomeParmsPanel.setVisible(false);
		log.info("End : init1");
	}
	/**
	 * Given a parameter type of a method, this method will find out
	 * if the parm type is a primitive.  If so, it'll just add the parm type
	 * as a node into the tree model (used to store all parm types of the method).
	 * If however, the parm type is not a primitive, then the parm type will be
	 * further recursed i.e. a reflection of all the fields is obtained
	 * and each of which will be examined to determined if they are primitives.
	 * For those which are primitives, they'll be added to the tree model
	 * and those otherwise will be further recursed.
	 *
	 * @param parmType	the parmType of a method which will be examined
	 * @param parentNode	the node under which the parmType will be added to
	 * @param childIndex	the index of the this parmType if it were to be added
	 *			under the parent node
	 */
	protected void recurseParm(
		Class parmType,
		DefaultMutableTreeNode parentNode,
		int childIndex,
		DefaultTreeModel treeModel)
	{
		log.debug("Start - recurseParm1");
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(parmType);
		treeModel.insertNodeInto(node, parentNode, childIndex);
		log.info("recurseParm1 - parent : " + parentNode);
		log.info("recurseParm1 - parent : " + treeModel.getChildCount(parentNode));
		log.info("recurseParm1 - child : " + treeModel.getChildCount(node));
		if (parmType.isPrimitive())
		{
			// if parmType is primitive then no need to recurse the parm
		}
		else if (parmType.getName().equals(STRING_CLASS))
		{
			// consider String as a primitive i.e. there is not need to recurse parm
		}
		else if (parmType.isArray())
		{
			// if parmType is array then need to handle differently
		}
		else
		{
			// if parmType is NOT primitive then need to recurse the parm since
			// it's an object.
			// to recurse the object, use reflections to get all fields
			Field[] fields = parmType.getFields();
			for (int i = 0; i < fields.length; i++)
			{
				Class fieldClass = fields[i].getType();
				log.debug("recurseParm1 : field #" + i + " - " + fieldClass);
				recurseParm(fieldClass, node, i, treeModel);
			}
		}
		log.debug("End - recurseParm1");
	}
	protected JPanel getMethodHomeNamePanel()
	{
		log.info("Start : getMethodHomeNamePanel1");
		JPanel panel = new JPanel();
		panel.add(new JLabel(JMeterUtils.getResString("jndi_method_home_name")));
		String methodName = model.getMethodHomeName();
		if (methodName != null)
		{
			methodHomeNameBox.setSelectedItem(methodName);
		}
		panel.add(methodHomeNameBox);
		log.info("End : getMethodHomeNamePanel1");
		return panel;
	}
	protected JPanel getRemoteInterfaceListPanel()
	{
		log.info("Start : getRemoteInterfaceListPanel1");
		JPanel panel = new JPanel();
		panel.add(
			new JLabel(
				JMeterUtils.getResString("jndi_method_remote_interface_list")));
		Object remoteInterfaceType = model.getRemoteInterfaceType();
		if (remoteInterfaceType != null)
		{
			remoteInterfaceBox.setSelectedItem(remoteInterfaceType.toString());
		}
		panel.add(remoteInterfaceBox);
		log.info("End : getRemoteInterfaceListPanel1");
		return panel;
	}
	protected JPanel getMethodRemoteNamePanel()
	{
		log.info("Start : getMethodRemoteNamePanel1");
		JPanel panel = new JPanel();
		panel.add(new JLabel(JMeterUtils.getResString("jndi_method_remote_name")));
		String methodName = model.getMethodRemoteName();
		if (methodName != null)
		{
			methodRemoteNameBox.setSelectedItem(methodName);
		}
		panel.add(methodRemoteNameBox);
		log.info("End : getMethodRemoteNamePanel1");
		return panel;
	}
	protected JPanel getMethodHomeParmsPanel()
	{
		log.info("Start : getMethodHomeParmsPanel1");
		JPanel panel = new JPanel();
		panel.add(new JLabel(JMeterUtils.getResString("jndi_method_home_parms")));
		rootHome = new DefaultMutableTreeNode("Root");
		treeModelHome = new DefaultTreeModel(rootHome);
		jTreeHome = new JTree(treeModelHome);
		jTreeHome.getSelectionModel().setSelectionMode(
			TreeSelectionModel.SINGLE_TREE_SELECTION);
		jTreeHome.addTreeSelectionListener(this);
		jTreeHome.setPreferredSize(new Dimension(200, 50));
		JPanel jTreePanel = new JPanel();
		jTreePanel.add(jTreeHome);
		panel.add(jTreePanel);
		// set mouse listener to listen for double clicks
		MouseListener ml = new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				TreePath selPath = jTreeHome.getPathForLocation(e.getX(), e.getY());
				if (e.getClickCount() == 2)
				{
					log.info("Double clicked on - " + selPath.getLastPathComponent());
					DefaultMutableTreeNode node =
						(DefaultMutableTreeNode) selPath.getLastPathComponent();
					int childCount = node.getChildCount();
					// if node is a leaf and has a parent (i.e. not root)
					// then node is a parm which needs a value so pop out
					// dialog for user to fill in
					if (childCount == 0 && node.getParent() != null)
					{
						log.info("Pop!!!");
						Object userObject = node.getUserObject();
						Class type = null;
						if (userObject instanceof Class)
						{
							type = (Class) userObject;
						}
						else if (userObject instanceof MethodConfigUserObject)
						{
							type = (Class) ((MethodConfigUserObject) userObject).getType();
						}
						MethodConfigDialog dialog = new MethodConfigDialog(frame, type);
						dialog.pack();
						dialog.setVisible(true);
						MethodConfigUserObject input = dialog.getValidatedInput();
						log.info("input - " + input);
						if (input != null)
						{
							node.setUserObject(input);
						}
					}
				}
			}
		};
		jTreeHome.addMouseListener(ml);
		log.info("End : getMethodHomeParmsPanel1");
		return panel;
	}
	protected JPanel getMethodRemoteParmsPanel()
	{
		log.info("Start : getMethodRemoteParmsPanel1");
		JPanel panel = new JPanel();
		panel.add(new JLabel(JMeterUtils.getResString("jndi_method_remote_parms")));
		rootRemote = new DefaultMutableTreeNode("Root");
		treeModelRemote = new DefaultTreeModel(rootRemote);
		jTreeRemote = new JTree(treeModelRemote);
		jTreeRemote.getSelectionModel().setSelectionMode(
			TreeSelectionModel.SINGLE_TREE_SELECTION);
		jTreeRemote.addTreeSelectionListener(this);
		jTreeRemote.setPreferredSize(new Dimension(200, 50));
		JPanel jTreePanel = new JPanel();
		jTreePanel.add(jTreeRemote);
		panel.add(jTreePanel);
		// set mouse listener to listen for double clicks
		MouseListener ml = new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				TreePath selPath = jTreeRemote.getPathForLocation(e.getX(), e.getY());
				if (e.getClickCount() == 2)
				{
					log.info("Double clicked on - " + selPath.getLastPathComponent());
					DefaultMutableTreeNode node =
						(DefaultMutableTreeNode) selPath.getLastPathComponent();
					int childCount = node.getChildCount();
					if (childCount == 0)
					{
						log.info("Pop!!!");
						Object userObject = node.getUserObject();
						Class type = null;
						if (userObject instanceof Class)
						{
							type = (Class) userObject;
						}
						else if (userObject instanceof MethodConfigUserObject)
						{
							type = (Class) ((MethodConfigUserObject) userObject).getType();
						}
						MethodConfigDialog dialog = new MethodConfigDialog(frame, type);
						dialog.pack();
						dialog.setVisible(true);
						MethodConfigUserObject input = dialog.getValidatedInput();
						log.info("input - " + input);
						if (input != null)
						{
							node.setUserObject(input);
						}
					}
				}
			}
		};
		jTreeRemote.addMouseListener(ml);
		log.info("End : getMethodHomeParmsPanel1");
		return panel;
	}
	public MethodConfig getModel()
	{
		return model;
	}
	/**
	 * Caller needs to be able to get Method Parms for both the Home Method
	 * and Remote Method.  Based on the state this method will call
	 * <code>getMethodParmsValues</code> witht the appropriate root.  NOTE :
	 * parms for Home and Remote Methods are stored under different roots.
	 */
	public Object[] getMethodParmsValues(int state)
		throws MethodConfigUserObjectException
	{
		Object[] objects = null;
		if (state == MethodConfig.METHOD_INVOKE_HOME)
		{
			objects = getMethodParmsValues(rootHome);
		}
		else if (state == MethodConfig.METHOD_INVOKE_REMOTE)
		{
			objects = getMethodParmsValues(rootRemote);
		}
		return objects;
	}
	public Object[] getMethodParmsValues(DefaultMutableTreeNode root)
		throws MethodConfigUserObjectException
	{
		log.info("Start : getMethodParmsValues1");
		// go through jTree to get all arguments
		int childCount = root.getChildCount();
		Object[] parmsValues = new Object[childCount];
		for (int i = 0; i < childCount; i++)
		{
			parmsValues[i] =
				formObject((DefaultMutableTreeNode) treeModelHome.getChild(root, i));
		}
		log.info("End : getMethodParmsValues1");
		return parmsValues;
	}
	protected Object formObject(DefaultMutableTreeNode node)
		throws MethodConfigUserObjectException
	{
		log.info("Start : formObject1");
		Object obj = node.getUserObject();
		Object returnVal = null;
		if (obj instanceof MethodConfigUserObject)
		{
			// then node contains a primitive so just get the object
			MethodConfigUserObject userObject = (MethodConfigUserObject) obj;
			returnVal = userObject.getObject();
			if (log.isDebugEnabled())
			{
				log.debug("formObject1 : primitive - " + userObject);
			}
		}
		else if (obj instanceof Class)
		{
			// there are cases when the tree node will contain only class
			// and not MethodConfigUserObject -
			// 1. its value has not been input by the user but it's a primitive
			// 2. it's not a primitive but an object
			Class type = (Class) obj;
			if (type.isPrimitive())
			{
				// it's a primitive but the user has not input a value for it
				String errorStr =
					type.getName() + " is a primitive with uninitialized values";
				log.error("formObject1 : " + errorStr);
				throw new MethodConfigUserObjectException(errorStr);
			}
			else
			{
				// then node is an object which contains other primitives
				if (log.isDebugEnabled())
				{
					log.debug("formObject1 : Creating object - " + type);
				}
				int childCount = node.getChildCount();
				Object[] constituents = new Object[childCount];
				for (int i = 0; i < childCount; i++)
				{
					constituents[i] =
						formObject(
							(DefaultMutableTreeNode) treeModelHome.getChild(node, i));
				}
				// get the fields of the class
				// gather all constituents to form object
				Field[] fields = type.getFields();
				try
				{
					for (int i = 0; i < constituents.length; i++)
					{
						log.debug("formObject1 : setting - " + fields[i].getName());
						log.debug(
							"formObject1 : to value - "
								+ constituents[i]
								+ " of - "
								+ constituents[i].getClass());
						returnVal = type.newInstance();
						fields[i].set(returnVal, constituents[i]);
					}
				}
				catch (IllegalAccessException e)
				{
					log.error(e);
					throw new MethodConfigUserObjectException(e.getMessage());
				}
				catch (InstantiationException e)
				{
					log.error(e);
					throw new MethodConfigUserObjectException(e.getMessage());
				}
			}
		}
		log.info("End : formObject1");
		return returnVal;
	}
	/**
	 * Sets up the pull-down menus to contain all the <code>String</code>s
	 * passed in as well as displaying the user's selection.
	 *
	 * @param methodNameBox	the <code>JComboBox</code> to be manipulated
	 * @param methodName		the user's selection
	 * @param strings		the list of <code>String</code>s to be added
	 *				into the <code>JComboBox</code>
	 */
	protected void setupPullDown(
		JComboBox methodNameBox,
		String methodName,
		String[] strings)
	{
		log.info("Start : setupPullDown1");
		// if the list is empty then try to fill it
		if (methodNameBox.getItemCount() == 0)
		{
			if (strings != null)
			{
				for (int i = 0; i < strings.length; i++)
				{
					log.debug("setupPullDown1 :  adding method - " + strings[i]);
					methodNameBox.addItem(strings[i]);
				}
			}
		}
		// change the methodBox selected item only if there's a change in the
		// methodName to avoid triggering actionPerformed each time
		String methodNameBoxSelected = (String) methodNameBox.getSelectedItem();
		if ((methodName != null) && !methodName.equals(methodNameBoxSelected))
		{
			methodNameBox.setSelectedItem(methodName);
		}
		log.info("End : setupPullDown1");
	}
	/**
	 * Resets the home method parms.  This method will be called when the user
	 * makes any changes which requires the resetting the method home parms
	 * e.g. when user decides to call another home method.
	 */
	protected void resetHomeMethodParms()
	{
		log.info("Start : resetHomeMethodParms1");
		// remove all parms of the old method
		int totalChild = rootHome.getChildCount();
		for (int i = 0; i < totalChild; i++)
		{
			// the child to be removed will always be 0 'cos as the child
			// is removed the nth node will become (n-1)th
			treeModelHome.removeNodeFromParent(
				(DefaultMutableTreeNode) rootHome.getChildAt(0));
		}
		rootHome.setUserObject("Root");
		log.info("End : resetHomeMethodParms1");
	}
	/**
	 * Resets the remote interface list returned by the home method.  This method
	 * will be called when the user makes any changes which requires the 
	 * remote interface list to be modified e.g. when user decides to call 
	 * another home method.
	 */
	protected void resetRemoteInterfaceList()
	{
		log.info("Start : resetRemoteInterfaceList1");
		model.setRemoteInterfaceList(null);
		model.setRemoteInterfaceType(null);
		remoteInterfaceBox.removeAllItems();
		log.info("End : resetRemoteInterfaceList1");
	}
	/**
	 * Resets the selected remote interface.  This method will be called when 
	 * the user makes any changes which requires the resetting the selected
	 * remote interface e.g. when user decides to call another home method,
	 * when the user decides to change the remote interface to be executed.
	 */
	protected void resetRemoteMethodName()
	{
		log.info("Start : resetRemoteMethodName1");
		model.setMethodRemoteName(null);
		methodRemoteNameBox.removeAllItems();
		log.info("End : resetRemoteMethodName1");
	}
	/** 
	  * Resets the remote method parms.  This method will be called when the user 
	  * makes any changes which requires the resetting the method remote parms
	  * e.g. when user decides to call another home method, when user changes the
	  * remote interface to be executed, when user changes the remote method
	  * to be run.
	  */
	protected void resetRemoteMethodParms()
	{
		log.info("Start : resetRemoteMethodParms1");
		// remove all parms of the old method
		int totalChild = rootRemote.getChildCount();
		for (int i = 0; i < totalChild; i++)
		{
			// the child to be removed will always be 0 'cos as the child
			// is removed the nth node will become (n-1)th
			treeModelRemote.removeNodeFromParent(
				(DefaultMutableTreeNode) rootRemote.getChildAt(0));
		}
		rootRemote.setUserObject("Root");
		log.info("End : resetRemoteMethodParms1");
	}
	//----- ActionListener interface : start -----
	public void actionPerformed(ActionEvent e)
	{
		String command = e.getActionCommand();
		ReflectionJMeterEngine engine = null;
		GuiPackage guiPackage = null;
		if (log.isDebugEnabled())
		{
			log.debug("actionPerformed1 : command - " + command);
		}
		if (command.equals(REFLECT))
		{
			guiPackage = GuiPackage.getInstance();
			Collection groups = TestPlan.createTestPlan(null).compileTestPlan();
			engine = new ReflectionJMeterEngine();
			for (Iterator i = groups.iterator(); i.hasNext();)
			{
				ThreadGroup tg = (ThreadGroup) i.next();
				if (log.isDebugEnabled())
				{
					log.debug("actionPerformed1 : threadgroup - " + tg);
				}
				engine.addThreadGroup(tg);
			}
			guiPackage.getMainFrame().setRunning(true);
			model.setReflectionStatus(true);
			engine.runTest();
			guiPackage.getMainFrame().setRunning(false);
			model.setReflectionStatus(false);
			updateGui();
		}
		if (command.equals(INVOKE))
		{
		}
	}
	//----- ActionListener interface : end -----
	//----- TreeSelectionListener interface : start -----
	public void valueChanged(TreeSelectionEvent e)
	{
		log.debug("Start : valueChanged1");
		log.debug("End : valueChanged1");
	}
	//----- TreeSelectionListener interface : end -----
}
