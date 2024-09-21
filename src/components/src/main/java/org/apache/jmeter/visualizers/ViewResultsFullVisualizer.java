/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.visualizers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.JMeter;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.gui.TestElementMetadata;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.apache.jorphan.gui.JMeterUIDefaults;
import org.apache.jorphan.reflect.LogAndIgnoreServiceLoadExceptionHandler;
import org.apache.jorphan.util.StringWrap;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base for ViewResults
 */
@GUIMenuSortOrder(1)
@TestElementMetadata(labelResource = "view_results_tree_title")
public class ViewResultsFullVisualizer extends AbstractVisualizer
implements ActionListener, TreeSelectionListener, Clearable, ItemListener {

    private static final long serialVersionUID = 2L;

    private static final Logger log = LoggerFactory.getLogger(ViewResultsFullVisualizer.class);

    public static final Color SERVER_ERROR_COLOR = Color.red;
    public static final Color CLIENT_ERROR_COLOR = Color.blue;
    public static final Color REDIRECT_COLOR = Color.green;

    protected static final String COMBO_CHANGE_COMMAND = "change_combo"; // $NON-NLS-1$

    private static final Border RED_BORDER = BorderFactory.createLineBorder(Color.red);
    private static final Border BLUE_BORDER = BorderFactory.createLineBorder(Color.blue);
    private static final String ICON_SIZE = JMeterUtils.getPropDefault(JMeter.TREE_ICON_SIZE, JMeter.DEFAULT_TREE_ICON_SIZE);

    // Default limited to 10 megabytes
    private static final int MAX_DISPLAY_SIZE =
            JMeterUtils.getPropDefault("view.results.tree.max_size", 10485760); // $NON-NLS-1$

    // Default limited to 110K
    private static final int MAX_LINE_SIZE =
            JMeterUtils.getPropDefault("view.results.tree.max_line_size", 110000); // $NON-NLS-1$

    // Limit the soft wrap to 100K (hard limit divided by 1.1)
    private static final int SOFT_WRAP_LINE_SIZE =
            JMeterUtils.getPropDefault("view.results.tree.soft_wrap_line_size", (int) (MAX_LINE_SIZE / 1.1f)); // $NON-NLS-1$

    // default display order
    private static final String VIEWERS_ORDER =
            JMeterUtils.getPropDefault("view.results.tree.renderers_order", ""); // $NON-NLS-1$ //$NON-NLS-2$

    private static final int REFRESH_PERIOD = JMeterUtils.getPropDefault("jmeter.gui.refresh_period", 500);

    private static final ImageIcon imageSuccess = JMeterUtils.getImage(
            JMeterUtils.getPropDefault("viewResultsTree.success",  //$NON-NLS-1$
                    "vrt/" + ICON_SIZE + "/security-high-2.png")); //$NON-NLS-1$ $NON-NLS-2$

    private static final ImageIcon imageFailure = JMeterUtils.getImage(
            JMeterUtils.getPropDefault("viewResultsTree.failure",  //$NON-NLS-1$
                    "vrt/" + ICON_SIZE + "/security-low-2.png")); //$NON-NLS-1$ $NON-NLS-2$

    private JSplitPane mainSplit;
    private DefaultMutableTreeNode root;
    private DefaultTreeModel treeModel;
    private JTree jTree;
    private Component leftSide;
    private JTabbedPane rightSide;
    private JComboBox<ResultRenderer> selectRenderPanel;
    private int selectedTab;
    private ResultRenderer resultsRender = null;
    private Object resultsObject = null;
    private TreeSelectionEvent lastSelectionEvent;
    private JCheckBox autoScrollCB;
    private final Queue<SampleResult> buffer;
    private boolean dataChanged;

    /**
     * Constructor
     */
    public ViewResultsFullVisualizer() {
        super();
        final int maxResults = JMeterUtils.getPropDefault("view.results.tree.max_results", 500);
        if (maxResults > 0) {
            buffer = new CircularFifoQueue<>(maxResults);
        } else {
            buffer = new ArrayDeque<>();
        }
        init();
        new Timer(REFRESH_PERIOD, e -> updateGui()).start();
    }

    /** {@inheritDoc} */
    @Override
    public void add(final SampleResult sample) {
        synchronized (buffer) {
            buffer.add(sample);
            dataChanged = true;
        }
    }

    /**
     * Update the visualizer with new data.
     */
    private void updateGui() {
        TreePath selectedPath = null;
        Object oldSelectedElement;
        Set<Object> oldExpandedElements;
        Set<TreePath> newExpandedPaths = new HashSet<>();
        synchronized (buffer) {
            if (!dataChanged) {
                return;
            }

            final Enumeration<TreePath> expandedElements = jTree.getExpandedDescendants(new TreePath(root));
            oldExpandedElements = extractExpandedObjects(expandedElements);
            oldSelectedElement = getSelectedObject();
            root.removeAllChildren();
            for (SampleResult sampler: buffer) {
                SampleResult res = sampler;
                // Add sample
                DefaultMutableTreeNode currNode = new SearchableTreeNode(res, treeModel);
                treeModel.insertNodeInto(currNode, root, root.getChildCount());
                List<TreeNode> path = new ArrayList<>(Arrays.asList(root, currNode));
                selectedPath = checkExpandedOrSelected(path,
                        res, oldSelectedElement,
                        oldExpandedElements, newExpandedPaths, selectedPath);
                TreePath potentialSelection = addSubResults(currNode, res, path, oldSelectedElement, oldExpandedElements, newExpandedPaths);
                if (potentialSelection != null) {
                    selectedPath = potentialSelection;
                }
                // Add any assertion that failed as children of the sample node
                AssertionResult[] assertionResults = res.getAssertionResults();
                int assertionIndex = currNode.getChildCount();
                for (AssertionResult assertionResult : assertionResults) {
                    if (assertionResult.isFailure() || assertionResult.isError()) {
                        DefaultMutableTreeNode assertionNode = new SearchableTreeNode(assertionResult, treeModel);
                        treeModel.insertNodeInto(assertionNode, currNode, assertionIndex++);
                        selectedPath = checkExpandedOrSelected(path,
                                assertionResult, oldSelectedElement,
                                oldExpandedElements, newExpandedPaths, selectedPath,
                                assertionNode);
                    }
                }
            }
            treeModel.nodeStructureChanged(root);
            dataChanged = false;
        }

        if (root.getChildCount() == 1) {
            jTree.expandPath(new TreePath(root));
        }
        newExpandedPaths.stream().forEach(jTree::expandPath);
        if (selectedPath != null) {
            jTree.setSelectionPath(selectedPath);
        }
        if (autoScrollCB.isSelected() && root.getChildCount() > 1) {
            jTree.scrollPathToVisible(new TreePath(new Object[] { root,
                    treeModel.getChild(root, root.getChildCount() - 1) }));
        }
    }

    private Object getSelectedObject() {
        Object oldSelectedElement;
        DefaultMutableTreeNode oldSelectedNode = (DefaultMutableTreeNode) jTree.getLastSelectedPathComponent();
        oldSelectedElement = oldSelectedNode == null ? null : oldSelectedNode.getUserObject();
        return oldSelectedElement;
    }

    private static TreePath checkExpandedOrSelected(List<TreeNode> path,
            Object item, Object oldSelectedObject,
            Set<Object> oldExpandedObjects, Set<? super TreePath> newExpandedPaths,
            TreePath defaultPath) {
        TreePath result = defaultPath;
        if (oldSelectedObject == item) {
            result = toTreePath(path);
        }
        if (oldExpandedObjects.contains(item)) {
            newExpandedPaths.add(toTreePath(path));
        }
        return result;
    }

    private static TreePath checkExpandedOrSelected(List<TreeNode> path,
            Object item, Object oldSelectedObject,
            Set<Object> oldExpandedObjects, Set<? super TreePath> newExpandedPaths,
            TreePath defaultPath, DefaultMutableTreeNode extensionNode) {
        TreePath result = defaultPath;
        if (oldSelectedObject == item) {
            result = toTreePath(path, extensionNode);
        }
        if (oldExpandedObjects.contains(item)) {
            newExpandedPaths.add(toTreePath(path, extensionNode));
        }
        return result;
    }

    private static Set<Object> extractExpandedObjects(final Enumeration<TreePath> expandedElements) {
        if (expandedElements != null) {
            final List<TreePath> list = Collections.list(expandedElements);
            log.debug("Expanded: {}", list);
            Set<Object> result = list.stream()
                    .map(TreePath::getLastPathComponent)
                    .map(c -> (DefaultMutableTreeNode) c)
                    .map(DefaultMutableTreeNode::getUserObject)
                    .collect(Collectors.toSet());
            log.debug("Elements: {}", result);
            return result;
        }
        return Collections.emptySet();
    }

    private TreePath addSubResults(DefaultMutableTreeNode currNode,
            SampleResult res, List<TreeNode> path, Object selectedObject,
            Set<Object> oldExpandedObjects, Set<? super TreePath> newExpandedPaths) {
        SampleResult[] subResults = res.getSubResults();

        int leafIndex = 0;
        TreePath result = null;

        for (SampleResult child : subResults) {
            log.debug("updateGui1 : child sample result - {}", child);
            DefaultMutableTreeNode leafNode = new SearchableTreeNode(child, treeModel);

            treeModel.insertNodeInto(leafNode, currNode, leafIndex++);
            List<TreeNode> newPath = new ArrayList<>(path);
            newPath.add(leafNode);
            result = checkExpandedOrSelected(newPath, child, selectedObject, oldExpandedObjects, newExpandedPaths, result);
            addSubResults(leafNode, child, newPath, selectedObject, oldExpandedObjects, newExpandedPaths);
            // Add any assertion that failed as children of the sample node
            AssertionResult[] assertionResults = child.getAssertionResults();
            int assertionIndex = leafNode.getChildCount();
            for (AssertionResult item : assertionResults) {
                if (item.isFailure() || item.isError()) {
                    DefaultMutableTreeNode assertionNode = new SearchableTreeNode(item, treeModel);
                    treeModel.insertNodeInto(assertionNode, leafNode, assertionIndex++);
                    result = checkExpandedOrSelected(path, item,
                            selectedObject, oldExpandedObjects, newExpandedPaths, result,
                            assertionNode);
                }
            }
        }
        return result;
    }

    private static TreePath toTreePath(List<TreeNode> newPath) {
        return new TreePath(newPath.toArray(new TreeNode[newPath.size()]));
    }

    private static TreePath toTreePath(List<TreeNode> path,
            DefaultMutableTreeNode extensionNode) {
        TreeNode[] result = path.toArray(new TreeNode[path.size() + 1]);
        result[result.length - 1] = extensionNode;
        return new TreePath(result);
    }

    /** {@inheritDoc} */
    @Override
    public void clearData() {
        synchronized (buffer) {
            buffer.clear();
            dataChanged = true;
        }
        resultsRender.clearData();
        resultsObject = null;
    }

    /** {@inheritDoc} */
    @Override
    public String getLabelResource() {
        return "view_results_tree_title"; // $NON-NLS-1$
    }

    /**
     * Initialize this visualizer
     */
    private void init() {  // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        log.debug("init() - pass");
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        leftSide = createLeftPanel();
        // Prepare the common tab
        rightSide = new JTabbedPane();

        // Create the split pane
        mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSide, rightSide);
        mainSplit.setOneTouchExpandable(true);

        JSplitPane searchAndMainSP = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new SearchTreePanel(root), mainSplit);
        searchAndMainSP.setOneTouchExpandable(true);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, makeTitlePanel(), searchAndMainSP);
        splitPane.setOneTouchExpandable(true);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        add(splitPane);

        // init right side with first render
        resultsRender.setRightSide(rightSide);
        resultsRender.init();
    }

    /** {@inheritDoc} */
    @Override
    public void valueChanged(TreeSelectionEvent e) {
        valueChanged(e, false);
    }

    /**
     * @param e {@link TreeSelectionEvent}
     * @param forceRendering boolean
     */
    private void valueChanged(TreeSelectionEvent e, boolean forceRendering) {
        lastSelectionEvent = e;
        DefaultMutableTreeNode node;
        synchronized (this) {
            node = (DefaultMutableTreeNode) jTree.getLastSelectedPathComponent();
        }

        if (node != null && (forceRendering || node.getUserObject() != resultsObject)) {
            resultsObject = node.getUserObject();
            // to restore last tab used
            if (rightSide.getTabCount() > selectedTab) {
                resultsRender.setLastSelectedTab(rightSide.getSelectedIndex());
            }
            Object userObject = node.getUserObject();
            resultsRender.setSamplerResult(userObject);
            resultsRender.setupTabPane(); // Processes Assertions
            // display a SampleResult
            if (userObject instanceof SampleResult) {
                SampleResult sampleResult = (SampleResult) userObject;
                if (isTextDataType(sampleResult)){
                    resultsRender.renderResult(sampleResult);
                } else {
                    byte[] responseBytes = sampleResult.getResponseData();
                    if (responseBytes != null) {
                        resultsRender.renderImage(sampleResult);
                    }
                }
            }
        }
    }

    /**
     * @param sampleResult SampleResult
     * @return true if sampleResult is text or has empty content type
     */
    protected static boolean isTextDataType(SampleResult sampleResult) {
        return SampleResult.TEXT.equals(sampleResult.getDataType())
                || StringUtils.isEmpty(sampleResult.getDataType());
    }

    private synchronized Component createLeftPanel() {
        SampleResult rootSampleResult = new SampleResult();
        rootSampleResult.setSampleLabel("Root");
        rootSampleResult.setSuccessful(true);
        root = new SearchableTreeNode(rootSampleResult, null);

        treeModel = new DefaultTreeModel(root);
        jTree = new JTree(treeModel);
        jTree.setCellRenderer(new ResultsNodeRenderer());
        jTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        jTree.addTreeSelectionListener(this);
        jTree.setRootVisible(false);
        jTree.setShowsRootHandles(true);
        JScrollPane treePane = new JScrollPane(jTree);
        treePane.setPreferredSize(new Dimension(200, 300));

        VerticalPanel leftPane = new VerticalPanel();
        leftPane.add(treePane, BorderLayout.CENTER);
        leftPane.add(createComboRender(), BorderLayout.NORTH);
        autoScrollCB = new JCheckBox(JMeterUtils.getResString("view_results_autoscroll")); // $NON-NLS-1$
        autoScrollCB.setSelected(false);
        autoScrollCB.addItemListener(this);
        leftPane.add(autoScrollCB, BorderLayout.SOUTH);
        return leftPane;
    }

    /**
     * Create the drop-down list to changer render
     * @return List of all render (implement ResultsRender)
     */
    private Component createComboRender() {
        ComboBoxModel<ResultRenderer> nodesModel = new DefaultComboBoxModel<>();
        // drop-down list for renderer
        selectRenderPanel = new JComboBox<>(nodesModel);
        selectRenderPanel.setActionCommand(COMBO_CHANGE_COMMAND);
        selectRenderPanel.addActionListener(this);

        // if no results render in jmeter.properties, load Standard (default)
        String defaultRenderer = expandToClassname(".RenderAsText"); // $NON-NLS-1$
        if (VIEWERS_ORDER.length() > 0) {
            defaultRenderer = expandToClassname(VIEWERS_ORDER.split(",", 2)[0]);
        }
        ResultRenderer defaultObject = null;
        Map<String, ResultRenderer> map = new HashMap<>();
        for (ResultRenderer renderer : JMeterUtils.loadServicesAndScanJars(
                ResultRenderer.class,
                ServiceLoader.load(ResultRenderer.class),
                Thread.currentThread().getContextClassLoader(),
                new LogAndIgnoreServiceLoadExceptionHandler(log)
        )) {
            // Instantiate render classes
            if (defaultRenderer.equals(renderer.getClass().getName())) {
                defaultObject = renderer;
            }
            renderer.setBackgroundColor(getBackground());
            map.put(renderer.getClass().getName(), renderer);
        }
        if (VIEWERS_ORDER.length() > 0) {
            Arrays.stream(VIEWERS_ORDER.split(","))
                    .map(ViewResultsFullVisualizer::expandToClassname)
                    .forEach(key -> {
                        ResultRenderer renderer = map.remove(key);
                        if (renderer != null) {
                            selectRenderPanel.addItem(renderer);
                        } else {
                            log.warn(
                                    "Missing (check renderer name) or already added (check doublon) result renderer," +
                                            " check property 'view.results.tree.renderers_order', renderer name: '{}'",
                                    key);
                        }
                    });
        }
        // Add remaining (plugins or missed in property)
        map.values().forEach(renderer -> selectRenderPanel.addItem(renderer));
        nodesModel.setSelectedItem(defaultObject); // preset to "Text" option or the first option from the view.results.tree.renderers_order property
        return selectRenderPanel;
    }

    private static String expandToClassname(String name) {
        if (name.startsWith(".")) {
            return "org.apache.jmeter.visualizers" + name; // $NON-NLS-1$
        }
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        if (COMBO_CHANGE_COMMAND.equals(command)) {
            JComboBox<?> jcb = (JComboBox<?>) event.getSource();

            if (jcb != null) {
                resultsRender = (ResultRenderer) jcb.getSelectedItem();
                if (rightSide != null) {
                    // to restore last selected tab (better user-friendly)
                    selectedTab = rightSide.getSelectedIndex();
                    // Remove old right side and keep the position of the divider
                    int dividerLocation = mainSplit.getDividerLocation();
                    mainSplit.remove(rightSide);

                    // create and add a new right side at the old position
                    rightSide = new JTabbedPane();
                    mainSplit.add(rightSide);
                    mainSplit.setDividerLocation(dividerLocation);
                    resultsRender.setRightSide(rightSide);
                    resultsRender.setLastSelectedTab(selectedTab);
                    log.debug("selectedTab={}", selectedTab);
                    resultsRender.init();
                    // To display current sampler result before change
                    this.valueChanged(lastSelectionEvent, true);
                }
            }
        }
    }

    public static String getResponseAsString(SampleResult res) {
        String response = null;
        if (isTextDataType(res)) {
            // Showing large strings can be VERY costly, so we will avoid
            // doing so if the response
            // data is larger than 200K. TODO: instead, we could delay doing
            // the result.setText
            // call until the user chooses the "Response data" tab. Plus we
            // could warn the user
            // if this happens and revert the choice if they doesn't confirm
            // they are ready to wait.
            int len = res.getResponseDataAsString().length();
            if (MAX_DISPLAY_SIZE > 0 && len > MAX_DISPLAY_SIZE) {
                StringBuilder builder = new StringBuilder(MAX_DISPLAY_SIZE + 100);
                builder.append(JMeterUtils.getResString("view_results_response_too_large_message")) //$NON-NLS-1$
                    .append(len).append(" > Max: ").append(MAX_DISPLAY_SIZE)
                    .append(", ").append(JMeterUtils.getResString("view_results_response_partial_message")) // $NON-NLS-1$
                    .append("\n").append(res.getResponseDataAsString(), 0, MAX_DISPLAY_SIZE).append("\n...");
                response = builder.toString();
            } else {
                response = res.getResponseDataAsString();
            }
        }
        return response;
    }

    @API(status = API.Status.INTERNAL, since = "5.5")
    public static String wrapLongLines(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        if (SOFT_WRAP_LINE_SIZE > 0 && MAX_LINE_SIZE > 0) {
            StringWrap stringWrap = new StringWrap(SOFT_WRAP_LINE_SIZE, MAX_LINE_SIZE);
            return stringWrap.wrap(input, "\n");
        }
        return input;
    }


    private static class ResultsNodeRenderer extends DefaultTreeCellRenderer {
        private static final long serialVersionUID = 4159626601097711565L;

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean sel, boolean expanded, boolean leaf, int row, boolean focus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, focus);
            boolean failure = true;
            Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            if (userObject instanceof SampleResult) {
                failure = !((SampleResult) userObject).isSuccessful();
            } else if (userObject instanceof AssertionResult) {
                AssertionResult assertion = (AssertionResult) userObject;
                failure = assertion.isError() || assertion.isFailure();
            }

            // Set the status for the node
            if (failure) {
                this.setForeground(UIManager.getColor(JMeterUIDefaults.LABEL_ERROR_FOREGROUND));
                this.setIcon(imageFailure);
            } else {
                this.setIcon(imageSuccess);
            }

            // Handle search related rendering
            SearchableTreeNode node = (SearchableTreeNode) value;
            if(node.isNodeHasMatched()) {
                setBorder(RED_BORDER);
            } else if (node.isChildrenNodesHaveMatched()) {
                setBorder(BLUE_BORDER);
            } else {
                setBorder(null);
            }
            return this;
        }
    }

    /**
     * Handler for Checkbox
     */
    @Override
    public void itemStateChanged(ItemEvent e) {
        // NOOP state is held by component
    }
}
