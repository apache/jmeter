/*
 * Created on Mar 8, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.apache.jmeter.visualizers;

import java.util.HashMap;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;


import org.apache.jmeter.gui.util.JMeterColor;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;

public class MonitorPerformancePanel extends JSplitPane
	implements TreeSelectionListener, MonitorListener, Clearable
{

	private JScrollPane TREEPANE;
	private JPanel GRAPHPANEL;
	private JTree SERVERTREE;
	private DefaultTreeModel TREEMODEL;
	private MonitorGraph GRAPH;
	private DefaultMutableTreeNode ROOTNODE;
	private HashMap SERVERMAP;
	private MonitorAccumModel MODEL;
	private SampleResult ROOTSAMPLE;
	
	public static final String LEGEND_HEALTH =
		JMeterUtils.getResString("monitor_legend_health");
	public static final String LEGEND_LOAD =
		JMeterUtils.getResString("monitor_legend_load");
	public static final String LEGEND_MEM =
		JMeterUtils.getResString("monitor_legend_memory_per");
	public static final String LEGEND_THREAD =
		JMeterUtils.getResString("monitor_legend_thread_per");
	public static ImageIcon LEGEND_HEALTH_ICON =
		JMeterUtils.getImage("monitor-green-legend.gif");
	public static ImageIcon LEGEND_LOAD_ICON =
		JMeterUtils.getImage("monitor-blue-legend.gif");
	public static ImageIcon LEGEND_MEM_ICON =
		JMeterUtils.getImage("monitor-orange-legend.gif");
	public static ImageIcon LEGEND_THREAD_ICON =
		JMeterUtils.getImage("monitor-red-legend.gif");
	public static final String GRID_LABEL_TOP =
		JMeterUtils.getResString("monitor_label_left_top");
	public static final String GRID_LABEL_MIDDLE =
		JMeterUtils.getResString("monitor_label_left_middle");
	public static final String GRID_LABEL_BOTTOM =
		JMeterUtils.getResString("monitor_label_left_bottom");
	public static final String GRID_LABEL_HEALTHY =
		JMeterUtils.getResString("monitor_label_right_healthy");
	public static final String GRID_LABEL_ACTIVE =
		JMeterUtils.getResString("monitor_label_right_active");
	public static final String GRID_LABEL_WARNING =
		JMeterUtils.getResString("monitor_label_right_warning");
	public static final String GRID_LABEL_DEAD =
		JMeterUtils.getResString("monitor_label_right_dead");
	
	public static final String PERF_TITLE =
		JMeterUtils.getResString("monitor_performance_title");
	public static final String SERVER_TITLE =
		JMeterUtils.getResString("monitor_performance_servers");

	protected Font plaintext = new Font("plain", Font.TRUETYPE_FONT, 10);

    /**
     * 
     */
    public MonitorPerformancePanel(MonitorAccumModel model, MonitorGraph graph)
    {
        super();
        this.SERVERMAP = new HashMap();
        this.MODEL = model;
        this.MODEL.addListener(this);
        this.GRAPH = graph;
        init();
    }

	public void init(){
		ROOTSAMPLE = new SampleResult();
		ROOTSAMPLE.setSampleLabel(SERVER_TITLE);
		ROOTSAMPLE.setSuccessful(true);
		ROOTNODE = new DefaultMutableTreeNode(ROOTSAMPLE);
		TREEMODEL = new DefaultTreeModel(ROOTNODE);
		SERVERTREE = new JTree(TREEMODEL);
		SERVERTREE.getSelectionModel().setSelectionMode(
			TreeSelectionModel.SINGLE_TREE_SELECTION);
		SERVERTREE.addTreeSelectionListener(this);
		SERVERTREE.setShowsRootHandles(true);
		TREEPANE = new JScrollPane(SERVERTREE);
		TREEPANE.setPreferredSize(new Dimension(150,200));
		this.add(TREEPANE,JSplitPane.LEFT);
		this.setDividerLocation(0.18);
	
		JPanel right = new JPanel();
		right.setLayout(new BorderLayout());
		JLabel title = new JLabel(" " + PERF_TITLE);
		title.setPreferredSize(new Dimension(200,40));
		GRAPHPANEL = new JPanel();
		GRAPHPANEL.setLayout(new BorderLayout());
		GRAPHPANEL.setMaximumSize(
			new Dimension(MODEL.getBufferSize(),MODEL.getBufferSize()));
		GRAPHPANEL.setBackground(JMeterColor.WHITE);
		GRAPHPANEL.add(GRAPH,BorderLayout.CENTER);
		right.add(GRAPHPANEL,BorderLayout.CENTER);
		
		right.add(title,BorderLayout.NORTH);
		right.add(createLegend(),BorderLayout.SOUTH);
		right.add(createLeftGridLabels(),BorderLayout.WEST);
		right.add(createRightGridLabels(),BorderLayout.EAST);
		this.add(right,JSplitPane.RIGHT);		
	}
	
	public JPanel createLegend(){
		Dimension lsize = new Dimension(130,18);
		
		JPanel legend = new JPanel();
		legend.setLayout(new FlowLayout());
		JLabel health = new JLabel(LEGEND_HEALTH);
		health.setFont(plaintext);
		health.setPreferredSize(lsize);
		health.setIcon(LEGEND_HEALTH_ICON);
		legend.add(health);
		
		JLabel load = new JLabel(LEGEND_LOAD);
		load.setFont(plaintext);
		load.setPreferredSize(lsize);
		load.setIcon(LEGEND_LOAD_ICON);
		legend.add(load);
		
		JLabel mem = new JLabel(LEGEND_MEM);
		mem.setFont(plaintext);
		mem.setPreferredSize(lsize);
		mem.setIcon(LEGEND_MEM_ICON);
		legend.add(mem);
		
		JLabel thd = new JLabel(LEGEND_THREAD);
		thd.setFont(plaintext);
		thd.setPreferredSize(lsize);
		thd.setIcon(LEGEND_THREAD_ICON);
		legend.add(thd);
		return legend;
	}
	
	public JPanel createLeftGridLabels(){
		Dimension lsize = new Dimension(33,20);
		JPanel labels = new JPanel();
		labels.setLayout(new BorderLayout());
		
		JLabel top = new JLabel(" " + GRID_LABEL_TOP);
		top.setFont(plaintext);
		top.setPreferredSize(lsize);
		labels.add(top,BorderLayout.NORTH);
		
		JLabel mid = new JLabel(" " + GRID_LABEL_MIDDLE);
		mid.setFont(plaintext);
		mid.setPreferredSize(lsize);
		labels.add(mid,BorderLayout.CENTER);
		
		JLabel bottom = new JLabel(" " + GRID_LABEL_BOTTOM);
		bottom.setFont(plaintext);
		bottom.setPreferredSize(lsize);
		labels.add(bottom,BorderLayout.SOUTH);
		return labels;
	}
	
	public JPanel createRightGridLabels(){
		JPanel labels = new JPanel();
		labels.setLayout(new BorderLayout());
		labels.setPreferredSize(new Dimension(40,GRAPHPANEL.getWidth() - 100));
		Dimension lsize = new Dimension(40,20);
		JLabel h = new JLabel(GRID_LABEL_HEALTHY);
		h.setFont(plaintext);
		h.setPreferredSize(lsize);
		labels.add(h,BorderLayout.NORTH);
		
		JLabel d = new JLabel(GRID_LABEL_DEAD);
		d.setFont(plaintext);
		d.setPreferredSize(lsize);
		labels.add(d,BorderLayout.SOUTH);
		return labels;
	}
	
	public void addSample(MonitorModel model){
		if (!SERVERMAP.containsKey(model.getURL())){
			DefaultMutableTreeNode newnode =
				new DefaultMutableTreeNode(model);
			newnode.setAllowsChildren(false);
			SERVERMAP.put(model.getURL(),newnode);
			ROOTNODE.add(newnode);
			TREEPANE.updateUI();
		}
		DefaultMutableTreeNode node =
			(DefaultMutableTreeNode) SERVERTREE.getLastSelectedPathComponent();
		Object usrobj = node.getUserObject();
		if (usrobj instanceof MonitorModel){
			GRAPH.updateGui((MonitorModel)usrobj);
		}
	}
	
	/**
	 * When the user selects a different node in the
	 * tree, we get the selected node. From the node,
	 * we get the UserObject used to create the
	 * treenode in the constructor.
	 */
	public void valueChanged(TreeSelectionEvent e)
	{
		DefaultMutableTreeNode node =
			(DefaultMutableTreeNode) SERVERTREE.getLastSelectedPathComponent();
		Object usrobj = node.getUserObject();
		if (usrobj instanceof MonitorModel && usrobj != null){
			MonitorModel mo = (MonitorModel)usrobj;
			GRAPH.updateGui(mo);
			this.updateUI();
		}
		TREEPANE.updateUI();
	}
	
	public void clear(){
		this.SERVERMAP.clear();
		ROOTNODE.removeAllChildren();
		SERVERTREE.updateUI();
		GRAPH.clear();
	}
}
