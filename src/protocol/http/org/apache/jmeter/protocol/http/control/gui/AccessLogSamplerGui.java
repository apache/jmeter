// $Header$
/*
 * Copyright 2003-2004 The Apache Software Foundation.
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

package org.apache.jmeter.protocol.http.control.gui;

import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.JOptionPane;

import org.apache.jmeter.protocol.http.sampler.AccessLogSampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.gui.UnsharedComponent;
import org.apache.jmeter.gui.util.FilePanel;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.gui.layout.VerticalLayout;

import junit.framework.TestCase;

/**
 * Title:		JMeter Access Log utilities<br>
 * Copyright:	Apache.org<br>
 * Company:		nobody<br>
 * License:<br>
 * <br>
 * Look at the apache license at the top.<br>
 * <br>
 * Description:<br>
 * So what is this log Sampler GUI? It is a sampler that
 * can take Tomcat access logs and use them directly. I
 * wrote a tomcat access log parser to convert each line
 * to a normal HttpSampler. This way, you can stress
 * test your servers using real production traffic. This
 * is useful for a couple of reasons. Some bugs are
 * really hard to track down, which only appear under
 * production traffic. Therefore it is desirable to use
 * the actual queries to simulate the same exact condition
 * to facilitate diagnosis.<p>
 * If you're working on a project to replace an existing
 * site, it is a good way to simulate the same exact
 * use pattern and compare the results. The goal here is
 * to get as close to apples to apples comparison as
 * possible. Running a select subset of queries against
 * a webserver normally catches a lot, but it won't give
 * an accurate picture of how a system will perform
 * under real requests.
 * <br>
 * Created on:  Jun 26, 2003
 *
 * @version $Id$ 
 */
public class AccessLogSamplerGui
    extends AbstractSamplerGui
    implements ChangeListener, UnsharedComponent
{

    JLabeledTextField parserClassName =
        new JLabeledTextField(JMeterUtils.getResString("log_parser"));
	JLabeledTextField generatorClassName =
		new JLabeledTextField(JMeterUtils.getResString("generator"));
	JLabeledTextField HOSTNAME =
		new JLabeledTextField(JMeterUtils.getResString("servername"));
	JLabeledTextField PORT =
		new JLabeledTextField(JMeterUtils.getResString("port"));
    FilePanel logFile =
        new FilePanel(JMeterUtils.getResString("log_file"), ".txt");
	private JCheckBox getImages;

	protected int PORTNUMBER = 80;
	
	public String DEFAULT_GENERATOR =
		"org.apache.jmeter.protocol.http.util.accesslog.StandardGenerator";
	public String DEFAULT_PARSER =
		"org.apache.jmeter.protocol.http.util.accesslog.TCLogParser";
    private AccessLogSampler SAMPLER = null;

    /**
     * This is the font for the note.
     */
    Font plainText = new Font("plain", Font.PLAIN, 10);

	JLabel noteMessage =
		new JLabel(JMeterUtils.getResString("als_message"));
	JLabel noteMessage2 =
		new JLabel(JMeterUtils.getResString("als_message2"));
	JLabel noteMessage3 =
		new JLabel(JMeterUtils.getResString("als_message3"));

    public AccessLogSamplerGui()
    {
        init();
    }

    /**
     * @see org.apache.jmeter.gui.JMeterGUIComponent#getStaticLabel()
     */
    public String getLabelResource()
    {
        return "log_sampler";
    }

    /**
     * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
     */
    public TestElement createTestElement()
    {
    	if (SAMPLER == null){
			SAMPLER = new AccessLogSampler();
			this.configureTestElement(SAMPLER);
			SAMPLER.setParserClassName(parserClassName.getText());
			SAMPLER.setGeneratorClassName(generatorClassName.getText());
			SAMPLER.setLogFile(logFile.getFilename());
			SAMPLER.setDomain(HOSTNAME.getText());
			SAMPLER.setPort(getPortNumber());

    	}
		return SAMPLER;
    }

	/**
	 * Utility method to parse the string and get a int port
	 * number. If it couldn't parse the string to an integer,
	 * it will return the default port 80.
	 * @return port number
	 */
	public int getPortNumber(){
		try {
			int port = Integer.parseInt(PORT.getText());
			return port;
		} catch (NumberFormatException exception){
                        // since we return 80, there's not point
                        // in printing out the stack trace or 
                        // an exception.
			return 80;
		}
	}

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    public void modifyTestElement(TestElement s)
    {
		SAMPLER = (AccessLogSampler) s;
        this.configureTestElement(SAMPLER);
		SAMPLER.setParserClassName(parserClassName.getText());
		SAMPLER.setGeneratorClassName(generatorClassName.getText());
		SAMPLER.setLogFile(logFile.getFilename());
		SAMPLER.setDomain(HOSTNAME.getText());
		SAMPLER.setPort(getPortNumber());
		if (getImages.isSelected()){
			SAMPLER.setImageParser(true);
		} else {
			SAMPLER.setImageParser(false);
		}
    }

    /**
     * init() adds soapAction to the mainPanel. The class
     * reuses logic from SOAPSampler, since it is common.
     */
    private void init()
    {
        this.setLayout(
            new VerticalLayout(5, VerticalLayout.LEFT, VerticalLayout.TOP));

        // MAIN PANEL
        JPanel mainPanel = new JPanel();
        Border margin = new EmptyBorder(10, 10, 5, 10);
        mainPanel.setBorder(margin);
        mainPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));

        // TITLE
        JLabel panelTitleLabel = new JLabel(getStaticLabel());
        Font curFont = panelTitleLabel.getFont();
        int curFontSize = curFont.getSize();
        curFontSize += 4;
        panelTitleLabel.setFont(
            new Font(curFont.getFontName(), curFont.getStyle(), curFontSize));
        mainPanel.add(panelTitleLabel);
        // NAME
        mainPanel.add(getNamePanel());
        mainPanel.add(HOSTNAME);
        mainPanel.add(PORT);

        mainPanel.add(parserClassName);
        mainPanel.add(generatorClassName);
        mainPanel.add(logFile);
        HOSTNAME.addChangeListener(this);
        PORT.addChangeListener(this);
        logFile.addChangeListener(this);
        parserClassName.addChangeListener(this);
        generatorClassName.addChangeListener(this);

		// RETRIEVE IMAGES
		JPanel retrieveImagesPanel = new JPanel();
		getImages =
			new JCheckBox(
				JMeterUtils.getResString("web_testing_retrieve_images"));
		retrieveImagesPanel.add(getImages);
		mainPanel.add(retrieveImagesPanel);
		mainPanel.add(noteMessage);
		mainPanel.add(noteMessage2);
		mainPanel.add(noteMessage3);

        this.add(mainPanel);
    }

    /**
     * the implementation loads the URL and the soap
     * action for the request.
     */
    public void configure(TestElement el)
    {
        super.configure(el);
		SAMPLER = (AccessLogSampler) el;
		if (SAMPLER.getParserClassName().length() > 0){
			parserClassName.setText(SAMPLER.getParserClassName());
		} else {
			parserClassName.setText(this.DEFAULT_PARSER);
		}
        if (SAMPLER.getGeneratorClassName().length() > 0){
			generatorClassName.setText(SAMPLER.getGeneratorClassName());
        } else {
			generatorClassName.setText(this.DEFAULT_GENERATOR);
        }
        logFile.setFilename(SAMPLER.getLogFile());
        HOSTNAME.setText(SAMPLER.getDomain());
        PORT.setText(String.valueOf(SAMPLER.getPort()));
        getImages.setSelected(SAMPLER.isImageParser());

    }
    
    /**
     * stateChanged implements logic for the text field
     * and file chooser. When the value in the widget
     * changes, it will call the corresponding method to
     * create the parser and initialize the generator.
     */
    public void stateChanged(ChangeEvent event)
    {
        if (event.getSource() == parserClassName)
        {
        	SAMPLER.setParserClassName(parserClassName.getText());
        	handleParserEvent();
        }
        if (event.getSource() == logFile)
        {
            //this.setUpGenerator();
        }
        if (event.getSource() == generatorClassName){
        	SAMPLER.setGeneratorClassName(generatorClassName.getText());
        	handleGeneratorEvent();
        }
        if (event.getSource() == HOSTNAME){
        }
        if (event.getSource() == PORT){
        }
    }

	/**
	 * handleParserEvent is used to check the
	 * parser class. If it is not valid, it
	 * should pop up an error message.
	 */    
    public void handleParserEvent(){
		if(!SAMPLER.checkParser()){
			// we should pop up a dialog
			JOptionPane.showConfirmDialog(
				this,
				JMeterUtils.getResString("log_parser_cnf_msg"),
				"Warning",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.ERROR_MESSAGE);
		}
    }

	/**
	 * handleGeneratorEvent is used to check the
	 * generator class. If it is not valid, it
	 * should pop up an error message.
	 */
	public void handleGeneratorEvent(){
		if(!SAMPLER.checkGenerator()){
			// we should pop up a dialog
			JOptionPane.showConfirmDialog(
				this,
				JMeterUtils.getResString("generator_cnf_msg"),
				"Warning",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Added basic TestCase for AccessLogSamplerGui. It does
	 * the same test at HttpTestSampleGui.java.
	 */
	public static class Test extends TestCase
	{
		AccessLogSamplerGui gui;
        
		public Test(String name)
		{
			super(name);
		}
        
		public void setUp()
		{
			gui = new AccessLogSamplerGui();
		}
        
		public void testCloneSampler() throws Exception
		{
			AccessLogSampler sampler = (AccessLogSampler)gui.createTestElement();
			sampler.addArgument("param","value");
			AccessLogSampler clonedSampler = (AccessLogSampler)sampler.clone();
			clonedSampler.setRunningVersion(true);
			sampler.getArguments().getArgument(0).setValue("new value");
			assertEquals(
				"Sampler didn't clone correctly",
				"new value",
				sampler.getArguments().getArgument(0).getValue());
		}
	}

}
