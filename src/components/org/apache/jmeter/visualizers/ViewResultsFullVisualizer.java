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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
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
    public final static Color SERVER_ERROR_COLOR = Color.red;
    public final static Color CLIENT_ERROR_COLOR = Color.blue;
    public final static Color REDIRECT_COLOR = Color.green;
    protected static final String HTML_BUTTON_LABEL = "Render HTML";
    protected static final String TEXT_BUTTON_LABEL = "Show Text";
    protected DefaultMutableTreeNode root;
    protected DefaultTreeModel treeModel;
    //    protected GridBagLayout gridBag;
    //    protected GridBagConstraints gbc;
    //    private JScrollPane textScrollArea;

    /** The button that will pop up the response as rendered HTML or
     text.  **/
    protected JButton htmlOrTextButton;

    /** The response to be displayed.  **/
    //    protected String response;

    /** The pane where the rendered HTML response is displayed.  **/
    //    transient protected JEditorPane htmlEditPane;
    //    private JSplitPane treeSplitPane;

    /** The text area where the response is displayed.  **/
    //    protected JTextArea textArea;
    protected JTree jTree;
    protected int childIndex;
    transient private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor("jmeter.gui");

    //    private JLabel loadTimeLabel;
    //    private JLabeledTextArea postDataField;
    //    private JLabel responseCodeLabel;
    //    private JLabel responseMsgLabel;
    /****************************************
     * !ToDo (Constructor description)
     ***************************************/
    public ViewResultsFullVisualizer()
    {
        super();
        init();
        log.debug("Start : ViewResultsFullVisualizer1");
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
        { // the child to be removed will always be 0 'cos as the nodes are removed
            // the nth node will become (n-1)th
            treeModel.removeNodeFromParent((DefaultMutableTreeNode) root.getChildAt(0));
        }

        results.setText("");
        //            textArea.setText("");
        //        textScrollArea.setViewportView(textArea);
        // reset the child index
        childIndex = 0;
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
                    sampleDataField.setText("Request data: " + res.getSamplerData().trim());
                    //                    postDataField.setText(res.getSamplerData().toString());
                }

                stats.append("Load time: " + res.getTime() + "\n");
                //                loadTimeLabel.setText("Load time : " + res.getTime());
                //                gbc.gridx = 0;
                //                gbc.gridy = 0;
                // keep all of the labels to the left
                //                gbc.anchor = GridBagConstraints.WEST;
                // with weightx != 0.0, components won't clump in the center
                //                gbc.weightx = 1.0;
                // pad a bit from the display area
                //                gbc.insets = new Insets(0, 10, 0, 0);
                // response code label

                String responseCode = res.getResponseCode();

                log.debug("valueChanged1 : response code - " + responseCode);
                int responseLevel = 0;

                if (responseCode != null)
                    try
                    {
                        responseLevel = Integer.parseInt(responseCode) / 100;
                    }
                    catch (NumberFormatException numberFormatException)
                    {
                        // no need to change the foreground color
                    }
                switch (responseLevel)
                {
                    case 3 :
                        //                        responseCodeLabel.setForeground(REDIRECT_COLOR);
                        break;
                    case 4 :
                        //                        responseCodeLabel.setForeground(CLIENT_ERROR_COLOR);
                        break;
                    case 5 :
                        //                        responseCodeLabel.setForeground(SERVER_ERROR_COLOR);
                        break;
                }
                stats.append("HTTP response code: " + responseCode + "\n");
                //                responseCodeLabel.setText(JMeterUtils.getResString("HTTP response code") + " : " + responseCode);
                // response message label

                String responseMsgStr = res.getResponseMessage();

                log.debug("valueChanged1 : response message - " + responseMsgStr);
                stats.append("HTTP response message: " + responseMsgStr);
                //                responseMsgLabel.setText("HTTP response message : " + responseMsgStr);
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
                    // Text display <=> HTML labeled button
                    if (HTML_BUTTON_LABEL.equals(htmlOrTextButton.getText()))
                    {
                        showTextResponse(response);
                    }
                    // HTML display <=> Text labeled button
                    else
                    {
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
        //        textArea.setText(response);
        //        textArea.setCaretPosition(0);
        //        textScrollArea.setViewportView(textArea);
        results.setText(response);
        results.setCaretPosition(0);
    }

    /**********************************************************************
     * Display the response as text or as rendered HTML.  Change the
     * text on the button appropriate to the current display.
     * @param e the ActionEvent being processed
     *********************************************************************/

    public void actionPerformed(ActionEvent e)
    {
        // If the htmlOrTextButton is clicked, show the response in the
        // appropriate way, and change the button label
        if (htmlOrTextButton.equals(e.getSource()))
        {

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) jTree.getLastSelectedPathComponent();
            if (node == null)
            {
                results.setText("");
            }
            else
            {
                SampleResult res = (SampleResult) node.getUserObject();
                byte[] responseBytes = (byte[]) res.getResponseData();
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

                // Show rendered HTML
                if (HTML_BUTTON_LABEL.equals(htmlOrTextButton.getText()))
                {
                    showRenderedResponse(response);
                    htmlOrTextButton.setText(TEXT_BUTTON_LABEL);
                }
                // Show the textual response
                else
                {
                    showTextResponse(response);
                    htmlOrTextButton.setText(HTML_BUTTON_LABEL);
                }
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
        int htmlIndex = response.indexOf("<HTML>");

        // Look for a case variation
        if (htmlIndex < 0)
        {
            htmlIndex = response.indexOf("<html>");
        }

        // If there is text, render it
        if (htmlIndex > -1)
        {
            String html = response.substring(htmlIndex, response.length());

            //            htmlEditPane.setText(html);
            results.setText(html);
        }
        // No HTML tag, so try to render what's there
        else
        {
            //            htmlEditPane.setText(response);
            results.setText(response);
        }
        results.setCaretPosition(0);
        //        htmlEditPane.setCaretPosition(0);
        //        textScrollArea.setViewportView(htmlEditPane);

    }

    protected void initHtmlOrTextButton()
    {
        htmlOrTextButton = new JButton(HTML_BUTTON_LABEL);
        htmlOrTextButton.addActionListener(this);
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
    private JTextArea stats;
    private JTextArea results;
    private JTextArea sampleDataField;

    protected void initSampleDataArea()
    {
        sampleDataField = new JTextArea();
        sampleDataField.setLineWrap(true);
        sampleDataField.setWrapStyleWord(true);
    }

    /****************************************
     * Initialize this visualizer
     ***************************************/
    protected void init()
    {
        setLayout(new BorderLayout());
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);

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
        treePane.setPreferredSize(new Dimension(100, 70));

        JPanel statsPane = new JPanel(new GridLayout(1, 1));
        stats = new JTextArea();

        statsPane.add(makeScrollPane(stats));
        initSampleDataArea();
        initHtmlOrTextButton();
        results = new JTextArea();
        JPanel resultsPane = makeResultPane();

        JSplitPane split2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, treePane, statsPane);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, split2, resultsPane);
        add(mainSplit, BorderLayout.CENTER);
    }

    protected JPanel makeResultPane()
    {
        JPanel resultsPane = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = .1;
        resultsPane.add(makeScrollPane(sampleDataField), gbc.clone());
        gbc.gridy++;
        gbc.weighty = .9;
        resultsPane.add(makeScrollPane(results),gbc.clone());
        gbc.gridy++;
        gbc.weighty = 0;
        resultsPane.add(htmlOrTextButton,gbc.clone());
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
