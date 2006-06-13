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

import java.util.Enumeration;
import java.util.Hashtable;

import org.htmlparser.Node;
import org.htmlparser.tags.data.CompositeTagData;
import org.htmlparser.tags.data.TagData;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.SimpleNodeIterator;

/**
 * HTMLAppletTag represents an &lt;Applet&gt; tag
 */
public class AppletTag extends CompositeTag {
	private java.lang.String codeBase;

	private java.lang.String archive;

	private java.lang.String appletClass;

	private Hashtable appletParams;

	/**
	 * HTMLAppletTag constructor comment.
	 * 
	 * @param nodeBegin
	 *            int
	 * @param nodeEnd
	 *            int
	 * @param tagContents
	 *            java.lang.String
	 * @param tagLine
	 *            java.lang.String
	 */
	public AppletTag(TagData tagData, CompositeTagData compositeTagData) {
		super(tagData, compositeTagData);
		this.appletClass = compositeTagData.getStartTag().getAttribute("CODE");
		this.codeBase = compositeTagData.getStartTag().getAttribute("CODEBASE");
		this.archive = compositeTagData.getStartTag().getAttribute("ARCHIVE");
		NodeList children = compositeTagData.getChildren();
		appletParams = new Hashtable();
		createAppletParamsTable(children);
	}

	public void createAppletParamsTable(NodeList children) {
		for (int i = 0; i < children.size(); i++) {
			Node node = children.elementAt(i);
			if (node instanceof Tag) {
				Tag tag = (Tag) node;
				if (tag.getTagName().equals("PARAM")) {
					String paramName = tag.getAttribute("NAME");
					if (paramName != null && paramName.length() != 0) {
						String paramValue = tag.getAttribute("VALUE");
						appletParams.put(paramName, paramValue);
					}
				}
			}
		}
	}

	public java.lang.String getAppletClass() {
		return appletClass;
	}

	public Hashtable getAppletParams() {
		return appletParams;
	}

	public java.lang.String getArchive() {
		return archive;
	}

	public java.lang.String getCodeBase() {
		return codeBase;
	}

	public String getAttribute(String key) {
		return (String) appletParams.get(key);
	}

	public Enumeration getParameterNames() {
		return appletParams.keys();
	}

	public void setAppletClass(java.lang.String newAppletClass) {
		appletClass = newAppletClass;
	}

	public void setAppletParams(Hashtable newAppletParams) {
		appletParams = newAppletParams;
	}

	public void setArchive(java.lang.String newArchive) {
		archive = newArchive;
	}

	public void setCodeBase(java.lang.String newCodeBase) {
		codeBase = newCodeBase;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Applet Tag\n");
		sb.append("**********\n");
		sb.append("Class Name = " + appletClass + "\n");
		sb.append("Archive = " + archive + "\n");
		sb.append("Codebase = " + codeBase + "\n");
		Enumeration params = appletParams.keys();
		if (params == null)
			sb.append("No Params found.\n");
		else {
			int cnt = 0;
			for (; params.hasMoreElements();) {
				String paramName = (String) params.nextElement();
				String paramValue = (String) appletParams.get(paramName);
				sb.append((cnt++) + ": Parameter name = " + paramName + ", Parameter value = " + paramValue + "\n");
			}
		}
		if (children() == null)
			sb.append("No Miscellaneous items\n");
		else {
			sb.append("Miscellaneous items :\n");
			for (SimpleNodeIterator e = children(); e.hasMoreNodes();) {
				sb.append(((Tag) e.nextNode()).toString());
			}
		}
		sb.append("End of Applet Tag\n");
		sb.append("*****************\n");
		return sb.toString();
	}
}
