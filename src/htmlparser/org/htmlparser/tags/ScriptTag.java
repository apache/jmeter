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

/**
 * A HTMLScriptTag represents a JavaScript node
 */
public class ScriptTag extends CompositeTag {
	private java.lang.String language;

	private java.lang.String type;

	private String scriptCode;

	/**
	 * The HTMLScriptTag is constructed by providing the beginning posn, ending
	 * posn and the tag contents.
	 * 
	 * @param nodeBegin
	 *            beginning position of the tag
	 * @param nodeEnd
	 *            ending position of the tag
	 * @param tagContents
	 *            The contents of the Script Tag (should be kept the same as
	 *            that of the original Tag contents)
	 * @param scriptCode
	 *            The Javascript code b/w the tags
	 * @param language
	 *            The language parameter
	 * @param type
	 *            The type parameter
	 * @param tagLine
	 *            The current line being parsed, where the tag was found
	 */
	public ScriptTag(TagData tagData, CompositeTagData compositeTagData) {
		super(tagData, compositeTagData);
		this.scriptCode = getChildrenHTML();
		this.language = getAttribute("LANGUAGE");
		this.type = getAttribute("TYPE");
	}

	public java.lang.String getLanguage() {
		return language;
	}

	public java.lang.String getScriptCode() {
		return scriptCode;
	}

	public java.lang.String getType() {
		return type;
	}

	/**
	 * Set the language of the javascript tag
	 * 
	 * @param newLanguage
	 *            java.lang.String
	 */
	public void setLanguage(java.lang.String newLanguage) {
		language = newLanguage;
	}

	/**
	 * Set the type of the javascript node
	 * 
	 * @param newType
	 *            java.lang.String
	 */
	public void setType(java.lang.String newType) {
		type = newType;
	}

	/**
	 * Print the contents of the javascript node
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Script Node : \n");
		if (language != null && type != null)
			if (language.length() != 0 || type.length() != 0) {
				sb.append("Properties -->\n");
				if (language.length() != 0)
					sb.append("[Language : " + language + "]\n");
				if (type != null && type.length() != 0)
					sb.append("[Type : " + type + "]\n");
			}
		sb.append("\n");
		sb.append("Code\n");
		sb.append("****\n");
		sb.append(getScriptCode() + "\n");
		return sb.toString();
	}
}
