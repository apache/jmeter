/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.html.HTMLEditorKit;
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
import org.apache.jorphan.gui.JLabeledTextArea;
/****************************************
 * Allows the tester to view the textual response from sampling an Entry. This
 * also allows to "single step through" the sampling process via a nice
 * "Continue" button.
 *
 *@author    Khor Soon Hin
 *@created   2001/07/25
 *@version   $Revision$ $Date$
 ***************************************/
public class ViewResultsFullVisualizer
        extends AbstractVisualizer
        implements ActionListener, TreeSelectionListener, Clearable
{
        public final static Color SERVER_ERROR_COLOR = Color.red;
        public final static Color CLIENT_ERROR_COLOR = Color.blue;
        public final static Color REDIRECT_COLOR = Color.green;
        protected static final String HTML_BUTTON_LABEL = "Render HTML";
        protected static final String TEXT_BUTTON_LABEL = "Show Text";
        protected DefaultMutableTreeNode root;
        protected DefaultTreeModel treeModel;
        protected GridBagLayout gridBag;
        protected GridBagConstraints gbc;

        /** The button that will pop up the response as rendered HTML or
            text.  **/
        protected JButton htmlOrTextButton;

        /** The response to be displayed.  **/
        protected String response;

        /** The pane where the rendered HTML response is displayed.  **/
        transient protected JEditorPane htmlEditPane;

        protected JPanel resultPanel;
        protected JScrollPane treePane;
        protected JScrollPane resultPane;
        protected JSplitPane treeSplitPane;
        /** The text area where the response is displayed.  **/
        protected JTextArea textArea;
        protected JTree jTree;
        protected int childIndex;
        transient private static Logger log =
                Hierarchy.getDefaultHierarchy().getLoggerFor("jmeter.gui");
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

       private void addSubResults(DefaultMutableTreeNode currNode,
                                  SampleResult res) {
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
                for (int i = 0; i < totalChild; i++)                    // the child to be removed will always be 0 'cos as the nodes are removed
                        // the nth node will become (n-1)th
                        treeModel.removeNodeFromParent(
                                (DefaultMutableTreeNode) root.getChildAt(0));
                resultPanel.removeAll();
                resultPanel.revalidate();
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
                DefaultMutableTreeNode node =
                        (DefaultMutableTreeNode) jTree.getLastSelectedPathComponent();
                if (log.isDebugEnabled())
                        log.debug("valueChanged : selected node - " + node);
                if (node != null)
                {
                        SampleResult res = (SampleResult) node.getUserObject();
                        if (log.isDebugEnabled())
                                log.debug("valueChanged1 : sample result - " + res);
                        if (res != null)
                        {
                                resultPanel.removeAll();
                                // load time label
                                JLabel loadTime = new JLabel();
                                log.debug("valueChanged1 : load time - " + res.getTime());
                                loadTime.setText("Load time : " + res.getTime());
                                gbc.gridx = 0;
                                gbc.gridy = 0;
                                // keep all of the labels to the left
                                gbc.anchor = GridBagConstraints.WEST;
                                // with weightx != 0.0, components won't clump in the center
                                gbc.weightx = 1.0;
                                // pad a bit from the display area
                                gbc.insets = new Insets(0, 10, 0, 0);
                                if (res != null && res.getSamplerData() != null)
                                {
                                        JLabeledTextArea postData =
                                                new JLabeledTextArea(
                                                        JMeterUtils.getResString("request_data"),
                                                        null);
                                        postData.setText(res.getSamplerData().toString());
                                        resultPanel.add(postData, gbc.clone());
                                        gbc.gridy++;
                                }
                                resultPanel.add(loadTime, gbc.clone());
                                // response code label
                                JLabel httpResponseCode = new JLabel();
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
                                                httpResponseCode.setForeground(REDIRECT_COLOR);
                                        case 4 :
                                                httpResponseCode.setForeground(CLIENT_ERROR_COLOR);
                                        case 5 :
                                                httpResponseCode.setForeground(SERVER_ERROR_COLOR);
                                }
                                httpResponseCode.setText(
                                        JMeterUtils.getResString("HTTP response code")
                                                + " : "
                                                + responseCode);
                                gbc.gridx = 0;
                                gbc.gridy++;
                                gridBag.setConstraints(httpResponseCode, gbc);
                                resultPanel.add(httpResponseCode);
                                // response message label
                                JLabel httpResponseMsg = new JLabel();
                                String responseMsgStr = res.getResponseMessage();
                                log.debug("valueChanged1 : response message - " + responseMsgStr);
                                httpResponseMsg.setText("HTTP response message : " + responseMsgStr);
                                gbc.gridx = 0;
                                gbc.gridy++;
                                gridBag.setConstraints(httpResponseMsg, gbc);
                                resultPanel.add(httpResponseMsg);
                                gbc.gridy++;
                                // get the text response and image icon
                                // to determine which is NOT null
                                byte[] responseBytes = (byte[]) res.getResponseData();
                                ImageIcon icon = null;
                                if (res.getDataType() != null
                                        && res.getDataType().equals(SampleResult.TEXT))
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
                                        gbc.gridx = 0;
                                        gbc.gridy++;
                                        gridBag.setConstraints(htmlOrTextButton,
                                                               gbc);
                                        resultPanel.add(htmlOrTextButton);

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
                                else if (icon != null)
                                {
                                        JLabel image = new JLabel();
                                        image.setIcon(icon);
                                        gbc.gridx = 0;
                                        gridBag.setConstraints(image, gbc);
                                        resultPanel.add(image);
                                }
                                resultPanel.repaint();
                                resultPanel.revalidate();
                        }
                }
                log.debug("End : valueChanged1");
        }

        protected void initTextArea()
        {
                textArea = new JTextArea();
                textArea.setColumns(70);
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                textArea.setTabSize(4);
                gridBag.setConstraints(textArea, gbc);
                resultPanel.add(textArea);
                gbc.gridy++;
        }

        protected void showTextResponse(String response)
        {
                resultPanel.remove(htmlEditPane);

                gbc.gridx = 0;
                gbc.gridy++;
                gridBag.setConstraints(textArea, gbc);
                textArea.setText(response);
                textArea.setCaretPosition(0);
                resultPanel.add(textArea);
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


        protected void initHtmlEditPane()
        {
                htmlEditPane = new JEditorPane();
                HTMLEditorKit htmlKit = new HTMLEditorKit();
                htmlEditPane.setEditorKit(htmlKit);
        }

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
                        resultPanel.remove(textArea);
                        String html = response.substring(htmlIndex, response.length());
                        htmlEditPane.setText(html);
                }
                // No HTML tag, so try to render what's there
                else
                {
                        htmlEditPane.setText(response);
                }
                htmlEditPane.setCaretPosition(0);
                gbc.gridx = 0;
                gbc.gridy++;
                gridBag.setConstraints(htmlEditPane, gbc);
                resultPanel.add(htmlEditPane);

        }

        protected void initHtmlOrTextButton()
        {
                htmlOrTextButton = new JButton(HTML_BUTTON_LABEL);
                htmlOrTextButton.addActionListener(this);
        }


        /****************************************
         * Initialize this visualizer
         ***************************************/
        protected void init()
        {
                this.setLayout(new BorderLayout());
                SampleResult rootSampleResult = new SampleResult();
                rootSampleResult.setSampleLabel("Root");
                rootSampleResult.setSuccessful(true);
                root = new DefaultMutableTreeNode(rootSampleResult);
                treeModel = new DefaultTreeModel(root);
                jTree = new JTree(treeModel);
                jTree.setCellRenderer(new ResultsNodeRenderer());
                jTree.getSelectionModel().setSelectionMode(
                        TreeSelectionModel.SINGLE_TREE_SELECTION);
                jTree.addTreeSelectionListener(this);
                treePane = new JScrollPane(jTree);
                gridBag = new GridBagLayout();
                gbc = new GridBagConstraints();
                resultPanel = new JPanel(gridBag);
                resultPane = new JScrollPane(resultPanel);
                initHtmlOrTextButton();
                initTextArea();
                initHtmlEditPane();
                treeSplitPane =
                        new JSplitPane(JSplitPane.VERTICAL_SPLIT, treePane, resultPane);
                getFilePanel().add(getErrorLoggingCheckbox());
                add(getFilePanel(), BorderLayout.NORTH);
                add(treeSplitPane, BorderLayout.CENTER);
        }
        private class ResultsNodeRenderer extends DefaultTreeCellRenderer
        {
                public Component getTreeCellRendererComponent(
                        JTree tree,
                        Object value,
                        boolean sel,
                        boolean expanded,
                        boolean leaf,
                        int row,
                        boolean hasFocus)
                {
                        super.getTreeCellRendererComponent(
                                tree,
                                value,
                                sel,
                                expanded,
                                leaf,
                                row,
                                hasFocus);
                        if (!((SampleResult) ((DefaultMutableTreeNode) value).getUserObject())
                                .isSuccessful())
                        {
                                this.setForeground(Color.red);
                        }
                        return this;
                }
        }
}
