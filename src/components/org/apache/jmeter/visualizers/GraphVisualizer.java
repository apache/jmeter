/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001,2003 The Apache Software Foundation.  All rights
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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.jmeter.gui.util.JMeterColor;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.apache.jorphan.gui.layout.VerticalLayout;


/****************************************
 * This class implements a statistical analyser that calculates both the average
 * and the standard deviation of the sampling process and outputs them as
 * autoscaling plots.
 *
 *@author    <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 *@created   February 8, 2001
 *@version   $Revision$ $Date$
 ***************************************/
public class GraphVisualizer extends AbstractVisualizer
        implements ImageVisualizer, ItemListener, GraphListener, Clearable
{
    GraphModel model;
    private JTextField maxYField = null;
    private JTextField minYField = null;
    private JTextField noSamplesField = null;
    String minute = JMeterUtils.getResString("minute");

    private Graph graph;
    private JCheckBox data;
    private JCheckBox average;
    private JCheckBox deviation;
    private JCheckBox throughput;
    private JTextField dataField;
    private JTextField averageField;
    private JTextField deviationField;
    private JTextField throughputField;
    private boolean perSecond = false;

    /****************************************
     * Constructor for the GraphVisualizer object
     ***************************************/
    public GraphVisualizer()
    {
        model = new GraphModel();
        model.addGraphListener(this);
        graph = new Graph(model);
        init();
    }

    /****************************************
     * Gets the Image attribute of the GraphVisualizer object
     *
     *@return   The Image value
     ***************************************/
    public Image getImage()
    {
        Image result = graph.createImage(graph.getWidth(), graph.getHeight());

        graph.paintComponent(result.getGraphics());

        return result;
    }

    /****************************************
     * !ToDo (Method description)
     ***************************************/
    public void updateGui()
    {
        graph.updateGui();
        noSamplesField.setText(Long.toString(model.getSampleCount()));
        dataField.setText(Long.toString(model.getCurrentData()));
        averageField.setText(Long.toString(model.getCurrentAverage()));
        deviationField.setText(Long.toString(model.getCurrentDeviation()));
        throughputField.setText(Float.toString(model.getCurrentThroughput()) + "/" + minute);
        updateYAxis();
    }

    /****************************************
     * !ToDo (Method description)
     *
     *@param s  !ToDo (Parameter description)
     ***************************************/
    public void updateGui(Sample s)
    {
        // We have received one more sample
        graph.updateGui(s);
        noSamplesField.setText(Long.toString(model.getSampleCount()));
        dataField.setText(Long.toString(s.data));
        averageField.setText(Long.toString(s.average));
        deviationField.setText(Long.toString(s.deviation));
        throughputField.setText(Float.toString(s.throughput) + "/" + minute);
        updateYAxis();
    }

    /****************************************
     * !ToDo (Method description)
     *
     *@param res  !ToDo (Parameter description)
     ***************************************/
    public void add(SampleResult res)
    {
        model.addSample(res);
    }

    /****************************************
     * !ToDoo (Method description)
     *
     *@return   !ToDo (Return description)
     ***************************************/
    public String getStaticLabel()
    {
        return JMeterUtils.getResString("graph_results_title");
    }

    /****************************************
     * Description of the Method
     *
     *@param e  Description of Parameter
     ***************************************/
    public void itemStateChanged(ItemEvent e)
    {
        if (e.getItem() == data)
        {
            this.graph.enableData(e.getStateChange() == ItemEvent.SELECTED);
        }
        else if (e.getItem() == average)
        {
            this.graph.enableAverage(e.getStateChange() == ItemEvent.SELECTED);
        }
        else if (e.getItem() == deviation)
        {
            this.graph.enableDeviation(e.getStateChange() == ItemEvent.SELECTED);
        }
        else if (e.getItem() == throughput)
        {
            this.graph.enableThroughput(e.getStateChange() == ItemEvent.SELECTED);
        }
        this.graph.repaint();
    }

    /****************************************
     * Description of the Method
     ***************************************/
    public synchronized void clear()
    {
        // this.graph.clear();
        model.clear();
        dataField.setText("0000");
        averageField.setText("0000");
        deviationField.setText("0000");
        throughputField.setText("0/" + minute);
        updateYAxis();
        repaint();
    }

    /****************************************
     * Description of the Method
     *
     *@return   Description of the Returned Value
     ***************************************/
    public String toString()
    {
        return "Show the samples analysis as dot plots";
    }

    /****************************************
     * Update the max and min value of the Y axis
     ***************************************/
    private void updateYAxis()
    {
        maxYField.setText(Long.toString(model.getGraphMax()));
        minYField.setText("0");
    }

    /****************************************
     * Initialize the GUI
     ***************************************/
    private void init()
    {
        this.setLayout(new BorderLayout());

        // MAIN PANEL
        JPanel mainPanel = new JPanel();
        Border margin = new EmptyBorder(10, 10, 5, 10);

        mainPanel.setBorder(margin);
        mainPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));

        // TITLE
        JLabel panelTitleLabel = new JLabel(JMeterUtils.getResString("graph_results_title"));
        Font curFont = panelTitleLabel.getFont();
        int curFontSize = curFont.getSize();

        curFontSize += 4;
        panelTitleLabel.setFont(new Font(curFont.getFontName(), curFont.getStyle(), curFontSize));
        mainPanel.add(panelTitleLabel);

        // NAME
        mainPanel.add(getNamePanel());
        mainPanel.add(this.getFilePanel());


        // Set up the graph with header, footer, Y axis and graph display
        JPanel graphPanel = new JPanel(new BorderLayout());
        graphPanel.add(createYAxis(), BorderLayout.WEST);
        graphPanel.add(createChoosePanel(), BorderLayout.NORTH);
        graphPanel.add(createGraphPanel(), BorderLayout.CENTER);
        graphPanel.add(createGraphInfoPanel(), BorderLayout.SOUTH);

        // Add the main panel and the graph
        this.add(mainPanel, BorderLayout.NORTH);
        this.add(graphPanel, BorderLayout.CENTER);
    }

    // Methods used in creating the GUI

    /**
     * Creates the panel containing the graph's Y axis labels.
     * 
     * @return the Y axis panel
     */
    private JPanel createYAxis() {
        JPanel graphYAxisPanel = new JPanel();
        
        graphYAxisPanel.setLayout(new BorderLayout());
        
        maxYField = createYAxisField(5);
        minYField = createYAxisField(3);

        graphYAxisPanel.add(createYAxisPanel("graph_results_ms", maxYField),
                        BorderLayout.NORTH);
        graphYAxisPanel.add(createYAxisPanel("graph_results_ms", minYField),
                        BorderLayout.SOUTH);

        return graphYAxisPanel;
    }

    /**
     * Creates a text field to be used for the value of a Y axis
     * label.  These fields hold the minimum and maximum values
     * for the graph.  The units are kept in a separate label
     * outside of this field.
     * 
     * @param length the number of characters which the field
     *                will use to calculate its preferred width.
     *                This should be set to the maximum number
     *                of digits that are expected to be necessary
     *                to hold the label value.
     * 
     * @see #createYAxisPanel(String, JTextField)
     * 
     * @return a text field configured to be used in the Y axis
     */
    private JTextField createYAxisField(int length) {
        JTextField field = new JTextField(length);
        field.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        field.setEditable(false);
        field.setForeground(Color.black);
        field.setBackground(getBackground());
        field.setHorizontalAlignment(JTextField.RIGHT);
        return field;
    }

    /**
     * Creates a panel for an entire Y axis label. This includes
     * the dynamic value as well as the unit label.
     * 
     * @param labelResourceName the name of the label resource.
     *          This is used to look up the label text using
     *          {@link JMeterUtils#getResString(String)}.
     * 
     * @return a panel containing both the dynamic and static parts
     *          of a Y axis label
     */
    private JPanel createYAxisPanel(String labelResourceName, JTextField field) {
        JPanel panel = new JPanel(new FlowLayout());
        JLabel label = new JLabel(JMeterUtils.getResString(labelResourceName));
        
        panel.add(field);
        panel.add(label);
        return panel;
    }


    /**
     * Creates a panel which allows the user to choose which graphs
     * to display. This panel consists of a check box for each type
     * of graph (current sample, average, deviation, and throughput).
     * 
     * @return a panel allowing the user to choose which graphs
     *          to display
     */
    private JPanel createChoosePanel() {
        JPanel chooseGraphsPanel = new JPanel();
        
        chooseGraphsPanel.setLayout(new FlowLayout());
        JLabel selectGraphsLabel = new JLabel(
                        JMeterUtils.getResString("graph_choose_graphs"));
        data = createChooseCheckBox("graph_results_data", Color.black);
        average = createChooseCheckBox("graph_results_average", Color.blue);
        deviation = createChooseCheckBox("graph_results_deviation", Color.red);
        throughput = createChooseCheckBox("graph_results_throughput",
                        JMeterColor.dark_green);

        chooseGraphsPanel.add(selectGraphsLabel);
        chooseGraphsPanel.add(data);
        chooseGraphsPanel.add(average);
        chooseGraphsPanel.add(deviation);
        chooseGraphsPanel.add(throughput);
        return chooseGraphsPanel;
    }

    /**
     * Creates a check box configured to be used to in the choose panel
     * allowing the user to select whether or not a particular kind of
     * graph data will be displayed.
     * 
     * @param labelResourceName the name of the label resource.
     *                This is used to look up the label text using
     *                {@link JMeterUtils#getResString(String)}.
     * @param color  the color used for the checkbox text. By
     *                convention this is the same color that is used
     *                to draw the graph and for the corresponding
     *                info field.
     *
     * @return       a checkbox allowing the user to select whether or
     *                not a kind of graph data will be displayed
     */
    private JCheckBox createChooseCheckBox(String labelResourceName, Color color) {
        JCheckBox checkBox = new JCheckBox(
                        JMeterUtils.getResString(labelResourceName));
        checkBox.setSelected(true);
        checkBox.addItemListener(this);
        checkBox.setForeground(color);
        return checkBox;
    }


    /**
     * Creates a scroll pane containing the actual graph of
     * the results.
     * 
     * @return a scroll pane containing the graph
     */
    private JScrollPane createGraphPanel() {
        JScrollPane graphScrollPanel =
            new JScrollPane(graph, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        graphScrollPanel.setViewportBorder(
                BorderFactory.createEmptyBorder(2, 2, 2, 2));

        return graphScrollPanel;
    }


    /**
     * Creates a panel which numerically displays the current graph
     * values.
     * 
     * @return a panel showing the current graph values
     */
    private Box createGraphInfoPanel() {
        Box graphInfoPanel = Box.createHorizontalBox();

        noSamplesField = createInfoField(Color.black, 6);
        dataField = createInfoField(Color.black, 5);
        averageField = createInfoField(Color.blue, 5);
        deviationField = createInfoField(Color.red, 5);
        throughputField = createInfoField(JMeterColor.dark_green, 15);

        graphInfoPanel.add(createInfoColumn(
                    createInfoLabel("graph_results_no_samples", noSamplesField),
                    noSamplesField,
                    createInfoLabel("graph_results_deviation", deviationField),
                    deviationField));
        graphInfoPanel.add(Box.createHorizontalGlue());

        graphInfoPanel.add(createInfoColumn(
                    createInfoLabel("graph_results_latest_sample", dataField),
                    dataField,
                    createInfoLabel("graph_results_throughput", throughputField),
                    throughputField));
        graphInfoPanel.add(Box.createHorizontalGlue());

        graphInfoPanel.add(createInfoColumn(
                    createInfoLabel("graph_results_average", averageField),
                    averageField,
                    null,
                    null));
        graphInfoPanel.add(Box.createHorizontalGlue());

        return graphInfoPanel;
    }

    /**
     * Creates one of the fields used to display the graph's current
     * values.
     * 
     * @param color   the color used to draw the value. By convention
     *                 this is the same color that is used to draw the
     *                 graph for this value and in the choose panel.
     * @param length  the number of digits which the field should be
     *                 able to display
     * 
     * @return        a text field configured to display one of the
     *                 current graph values
     */
    private JTextField createInfoField(Color color, int length) {
        JTextField field = new JTextField(length);
        field.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        field.setEditable(false);
        field.setForeground(color);
        field.setBackground(getBackground());

        // The text field should expand horizontally, but have
        // a fixed height
        field.setMaximumSize(new Dimension(
                    field.getMaximumSize().width,
                    field.getPreferredSize().height));
        return field;
    }


    /**
     * Creates a label for one of the fields used to display the graph's
     * current values. Neither the label created by this method or the
     * <code>field</code> passed as a parameter is added to the GUI here.
     * 
     * @param labelResourceName  the name of the label resource.
     *                This is used to look up the label text using
     *                {@link JMeterUtils#getResString(String)}.
     * @param field  the field this label is being created for.
     */
    private JLabel createInfoLabel(String labelResourceName, JTextField field) {
        JLabel label = new JLabel(
                JMeterUtils.getResString(labelResourceName));
        label.setForeground(field.getForeground());
        label.setLabelFor(field);
        return label;
    }

    /**
     * Creates a panel containing two pairs of labels and fields for
     * displaying the current graph values. This method exists to help with
     * laying out the fields in columns. If one or more components are null
     * then these components will be represented by blank space.
     * 
     * @param label1  the label for the first field. This label will
     *                 be placed in the upper left section of the panel.
     *                 If this parameter is null, this section of the
     *                 panel will be left blank.
     * @param field1  the field corresponding to the first label. This
     *                 field will be placed in the upper right section
     *                 of the panel. If this parameter is null, this
     *                 section of the panel will be left blank.
     * @param label2  the label for the second field. This label will
     *                 be placed in the lower left section of the panel.
     *                 If this parameter is null, this section of the
     *                 panel will be left blank.
     * @param field2  the field corresponding to the second label. This
     *                 field will be placed in the lower right section
     *                 of the panel. If this parameter is null, this
     *                 section of the panel will be left blank.
     */
    private Box createInfoColumn(JLabel label1, JTextField field1,
            JLabel label2, JTextField field2) {

        // This column actually consists of a row with two sub-columns
        // The first column contains the labels, and the second
        // column contains the fields.
        Box row = Box.createHorizontalBox();		
        Box col = Box.createVerticalBox();
        col.add(label1 != null ? label1 : Box.createVerticalGlue());
        col.add(label2 != null ? label2 : Box.createVerticalGlue());
        row.add(col);

        row.add(Box.createHorizontalStrut(5));

        col = Box.createVerticalBox();
        col.add(field1 != null ? field1 : Box.createVerticalGlue());
        col.add(field2 != null ? field2 : Box.createVerticalGlue());
        row.add(col);

        row.add(Box.createHorizontalStrut(5));

        return row;
    }
}
