/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002,2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.jmeter.visualizers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/****************************************
 * Allows the tester to view the textual response from sampling an Entry. This
 * also allows to "single step through" the sampling process via a nice
 * "Continue" button.
 *
 *@author    Khor Soon Hin
 *@created   2001/07/25
 *@version   $Revision$ $Date$
 ***************************************/
public class ViewResultsFullVisualizer extends AbstractVisualizer implements ActionListener, TreeSelectionListener, Clearable
{
    transient private static Logger log =
        Hierarchy.getDefaultHierarchy().getLoggerFor("jmeter.gui");

    public final static Color SERVER_ERROR_COLOR = Color.red;
    public final static Color CLIENT_ERROR_COLOR = Color.blue;
    public final static Color REDIRECT_COLOR = Color.green;
    
    private static final String HTML_BUTTON_LABEL = "Render HTML";
    private static final String TEXT_BUTTON_LABEL = "Show Text";
    
    private static final String HTML_COMMAND = "html";
    private static final String TEXT_COMMAND = "text";
    private boolean textMode = true;
    
    private DefaultMutableTreeNode root;
    private DefaultTreeModel treeModel;

    private JTextArea stats;
    private JEditorPane results;
    private JTextArea sampleDataField;

    private JTree jTree;

    public ViewResultsFullVisualizer()
    {
        super();
        log.debug("Start : ViewResultsFullVisualizer1");
        init();
        log.debug("End : ViewResultsFullVisualizer1");
    }

    public void add(SampleResult res)
    {
        updateGui(res);
    }

    public String getStaticLabel()
    {
        return JMeterUtils.getResString("view_results_tree_title");
    }

    /****************************************
     * Update the visualizer with new data
     ***************************************/
    public void updateGui(SampleResult res)
    {
        log.debug("Start : updateGui1");
        if (log.isDebugEnabled())
            log.debug("updateGui1 : sample result - " + res);
        DefaultMutableTreeNode currNode = new DefaultMutableTreeNode(res);

        treeModel.insertNodeInto(currNode, root, root.getChildCount());
        addSubResults(currNode, res);
        log.debug("End : updateGui1");
    }

    private void addSubResults(DefaultMutableTreeNode currNode, SampleResult res)
    {
        SampleResult[] subResults = res.getSubResults();

        if (subResults != null)
        {
            int leafIndex = 0;

            for (int i = 0; i < subResults.length; i++)
            {
                SampleResult child = subResults[i];

                if (log.isDebugEnabled())
                    log.debug("updateGui1 : child sample result - " + child);
                DefaultMutableTreeNode leafNode = new DefaultMutableTreeNode(child);

                treeModel.insertNodeInto(leafNode, currNode, leafIndex++);
                addSubResults(leafNode, child);
            }
        }
    }

    /****************************************
     * Clears the visualizer
     ***************************************/
    public void clear()
    {
        log.debug("Start : clear1");
        int totalChild = root.getChildCount();

        if (log.isDebugEnabled())
            log.debug("clear1 : total child - " + totalChild);
        for (int i = 0; i < totalChild; i++)
        {
            // the child to be removed will always be 0 'cos as the nodes are
            // removed the nth node will become (n-1)th
            treeModel.removeNodeFromParent((DefaultMutableTreeNode) root.getChildAt(0));
        }

        results.setText("");
        log.debug("End : clear1");
    }

    /****************************************
     * Returns the description of this visualizer
     *
     *@return   description of this visualizer
     ***************************************/
    public String toString()
    {
        String desc = "Shows the text results of sampling in tree form";

        if (log.isDebugEnabled())
            log.debug("toString1 : Returning description - " + desc);
        return desc;
    }

    /****************************************
     * Sets the bottom pane to correspond to the selected node of the top tree
     *
     *@param e  !ToDo (Parameter description)
     ***************************************/
    public void valueChanged(TreeSelectionEvent e)
    {
        log.debug("Start : valueChanged1");
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) jTree.getLastSelectedPathComponent();

        if (log.isDebugEnabled())
        {
            log.debug("valueChanged : selected node - " + node);
        }

        stats.setText("");
        if (node != null)
        {
            SampleResult res = (SampleResult) node.getUserObject();

            if (log.isDebugEnabled())
            {
                log.debug("valueChanged1 : sample result - " + res);
            }

            if (res != null)
            {
                // load time label

                log.debug("valueChanged1 : load time - " + res.getTime());
                if (res != null && res.getSamplerData() != null)
                {
                    sampleDataField.setText(res.getSamplerData().trim());
                }

                stats.append("Load time: " + res.getTime() + "\n");

                String responseCode = res.getResponseCode();
                log.debug("valueChanged1 : response code - " + responseCode);

                int responseLevel = 0;
                if (responseCode != null) {
                    try
                    {
                        responseLevel = Integer.parseInt(responseCode) / 100;
                    }
                    catch (NumberFormatException numberFormatException)
                    {
                        // no need to change the foreground color
                    }
                }
                
                switch (responseLevel)
                {
                    case 3 :
                        // responseCodeLabel.setForeground(REDIRECT_COLOR);
                        break;
                    case 4 :
                        // responseCodeLabel.setForeground(CLIENT_ERROR_COLOR);
                        break;
                    case 5 :
                        // responseCodeLabel.setForeground(SERVER_ERROR_COLOR);
                        break;
                }
                stats.append("HTTP response code: " + responseCode + "\n");
                // responseCodeLabel.setText(JMeterUtils.getResString("HTTP response code") + " : " + responseCode);

                // response message label
                String responseMsgStr = res.getResponseMessage();

                log.debug("valueChanged1 : response message - " + responseMsgStr);
                stats.append("HTTP response message: " + responseMsgStr);
                // responseMsgLabel.setText("HTTP response message : " + responseMsgStr);

                // get the text response and image icon
                // to determine which is NOT null
                byte[] responseBytes = (byte[]) res.getResponseData();
                ImageIcon icon = null;
                String response = null;
                if (res.getDataType() != null && res.getDataType().equals(SampleResult.TEXT))
                {
                    try
                    {
                        response = new String(responseBytes, "utf-8");
                    }
                    catch (UnsupportedEncodingException err)
                    {
                        response = new String(responseBytes);
                    }
                }
                else if (responseBytes != null)
                {
                    icon = new ImageIcon(responseBytes);
                }
                if (response != null)
                {
                    if (textMode) {
                        showTextResponse(response);
                    } else {
                        showRenderedResponse(response);
                    }
                }
                /*                else if (icon != null)
                                {
                                    JLabel image = new JLabel();
                
                                    image.setIcon(icon);
                                    showImage(image);
                                }
                */
                //                treeSplitPane.revalidate();
                //                treeSplitPane.repaint();
            }
        }
        log.debug("End : valueChanged1");
    }
    /*
        protected void initTextArea()
        {
            textArea = new JTextArea();
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setTabSize(4);
            textArea.setRows(4);
        }
        
        protected void showImage(JLabel image)
        {
            textScrollArea.setViewportView(image);
        }
    */
    protected void showTextResponse(String response)
    {
        //        textScrollArea.setViewportView(textArea);
        results.setContentType("text/plain");
        results.setText(response == null ? "" : response);
        results.setCaretPosition(0);
    }

    /**********************************************************************
     * Display the response as text or as rendered HTML.  Change the
     * text on the button appropriate to the current display.
     * @param e the ActionEvent being processed
     *********************************************************************/

    public void actionPerformed(ActionEvent e)
    {
        String command = e.getActionCommand();
        
        if (command != null
            && command.equals(TEXT_COMMAND)
            || command.equals(HTML_COMMAND)) {

            // Switch to the other mode
            textMode = !textMode;
                            
            DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) jTree.getLastSelectedPathComponent();

            if (node == null) {
                results.setText("");
                return;
            }

            SampleResult res = (SampleResult) node.getUserObject();
            byte[] responseBytes = (byte[]) res.getResponseData();
            String response = null;

            if (res.getDataType() != null
                && res.getDataType().equals(SampleResult.TEXT)) {
                try {
                    response = new String(responseBytes, "utf-8");
                } catch (UnsupportedEncodingException err) {
                    response = new String(responseBytes);
                }
            }

            if (textMode) {
                showTextResponse(response);
            } else {
                showRenderedResponse(response);
            }
        }
    }

    /*
        protected void initHtmlEditPane()
        {
            htmlEditPane = new JEditorPane();
            HTMLEditorKit htmlKit = new HTMLEditorKit();
    
            htmlEditPane.setEditorKit(htmlKit);
        }
    */
    protected void showRenderedResponse(String response)
    {
        if (response == null) {
            results.setText("");
            return;
        }
        
        int htmlIndex = response.indexOf("<HTML>");

        // Look for a case variation
        if (htmlIndex < 0)
        {
            htmlIndex = response.indexOf("<html>");
        }

        // If we still can't find it, just try using all of the text
        if (htmlIndex < 0) {
            htmlIndex = 0;
        }
        
        String html = response.substring(htmlIndex);
        results.setContentType("text/html");
        results.setText(html);
        results.setCaretPosition(0);
    }

    protected Component createHtmlOrTextPane()
    {
        ButtonGroup group = new ButtonGroup();

        JRadioButton textButton = new JRadioButton(TEXT_BUTTON_LABEL);
        textButton.setActionCommand(TEXT_COMMAND);
        textButton.addActionListener(this);
        textButton.setSelected(textMode);
        group.add(textButton);
        
        JRadioButton htmlButton = new JRadioButton(HTML_BUTTON_LABEL);
        htmlButton.setActionCommand(HTML_COMMAND);
        htmlButton.addActionListener(this);
        htmlButton.setSelected(!textMode);
        group.add(htmlButton);
        
        JPanel pane = new JPanel();
        pane.add(textButton);
        pane.add(htmlButton);
        return pane;
    }

    /*
        protected Component getBottomPane()
        {
            JPanel outerPanel = new JPanel(new BorderLayout());
            JPanel panel = new JPanel(new VerticalLayout(5, VerticalLayout.LEFT));
            postDataField = new JLabeledTextArea(JMeterUtils.getResString("request_data"), null);
            loadTimeLabel = new JLabel();
            panel.add(postDataField);
            panel.add(loadTimeLabel);
            responseCodeLabel = new JLabel();
            panel.add(responseCodeLabel);
            responseMsgLabel = new JLabel();
            panel.add(responseMsgLabel);
            initHtmlOrTextButton();
            panel.add(htmlOrTextButton);
            textScrollArea = new JScrollPane();
            outerPanel.add(panel,BorderLayout.NORTH);
            outerPanel.add(textScrollArea,BorderLayout.CENTER);
            return outerPanel;
        }
    */

    /****************************************
     * Initialize this visualizer
     ***************************************/
    protected void init()
    {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);

        Component topLeft = createTopLeftPanel();
        Component bottomLeft = createBottomLeftPanel();
        JSplitPane leftSide = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topLeft, bottomLeft);
        
        Component topRight = createTopRightPanel();
        Component bottomRight = createBottomRightPanel();
        JSplitPane rightSide = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topRight, bottomRight);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSide, rightSide);
        add(mainSplit, BorderLayout.CENTER);
    }

    private Component createTopLeftPanel() {
        SampleResult rootSampleResult = new SampleResult();
        rootSampleResult.setSampleLabel("Root");
        rootSampleResult.setSuccessful(true);
        root = new DefaultMutableTreeNode(rootSampleResult);
        
        treeModel = new DefaultTreeModel(root);
        jTree = new JTree(treeModel);
        jTree.setCellRenderer(new ResultsNodeRenderer());
        jTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        jTree.addTreeSelectionListener(this);
        jTree.setShowsRootHandles(true);
        
        JScrollPane treePane = new JScrollPane(jTree);
        treePane.setPreferredSize(new Dimension(200, 300));
        return treePane;
    }

    private Component createBottomLeftPanel() {
        stats = new JTextArea();
        stats.setEditable(false);
        stats.setBackground(getBackground());

        JScrollPane pane = makeScrollPane(stats);
        pane.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        return pane;
    }

    private Component createTopRightPanel() {
        sampleDataField = new JTextArea();
        sampleDataField.setEditable(false);
        sampleDataField.setLineWrap(true);
        sampleDataField.setWrapStyleWord(true);
//        sampleDataField.setRows(4);

        JPanel pane = new JPanel (new BorderLayout(0, 5));
        pane.setBorder(BorderFactory.createTitledBorder("Request Data"));
        pane.add (makeScrollPane(sampleDataField));
        return pane;
    }

    private Component createBottomRightPanel() {
        results = new JEditorPane();
        results.setEditable(false);
        
        JPanel resultsPane = new JPanel(new BorderLayout());
        resultsPane.setBorder(BorderFactory.createTitledBorder("Response Data"));
        resultsPane.add(makeScrollPane(results), BorderLayout.CENTER);
        resultsPane.add(createHtmlOrTextPane(), BorderLayout.SOUTH);

        return resultsPane;
    }
    
    private class ResultsNodeRenderer extends DefaultTreeCellRenderer
    {
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
        {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            if (!((SampleResult) ((DefaultMutableTreeNode) value).getUserObject()).isSuccessful())
            {
                this.setForeground(Color.red);
            }
            return this;
        }
    }
}
