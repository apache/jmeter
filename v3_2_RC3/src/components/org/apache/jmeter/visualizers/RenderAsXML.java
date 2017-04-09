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
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.visualizers;

import java.awt.Component;
import java.awt.GridLayout;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.XPathUtil;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;
import org.xml.sax.SAXException;

public class RenderAsXML extends SamplerResultTab
    implements ResultRenderer {

    private static final Logger log = LoggerFactory.getLogger(RenderAsXML.class);

    private static final byte[] XML_PFX = {'<','?','x','m','l',' '};//"<?xml "

    public RenderAsXML(){
        activateSearchExtension = false; // TODO work out how to search the XML pane
    }

    /** {@inheritDoc} */
    @Override
    public void renderResult(SampleResult sampleResult) {
        showRenderXMLResponse(sampleResult);
    }

    private void showRenderXMLResponse(SampleResult res) {
        results.setContentType("text/xml"); // $NON-NLS-1$
        results.setCaretPosition(0);
        byte[] source = res.getResponseData();
        final ByteArrayInputStream baIS = new ByteArrayInputStream(source);
        for(int i=0; i<source.length-XML_PFX.length; i++){
            if (JOrphanUtils.startsWith(source, XML_PFX, i)){
                baIS.skip(i);// NOSONAR Skip the leading bytes (if any)
                break;
            }
        }

        StringWriter sw = new StringWriter();
        Tidy tidy = XPathUtil.makeTidyParser(true, true, true, sw);
        org.w3c.dom.Document document = tidy.parseDOM(baIS, null);
        document.normalize();
        if (tidy.getParseErrors() > 0) {
            showErrorMessageDialog(sw.toString(),
                    "Tidy: " + tidy.getParseErrors() + " errors, " + tidy.getParseWarnings() + " warnings",
                    JOptionPane.WARNING_MESSAGE);
        }

        JPanel domTreePanel = new DOMTreePanel(document);
        resultsScrollPane.setViewportView(domTreePanel);
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.visualizers.SamplerResultTab#clearData()
     */
    @Override
    public void clearData() {
        super.clearData();
        resultsScrollPane.setViewportView(null); // clear result tab on Ctrl-E
    }

    /*
     *
     * A Dom tree panel for to display response as tree view author <a
     * href="mailto:d.maung@mdl.com">Dave Maung</a> 
     * TODO implement to find any nodes in the tree using TreePath.
     *
     */
    private static class DOMTreePanel extends JPanel {

        private static final long serialVersionUID = 6871690021183779153L;

        private JTree domJTree;

        public DOMTreePanel(org.w3c.dom.Document document) {
            super(new GridLayout(1, 0));
            try {
                Node firstElement = getFirstElement(document);
                DefaultMutableTreeNode top = new XMLDefaultMutableTreeNode(firstElement);
                domJTree = new JTree(top);

                domJTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
                domJTree.setShowsRootHandles(true);
                JScrollPane domJScrollPane = new JScrollPane(domJTree);
                domJTree.setAutoscrolls(true);
                this.add(domJScrollPane);
                ToolTipManager.sharedInstance().registerComponent(domJTree);
                domJTree.setCellRenderer(new DomTreeRenderer());
            } catch (SAXException e) {
                log.warn("Error trying to parse document", e);
            }

        }

        /**
         * Skip all DTD nodes, all prolog nodes. They are not supported in tree view
         * We let user insert them however in DOMTreeView, we don't display them.
         *
         * @param parent {@link Node}
         * @return
         */
        private Node getFirstElement(Node parent) {
            NodeList childNodes = parent.getChildNodes();
            Node toReturn = parent; // Must return a valid node, or may generate an NPE
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node childNode = childNodes.item(i);
                toReturn = childNode;
                if (childNode.getNodeType() == Node.ELEMENT_NODE){
                    break;
                }

            }
            return toReturn;
        }

        /**
         * This class is to view as tooltext. This is very useful, when the
         * contents has long string and does not fit in the view. it will also
         * automatically wrap line for each 100 characters since tool tip
         * support html. 
         */
        private static class DomTreeRenderer extends DefaultTreeCellRenderer {

            private static final long serialVersionUID = 240210061375790195L;

            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                    boolean leaf, int row, boolean phasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, phasFocus);

                DefaultMutableTreeNode valueTreeNode = (DefaultMutableTreeNode) value;
                setToolTipText(getHTML(valueTreeNode.toString(), "<br>", 100)); // $NON-NLS-1$
                return this;
            }

            /**
             * get the html
             *
             * @param str
             * @param separator
             * @param maxChar
             * @return
             */
            private String getHTML(String str, String separator, int maxChar) {
                StringBuilder strBuf = new StringBuilder("<html><body bgcolor=\"yellow\"><b>"); // $NON-NLS-1$
                char[] chars = str.toCharArray();
                for (int i = 0; i < chars.length; i++) {

                    if (i % maxChar == 0 && i != 0) {
                        strBuf.append(separator);
                    }
                    strBuf.append(encode(chars[i]));

                }
                strBuf.append("</b></body></html>"); // $NON-NLS-1$
                return strBuf.toString();

            }

            private String encode(char c) {
                String toReturn = String.valueOf(c);
                switch (c) {
                case '<': // $NON-NLS-1$
                    toReturn = "&lt;"; // $NON-NLS-1$
                    break;
                case '>': // $NON-NLS-1$
                    toReturn = "&gt;"; // $NON-NLS-1$
                    break;
                case '\'': // $NON-NLS-1$
                    toReturn = "&apos;"; // $NON-NLS-1$
                    break;
                case '\"': // $NON-NLS-1$
                    toReturn = "&quot;"; // $NON-NLS-1$
                    break;
                default:
                    // ignored
                    break;

                }
                return toReturn;
            }
        }
    }

    private static void showErrorMessageDialog(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(null, message, title, messageType);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return JMeterUtils.getResString("view_results_render_xml"); // $NON-NLS-1$
    }

}
