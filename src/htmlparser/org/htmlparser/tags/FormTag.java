// $Header$
/*
 * ====================================================================
 * Copyright 2002-2004 The Apache Software Foundation.
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

// The developers of JMeter and Apache are greatful to the developers
// of HTMLParser for giving Apache Software Foundation a non-exclusive
// license. The performance benefits of HTMLParser are clear and the
// users of JMeter will benefit from the hard work the HTMLParser
// team. For detailed information about HTMLParser, the project is
// hosted on sourceforge at http://htmlparser.sourceforge.net/.
//
// HTMLParser was originally created by Somik Raha in 2000. Since then
// a healthy community of users has formed and helped refine the
// design so that it is able to tackle the difficult task of parsing
// dirty HTML. Derrick Oswald is the current lead developer and was kind
// enough to assist JMeter.
package org.htmlparser.tags;

import org.htmlparser.tags.data.CompositeTagData;
import org.htmlparser.tags.data.TagData;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.SimpleNodeIterator;

/**
 * @author ili
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
/**
 * Represents a FORM tag.
 */
public class FormTag extends CompositeTag {
	public static final String POST = "POST";

	public static final String GET = "GET";

	protected String formURL;

	protected String formName;

	protected String formMethod;

	protected NodeList formInputList;

	private NodeList textAreaList;

	/**
	 * Constructor takes in tagData, compositeTagData, formTagData
	 * 
	 * @param tagData
	 * @param compositeTagData
	 */
	public FormTag(TagData tagData, CompositeTagData compositeTagData) {
		super(tagData, compositeTagData);

		this.formURL = compositeTagData.getStartTag().getAttribute("ACTION");
		this.formName = compositeTagData.getStartTag().getAttribute("NAME");
		this.formMethod = compositeTagData.getStartTag().getAttribute("METHOD");
		this.formInputList = compositeTagData.getChildren().searchFor(InputTag.class);
		this.textAreaList = compositeTagData.getChildren().searchFor(TextareaTag.class);
	}

	/**
	 * @return Vector Input elements in the form
	 */
	public NodeList getFormInputs() {
		return formInputList;
	}

	/**
	 * @return String The url of the form
	 */
	public String getFormLocation() {
		return formURL;
	}

	/**
	 * Returns the method of the form
	 * 
	 * @return String The method of the form (GET if nothing is specified)
	 */
	public String getFormMethod() {
		if (formMethod == null) {
			formMethod = "GET";
		}
		return formMethod;
	}

	/**
	 * Get the input tag in the form corresponding to the given name
	 * 
	 * @param name
	 *            The name of the input tag to be retrieved
	 * @return Tag The input tag corresponding to the name provided
	 */
	public InputTag getInputTag(String name) {
		InputTag inputTag = null;
		boolean found = false;
		for (SimpleNodeIterator e = formInputList.elements(); e.hasMoreNodes() && !found;) {
			inputTag = (InputTag) e.nextNode();
			String inputTagName = inputTag.getAttribute("NAME");
			if (inputTagName != null && inputTagName.equalsIgnoreCase(name)) {
				found = true;
			}
		}
		if (found)
			return inputTag;
		else
			return null;
	}

	/**
	 * @return String The name of the form
	 */
	public String getFormName() {
		return formName;
	}

	/**
	 * Set the form location. Modification of this element will cause the HTML
	 * rendering to change as well (in a call to toHTML()).
	 * 
	 * @param formURL
	 *            The new FORM location
	 */
	public void setFormLocation(String formURL) {
		attributes.put("ACTION", formURL);
		this.formURL = formURL;
	}

	/**
	 * @return String The contents of the FormTag
	 */
	public String toString() {
		return "FORM TAG : Form at " + formURL + "; begins at : " + elementBegin() + "; ends at : " + elementEnd();
	}

	/**
	 * Find the textarea tag matching the given name
	 * 
	 * @param name
	 *            Name of the textarea tag to be found within the form
	 */
	public TextareaTag getTextAreaTag(String name) {
		TextareaTag textareaTag = null;
		boolean found = false;
		for (SimpleNodeIterator e = textAreaList.elements(); e.hasMoreNodes() && !found;) {
			textareaTag = (TextareaTag) e.nextNode();
			String textAreaName = textareaTag.getAttribute("NAME");
			if (textAreaName != null && textAreaName.equals(name)) {
				found = true;
			}
		}
		if (found)
			return textareaTag;
		else
			return null;
	}

}
