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

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;


/**
 * @author    Michael Stover
 * @version   $Revision$
 */
public class AssertionVisualizer extends AbstractVisualizer implements Clearable
{

    private JTextArea textArea;

    public AssertionVisualizer()
    {
        init();
        setName(getStaticLabel());
    }

    public String getStaticLabel()
    {
        return JMeterUtils.getResString("assertion_visualizer_title");
    }

    public void add(SampleResult sample)
    {
    	StringBuffer sb = new StringBuffer(100);
    	String sd= sample.getSamplerData();
        if(null != sd)
        {
            sb.append(sd);
        }
        else
        {
            sb.append(sample.getSampleLabel());
        }
        sb.append(getAssertionResult(sample));
        sb.append("\n");
        synchronized(textArea){
        	textArea.append(sb.toString());
        }
    }

    public void clear()
    {
        textArea.setText("");
    }

    private String getAssertionResult(SampleResult res)
    {
        if (res != null)
        {
            StringBuffer display = new StringBuffer();
            AssertionResult assertionResults[] = res.getAssertionResults();
            if (assertionResults != null)
            {
                for (int i = 0; i < assertionResults.length; i++)
                {
                    AssertionResult item = assertionResults[i];

                    if (item.isFailure() || item.isError())
                    {
                        display.append("\n\t\t");
                        display.append(item.getFailureMessage());
                    }
                }
            }
            return display.toString();
        }
        return "";
    }

    private void init()
    {
        this.setLayout(new BorderLayout());

        // MAIN PANEL
        Border margin = new EmptyBorder(10, 10, 5, 10);

        this.setBorder(margin);


        // NAME
        this.add(makeTitlePanel(),BorderLayout.NORTH);

        // TEXTAREA LABEL
        JLabel textAreaLabel =
            new JLabel(JMeterUtils.getResString("assertion_textarea_label"));
        Box mainPanel = Box.createVerticalBox();
        mainPanel.add(textAreaLabel);

        // TEXTAREA
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(false);
        JScrollPane areaScrollPane = new JScrollPane(textArea);

        areaScrollPane.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        areaScrollPane.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        mainPanel.add(areaScrollPane);
        mainPanel.add(Box.createVerticalGlue());
        this.add(mainPanel, BorderLayout.CENTER);
    }
}
