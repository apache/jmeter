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
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.ComponentView;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Allows the tester to view the textual response from sampling an Entry. This
 * also allows to "single step through" the sampling process via a nice
 * "Continue" button.
 *
 * Created     2001/07/25
 * @version   $Revision$ $Date$
 */
public class ViewResultsFullVisualizer
    extends AbstractVisualizer
    implements ActionListener, TreeSelectionListener, Clearable
{
	transient private static Logger log = LoggingManager.getLoggerForClass();

    public final static Color SERVER_ERROR_COLOR = Color.red;
    public final static Color CLIENT_ERROR_COLOR = Color.blue;
    public final static Color REDIRECT_COLOR = Color.green;

    private static final String DOWNLOAD_LABEL = "Download embedded resources";
    private static final String HTML_BUTTON_LABEL = "Render HTML";
    private static final String TEXT_BUTTON_LABEL = "Show Text";

    private static final String TEXT_HTML = "text/html"; // $NON-NLS-1$
    private static final String HTML_COMMAND = "html"; // $NON-NLS-1$
    private static final String TEXT_COMMAND = "text"; // $NON-NLS-1$
    private boolean textMode = true;
    
    // Keep copies of the two editors needed
    private static EditorKit customisedEditor = new LocalHTMLEditorKit();
    private static EditorKit defaultHtmlEditor = 
    	JEditorPane.createEditorKitForContentType(TEXT_HTML);

    private DefaultMutableTreeNode root;
    private DefaultTreeModel treeModel;

    private JTextPane stats;
    private JEditorPane results;
    private JScrollPane resultsScrollPane;
    private JLabel imageLabel;
    private JTextArea sampleDataField;

    private JRadioButton textButton;
    private JRadioButton htmlButton;
    private JCheckBox downloadAll;

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

    public String getLabelResource()
    {
        return "view_results_tree_title";
    }

    /**
     * Update the visualizer with new data.
     */
    public synchronized void updateGui(SampleResult res)
    {
        log.debug("Start : updateGui1");
        if (log.isDebugEnabled())
        {
            log.debug("updateGui1 : sample result - " + res);
        }
        DefaultMutableTreeNode currNode = new DefaultMutableTreeNode(res);

        treeModel.insertNodeInto(currNode, root, root.getChildCount());
        addSubResults(currNode, res);
        log.debug("End : updateGui1");
    }

    private void addSubResults(
        DefaultMutableTreeNode currNode,
        SampleResult res)
    {
        SampleResult[] subResults = res.getSubResults();

        int leafIndex = 0;

        for (int i = 0; i < subResults.length; i++)
        {
            SampleResult child = subResults[i];

            if (log.isDebugEnabled())
            {
                log.debug("updateGui1 : child sample result - " + child);
            }
            DefaultMutableTreeNode leafNode =
                new DefaultMutableTreeNode(child);

            treeModel.insertNodeInto(leafNode, currNode, leafIndex++);
            addSubResults(leafNode, child);
        }
    }

    /**
     * Clears the visualizer.
     */
    public void clear()
    {
        log.debug("Start : clear1");
        int totalChild = root.getChildCount();

        if (log.isDebugEnabled())
        {
            log.debug("clear1 : total child - " + totalChild);
        }
        for (int i = 0; i < totalChild; i++)
        {
            // the child to be removed will always be 0 'cos as the nodes are
            // removed the nth node will become (n-1)th
            treeModel.removeNodeFromParent(
                (DefaultMutableTreeNode) root.getChildAt(0));
        }

        results.setText("");//Response Data
        sampleDataField.setText("");//Request Data
        log.debug("End : clear1");
    }

    /**
     * Returns the description of this visualizer.
     *
     * @return   description of this visualizer
     */
    public String toString()
    {
        String desc = "Shows the text results of sampling in tree form";

        if (log.isDebugEnabled())
        {
            log.debug("toString1 : Returning description - " + desc);
        }
        return desc;
    }

    /**
     * Sets the right pane to correspond to the selected node of the left tree.
     */
    public void valueChanged(TreeSelectionEvent e)
    {
        log.debug("Start : valueChanged1");
        DefaultMutableTreeNode node =
            (DefaultMutableTreeNode) jTree.getLastSelectedPathComponent();

        if (log.isDebugEnabled())
        {
            log.debug("valueChanged : selected node - " + node);
        }

        StyledDocument statsDoc = stats.getStyledDocument();
        try
        {
            statsDoc.remove(0, statsDoc.getLength());
			sampleDataField.setText("");
			results.setText("");
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
                    	String sd;
                    	String rh = res.getRequestHeaders();
                    	if (rh==null)
                    	{
                    		sd=res.getSamplerData().trim();
                    	} else {
							sd=res.getSamplerData().trim()
							   +"\n"+rh;
                    	} 
                        sampleDataField.setText(sd);
                    }

                    statsDoc.insertString(
                        statsDoc.getLength(),
                        "Load time: " + res.getTime() + "\n",
                        null);

                    String responseCode = res.getResponseCode();
                    log.debug(
                        "valueChanged1 : response code - " + responseCode);

                    int responseLevel = 0;
                    if (responseCode != null)
                    {
                        try
                        {
                            responseLevel =
                                Integer.parseInt(responseCode) / 100;
                        }
                        catch (NumberFormatException numberFormatException)
                        {
                            // no need to change the foreground color
                        }
                    }

                    Style style = null;
                    switch (responseLevel)
                    {
                        case 3 :
                            style = statsDoc.getStyle("Redirect");
                            break;
                        case 4 :
                            style = statsDoc.getStyle("ClientError");
                            break;
                        case 5 :
                            style = statsDoc.getStyle("ServerError");
                            break;
                    }
                    statsDoc.insertString(
                        statsDoc.getLength(),
                        "HTTP response code: " + responseCode + "\n",
                        style);

                    // response message label
                    String responseMsgStr = res.getResponseMessage();

                    log.debug(
                        "valueChanged1 : response message - " + responseMsgStr);
                    statsDoc.insertString(
                        statsDoc.getLength(),
                        "HTTP response message: " + responseMsgStr + "\n",
                        null);

					statsDoc.insertString(
						statsDoc.getLength(),
						"\nHTTP response headers:\n" + res.getResponseHeaders() + "\n",
						null);

					// get the text response and image icon
                    // to determine which is NOT null
                    if ((SampleResult.TEXT).equals(res.getDataType())) // equals(null) is OK
                    {
	                    String response = getResponseAsString(res);
	                    if (textMode)
	                    {
	                        showTextResponse(response);
	                    }
	                    else
	                    {
	                        showRenderedResponse(response,res);
	                    }
                    }
                    else
                    {
    	                byte[] responseBytes = res.getResponseData();
    	                if (responseBytes != null)
    	                {
    	                	showImage(new ImageIcon(responseBytes));	
    	                }
                    }
                }
            }
        }
        catch (BadLocationException exc)
        {
            log.error("Error setting statistics text", exc);
            stats.setText("");
        }
        log.debug("End : valueChanged1");
    }

    private void showImage(Icon image)
    {
        imageLabel.setIcon(image);
        resultsScrollPane.setViewportView(imageLabel);
        textButton.setEnabled(false);
        htmlButton.setEnabled(false);
    }

    protected void showTextResponse(String response)
    {
        results.setContentType("text/plain");
		results.setText(response == null ? "" : response);
        results.setCaretPosition(0);
        resultsScrollPane.setViewportView(results);

        textButton.setEnabled(true);
        htmlButton.setEnabled(true);
    }

    private static String getResponseAsString(SampleResult res)
	{
    	
        byte[] responseBytes = res.getResponseData();
        String response = null;
        if ((SampleResult.TEXT).equals(res.getDataType()))
        {
            try
            {
				// Showing large strings can be VERY costly, so we will avoid doing so if the response
				// data is larger than 200K. TODO: instead, we could delay doing the result.setText
				// call until the user chooses the "Response data" tab. Plus we could warn the user
				// if this happens and revert the choice if he doesn't confirm he's ready to wait.
				if (responseBytes.length > 200*1024)
				{
					response= 
						("Response too large to be displayed ("+responseBytes.length+" bytes).");
					log.warn("Response too large to display.");
				}
				else
				{
					response = 
						new String(responseBytes,res.getDataEncoding());
				}
            }
            catch (UnsupportedEncodingException err)
            {
            	log.warn("Could not decode response "+err);
				response = 	new String(responseBytes);// Try the default encoding instead
            }
        }
    	return response;
    }

    /**
     * Display the response as text or as rendered HTML.  Change the
     * text on the button appropriate to the current display.
     * 
     * @param e the ActionEvent being processed
     */
    public void actionPerformed(ActionEvent e)
    {
        String command = e.getActionCommand();

        if (command != null
            && (
                command.equals(TEXT_COMMAND)
                ||
                command.equals(HTML_COMMAND)
               )
            )
        {

            textMode = command.equals(TEXT_COMMAND);

            DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) jTree.getLastSelectedPathComponent();

            if (node == null)
            {
                results.setText("");
                return;
            }

            SampleResult res = (SampleResult) node.getUserObject();
            String response = getResponseAsString(res);
            if (textMode)
            {
                showTextResponse(response);
            }
            else
            {
                showRenderedResponse(response,res);
            }
        }
    }

    protected void showRenderedResponse(String response, SampleResult res)
    {
        if (response == null)
        {
            results.setText("");
            return;
        }

        int htmlIndex = response.indexOf("<HTML"); // could be <HTML lang="">

        // Look for a case variation
        if (htmlIndex < 0)
        {
            htmlIndex = response.indexOf("<html"); // ditto
        }

        // If we still can't find it, just try using all of the text
        if (htmlIndex < 0)
        {
            htmlIndex = 0;
        }

        String html = response.substring(htmlIndex);
        
        /* 
         * To disable downloading and rendering of images and frames,
         * enable the editor-kit. The Stream property can then be
         */
        
		// Must be done before setContentType
		results.setEditorKitForContentType(TEXT_HTML,
				downloadAll.isSelected() ? defaultHtmlEditor : customisedEditor);

        results.setContentType(TEXT_HTML);

        if (downloadAll.isSelected())
        {
            // Allow JMeter to render frames (and relative images)
            // Must be done after setContentType [Why?]
		    results.getDocument().putProperty(Document.StreamDescriptionProperty,res.getURL());
        }

        /* Get round problems parsing
         *  <META http-equiv='content-type' content='text/html; charset=utf-8'>
         * See http://nagoya.apache.org/bugzilla/show_bug.cgi?id=23315
         *
         * Is this due to a bug in Java?
         */
		results.getDocument().putProperty("IgnoreCharsetDirective", Boolean.TRUE);

        results.setText(html);
        results.setCaretPosition(0);
        resultsScrollPane.setViewportView(results);

        textButton.setEnabled(true);
        htmlButton.setEnabled(true);
    }

    protected Component createHtmlOrTextPane()
    {
        ButtonGroup group = new ButtonGroup();

        textButton = new JRadioButton(TEXT_BUTTON_LABEL);
        textButton.setActionCommand(TEXT_COMMAND);
        textButton.addActionListener(this);
        textButton.setSelected(textMode);
        group.add(textButton);

        htmlButton = new JRadioButton(HTML_BUTTON_LABEL);
        htmlButton.setActionCommand(HTML_COMMAND);
        htmlButton.addActionListener(this);
        htmlButton.setSelected(!textMode);
        group.add(htmlButton);

        downloadAll = new JCheckBox(DOWNLOAD_LABEL);

        JPanel pane = new JPanel();
        pane.add(textButton);
        pane.add(htmlButton);
        pane.add(downloadAll);
        return pane;
    }

    /**
     * Initialize this visualizer
     */
    protected void init()
    {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);

        Component leftSide = createLeftPanel();
        JTabbedPane rightSide= new JTabbedPane();

        rightSide.addTab(JMeterUtils.getResString("view_results_tab_sampler"), createResponseMetadataPanel());
		rightSide.addTab(JMeterUtils.getResString("view_results_tab_request"), createRequestPanel());
		rightSide.addTab(JMeterUtils.getResString("view_results_tab_response"), createResponseDataPanel());

        JSplitPane mainSplit =
            new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSide, rightSide);
        add(mainSplit, BorderLayout.CENTER);
    }

    private Component createLeftPanel()
    {
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
        jTree.setShowsRootHandles(true);

        JScrollPane treePane = new JScrollPane(jTree);
        treePane.setPreferredSize(new Dimension(200, 300));
        return treePane;
    }

    private Component createResponseMetadataPanel()
    {
        stats = new JTextPane();
        stats.setEditable(false);
        stats.setBackground(getBackground());

        // Add styles to use for different types of status messages        
        StyledDocument doc = (StyledDocument) stats.getDocument();

        Style style = doc.addStyle("Redirect", null);
        StyleConstants.setForeground(style, REDIRECT_COLOR);

        style = doc.addStyle("ClientError", null);
        StyleConstants.setForeground(style, CLIENT_ERROR_COLOR);

        style = doc.addStyle("ServerError", null);
        StyleConstants.setForeground(style, SERVER_ERROR_COLOR);

        JScrollPane pane = makeScrollPane(stats);
        pane.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        return pane;
    }

    private Component createRequestPanel()
    {
        sampleDataField = new JTextArea();
        sampleDataField.setEditable(false);
        sampleDataField.setLineWrap(true);
        sampleDataField.setWrapStyleWord(true);

        JPanel pane = new JPanel(new BorderLayout(0, 5));
        pane.add(makeScrollPane(sampleDataField));
        return pane;
    }

    private Component createResponseDataPanel()
    {
        results = new JEditorPane();
        results.setEditable(false);

        resultsScrollPane = makeScrollPane(results);
        imageLabel = new JLabel();

        JPanel resultsPane = new JPanel(new BorderLayout());
        resultsPane.add(resultsScrollPane, BorderLayout.CENTER);
        resultsPane.add(createHtmlOrTextPane(), BorderLayout.SOUTH);

        return resultsPane;
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
            if (!((SampleResult) ((DefaultMutableTreeNode) value)
                .getUserObject())
                .isSuccessful())
            {
                this.setForeground(Color.red);
            }
            return this;
        }
    }

    private static class LocalHTMLEditorKit extends HTMLEditorKit {

    	private static final ViewFactory defaultFactory = new LocalHTMLFactory();
    	
    	public ViewFactory getViewFactory() {
    		return defaultFactory;
    	}

    	private static class LocalHTMLFactory 
		extends javax.swing.text.html.HTMLEditorKit.HTMLFactory 
		{
    		/*
    		 * Provide dummy implementations to suppress download and display
    		 * of related resources:
    		 * - FRAMEs
    		 * - IMAGEs
    		 * TODO create better dummy displays
    		 */
    		public View create(Element elem) 
    		{
    		    Object o = elem.getAttributes().getAttribute(StyleConstants.NameAttribute);
    		    if (o instanceof HTML.Tag) 
    		    {
    			    HTML.Tag kind = (HTML.Tag) o;
    			    if (kind == HTML.Tag.FRAME)
    			    {
    			        return new ComponentView(elem);
	   			    }
	   			    else if (kind==HTML.Tag.IMG)
	   			    {
	   			    	return new ComponentView(elem);
	   		        }
    			}
    			return super.create(elem);
    		}
    	}
    }
}
