/*
 * Copyright 2005 The Apache Software Foundation.
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

package org.apache.jmeter.assertions.gui;

import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.XPathUtil;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * author jspears
 *
 */
public class XPathPanel extends JPanel {
	private static Document testDoc = null;
	private final static  Log log = LogFactory.getLog(XPathPanel.class);
	private JCheckBox negated;
	private JTextField xpath;
	private JButton checkXPath;
	
	/**
	 * 
	 */
	public XPathPanel() {
		super();
		init();
	}
	
	/**
	 * @param isDoubleBuffered
	 */
	public XPathPanel(boolean isDoubleBuffered) {
		super( isDoubleBuffered);
	}
	
	/**
	 * @param layout
	 */
	public XPathPanel(LayoutManager layout) {
		super(layout);
	}
	/**
	 * @param layout
	 * @param isDoubleBuffered
	 */
	public XPathPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
	
	}
	private void init() {
		Box hbox = Box.createHorizontalBox();
		hbox.add(Box.createHorizontalGlue());
		hbox.add(getXPathTextField());
		hbox.add(Box.createHorizontalGlue());
		hbox.add(getCheckXPathButton());
		
		Box vbox = Box.createVerticalBox();
		vbox.add(hbox);
		vbox.add(Box.createVerticalGlue());
		vbox.add(getNegatedCheckBox());
		
		add(vbox);
	}
	/**
	 * Get the XPath String 
	 * @return String
	 */
	public String getXPath() {
		return this.xpath.getText();
	}
	/**
	 * Set the string that will be used in the xpath evaluation
	 * @param xpath
	 */
	public void setXPath(String xpath) {
		this.xpath.setText(xpath);
	}
	/**
	 * Does this negate the xpath results
	 * @return boolean
	 */
	public boolean isNegated() {
		return this.negated.isSelected();
	}
	/**
	 * Set this to true, if you want success when the xpath
	 * does not match.  
	 * @param negated
	 */
	public void setNegated(boolean negated) {
		this.negated.setSelected(negated);
	}
	/**
	 * Negated chechbox
	 * @return JCheckBox
	 */
	public JCheckBox getNegatedCheckBox() {
	    if (negated == null ) {
	    	negated	= new JCheckBox(JMeterUtils.getResString("xpath_assertion_negate"), false);			
	    }

	    return negated;
	}
	/**
	 * Check XPath button
	 * @return JButton
	 */
	public JButton getCheckXPathButton() {
		if (checkXPath == null) {
			checkXPath = new JButton(JMeterUtils.getResString("xpath_assertion_button"));
			checkXPath.addActionListener(new ActionListener() {
		    	public void actionPerformed(ActionEvent e) {
					validXPath(xpath.getText(), true);
				}});   
		}
		return checkXPath;
	}
	public JTextField getXPathTextField() {
		if (xpath == null ) {
			xpath = new JTextField(50);
			xpath.setText("/");
		}
		return xpath;
	}
	/**
	 * @return Returns the showNegate.
	 */
	public boolean isShowNegated() {
		return this.getNegatedCheckBox().isVisible();
	}
	/**
	 * @param showNegate The showNegate to set.
	 */
	public void setShowNegated(boolean showNegate) {
		getNegatedCheckBox().setVisible(showNegate);
	}
	
	/**
	 * Test weather an XPath is valid.  It seems the Xalan has no
	 * easy way to check, so this creates a test document, then
	 * tries to evaluate the xpath.   
	 * 
	 * @param xpathString XPath String to validate
	 * @param showDialog weather to show a dialog
	 * @return returns true if valid, valse otherwise.
	 */
	public static boolean validXPath(String xpathString, boolean showDialog)  {
		String ret = null;
		boolean success= true;
		try {
			if (testDoc == null){
				testDoc = XPathUtil.makeDocumentBuilder(false,false,false).newDocument();
				Element el = testDoc.createElement("root");
				testDoc.appendChild(el);
					
			}
			if(XPathAPI.eval(testDoc, xpathString) == null){
				//We really should never get here
				// because eval will throw an exception
				// if xpath is invalid, but whatever, better
				// safe
				log.warn("xpath eval was null ");
				ret ="xpath eval was null";
				success = false;
			}
	
		} catch (ParserConfigurationException e) {
			success = false;
			ret = e.getLocalizedMessage();
		} catch (TransformerException e) {
			success = false;
			ret =e.getLocalizedMessage();
	
		} catch (SAXException e) {
			success = false;
			ret =e.getLocalizedMessage();
		} 
		
		if(showDialog){
			JOptionPane.showMessageDialog(
                null,
				(success) ? JMeterUtils.getResString("xpath_assertion_valid") : ret,
				(success) ? JMeterUtils.getResString("xpath_assertion_valid") 
						  : JMeterUtils.getResString("xpath_assertion_failed"),
                (success) ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE );
	    }
		return success;
		
	}
	/**
	 * Just a simple main to run this panel, It would be nice
	 * if it loaded up all the stuff for JMeterUtil but for 
	 * a little test it is overkill.
	 * @param args
	 */
	public static void main (String[] args) {
		JMeterUtils.setJMeterHome("/eclipse/workspace/jakarta-jmeter/bin");
		JFrame frame = new JFrame();
		frame.add(new XPathPanel());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);	
	}	
}