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

import org.htmlparser.Node;
import org.htmlparser.tags.data.CompositeTagData;
import org.htmlparser.tags.data.LinkData;
import org.htmlparser.tags.data.TagData;
import org.htmlparser.util.SimpleNodeIterator;
import org.htmlparser.visitors.NodeVisitor;

/**
 * Identifies a link tag
 */
public class LinkTag extends CompositeTag {
	public static final String LINK_TAG_FILTER = "-l";

	/**
	 * The URL where the link points to
	 */
	protected String link;

	/**
	 * The text of of the link element
	 */
	protected String linkText;

	/**
	 * The accesskey existing inside this link.
	 */
	protected String accessKey;

	private boolean mailLink;

	private boolean javascriptLink;

	/**
	 * Constructor creates an HTMLLinkNode object, which basically stores the
	 * location where the link points to, and the text it contains.
	 * <p>
	 * In order to get the contents of the link tag, use the method linkData(),
	 * which returns an enumeration of nodes encapsulated within the link.
	 * <p>
	 * The following code will get all the images inside a link tag.
	 * 
	 * <pre>
	 * Node node;
	 * ImageTag imageTag;
	 * for (Enumeration e = linkTag.linkData(); e.hasMoreElements();) {
	 * 	node = (Node) e.nextElement();
	 * 	if (node instanceof ImageTag) {
	 * 		imageTag = (ImageTag) node;
	 * 		// Process imageTag
	 * 	}
	 * }
	 * </pre>
	 * 
	 * There is another mechanism available that allows for uniform extraction
	 * of images. You could do this to get all images from a web page :
	 * 
	 * <pre>
	 * Node node;
	 * Vector imageCollectionVector = new Vector();
	 * for (NodeIterator e = parser.elements(); e.hasMoreNode();) {
	 * 	node = e.nextHTMLNode();
	 * 	node.collectInto(imageCollectionVector, ImageTag.IMAGE_FILTER);
	 * }
	 * </pre>
	 * 
	 * The link tag processes all its contents in collectInto().
	 * 
	 * @param tagData
	 *            The data relating to the tag.
	 * @param compositeTagData
	 *            The data regarding the composite structure of the tag.
	 * @param linkData
	 *            The data specific to the link tag.
	 * @see #linkData()
	 */
	public LinkTag(TagData tagData, CompositeTagData compositeTagData, LinkData linkData) {
		super(tagData, compositeTagData);
		this.link = linkData.getLink();
		this.linkText = linkData.getLinkText();
		this.accessKey = linkData.getAccessKey();
		this.mailLink = linkData.isMailLink();
		this.javascriptLink = linkData.isJavascriptLink();
	}

	/**
	 * Returns the accesskey element if any inside this link tag
	 */
	public String getAccessKey() {
		return accessKey;
	}

	/**
	 * Returns the url as a string, to which this link points
	 */
	public String getLink() {
		return link;
	}

	/**
	 * Returns the text contained inside this link tag
	 */
	public String getLinkText() {
		return linkText;
	}

	/**
	 * Return the text contained in this linkinode Kaarle Kaila 23.10.2001
	 */
	public String getText() {
		return toHtml();
	}

	/**
	 * Is this a mail address
	 * 
	 * @return boolean true/false
	 */
	public boolean isMailLink() {
		return mailLink;
	}

	/**
	 * Tests if the link is javascript
	 * 
	 * @return flag indicating if the link is a javascript code
	 */
	public boolean isJavascriptLink() {
		return javascriptLink;
	}

	/**
	 * Tests if the link is an FTP link.
	 * 
	 * @return flag indicating if this link is an FTP link
	 */
	public boolean isFTPLink() {
		return link.indexOf("ftp://") == 0;
	}

	/**
	 * Tests if the link is an HTTP link.
	 * 
	 * @return flag indicating if this link is an HTTP link
	 */
	public boolean isHTTPLink() {
		return (!isFTPLink() && !isHTTPSLink() && !isJavascriptLink() && !isMailLink());
	}

	/**
	 * Tests if the link is an HTTPS link.
	 * 
	 * @return flag indicating if this link is an HTTPS link
	 */
	public boolean isHTTPSLink() {
		return link.indexOf("https://") == 0;
	}

	/**
	 * Tests if the link is an HTTP link or one of its variations (HTTPS, etc.).
	 * 
	 * @return flag indicating if this link is an HTTP link or one of its
	 *         variations (HTTPS, etc.)
	 */
	public boolean isHTTPLikeLink() {
		return isHTTPLink() || isHTTPSLink();
	}

	/**
	 * Insert the method's description here. Creation date: (8/3/2001 1:49:31
	 * AM)
	 * 
	 * @param newMailLink
	 *            boolean
	 */
	public void setMailLink(boolean newMailLink) {
		mailLink = newMailLink;
	}

	/**
	 * Set the link as a javascript link.
	 * 
	 * @param newJavascriptLink
	 *            flag indicating if the link is a javascript code
	 */
	public void setJavascriptLink(boolean newJavascriptLink) {
		javascriptLink = newJavascriptLink;
	}

	/**
	 * Print the contents of this Link Node
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Link to : " + link + "; titled : " + linkText + "; begins at : " + elementBegin() + "; ends at : "
				+ elementEnd() + ", AccessKey=");
		if (accessKey == null)
			sb.append("null\n");
		else
			sb.append(accessKey + "\n");
		if (children() != null) {
			sb.append("  " + "LinkData\n");
			sb.append("  " + "--------\n");

			Node node;
			int i = 0;
			for (SimpleNodeIterator e = children(); e.hasMoreNodes();) {
				node = e.nextNode();
				sb.append("   " + (i++) + " ");
				sb.append(node.toString() + "\n");
			}
		}
		sb.append("  " + "*** END of LinkData ***\n");
		return sb.toString();
	}

	public void setLink(String link) {
		this.link = link;
		attributes.put("HREF", link);
	}

	/**
	 * This method returns an enumeration of data that it contains
	 * 
	 * @return Enumeration
	 * @deprecated Use children() instead.
	 */
	public SimpleNodeIterator linkData() {
		return children();
	}

	public void accept(NodeVisitor visitor) {
		visitor.visitLinkTag(this);
		super.accept(visitor);
	}

	public void removeChild(int i) {
		childTags.remove(i);
	}

}
