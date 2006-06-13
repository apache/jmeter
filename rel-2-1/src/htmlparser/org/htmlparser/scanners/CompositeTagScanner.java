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
package org.htmlparser.scanners;

import java.util.HashSet;
import java.util.Set;

import org.htmlparser.Node;
import org.htmlparser.NodeReader;
import org.htmlparser.parserHelper.CompositeTagScannerHelper;
import org.htmlparser.tags.EndTag;
import org.htmlparser.tags.Tag;
import org.htmlparser.tags.data.CompositeTagData;
import org.htmlparser.tags.data.TagData;
import org.htmlparser.util.ParserException;

/**
 * To create your own scanner that can hold children, create a subclass of this
 * class. The composite tag scanner can be configured with:<br>
 * <ul>
 * <li>Tags which will trigger a match</li>
 * <li>Tags which when encountered before a legal end tag, should force a
 * correction</li>
 * <li>Preventing more tags of its own type to appear as children
 * </ul>
 * Here are examples of each:<BR>
 * <B>Tags which will trigger a match</B> If we wish to recognize
 * &lt;mytag&gt;,
 * 
 * <pre>
 * 
 *  MyScanner extends CompositeTagScanner {
 *    private static final String [] MATCH_IDS = { &quot;MYTAG&quot; };
 * 	 MyScanner() {
 * 		super(MATCH_IDS);
 * 	 }
 * 	 ...
 *  }
 *  
 * </pre>
 * 
 * <B>Tags which force correction</B> If we wish to insert end tags if we get a
 * </BODY> or </HTML> without recieving &lt;/mytag&gt;
 * 
 * <pre>
 * 
 *  MyScanner extends CompositeTagScanner {
 *    private static final String [] MATCH_IDS = { &quot;MYTAG&quot; };
 *    private static final String [] ENDERS = {};
 *    private static final String [] END_TAG_ENDERS = { &quot;BODY&quot;, &quot;HTML&quot; };
 * 	 MyScanner() {
 * 		super(MATCH_IDS, ENDERS, END_TAG_ENDERS, true);
 * 	 }
 * 	 ...
 *  }
 *  
 * </pre>
 * 
 * <B>Preventing children of same type</B> This is useful when you know that a
 * certain tag can never hold children of its own type. e.g. &lt;FORM&gt; can
 * never have more form tags within it. If it does, it is an error and should be
 * corrected. The default behavior is to allow nesting.
 * 
 * <pre>
 * 
 *  MyScanner extends CompositeTagScanner {
 *    private static final String [] MATCH_IDS = { &quot;FORM&quot; };
 *    private static final String [] ENDERS = {};
 *    private static final String [] END_TAG_ENDERS = { &quot;BODY&quot;, &quot;HTML&quot; };
 * 	 MyScanner() {
 * 		super(MATCH_IDS, ENDERS,END_TAG_ENDERS, false);
 * 	 }
 * 	 ...
 *  }
 *  
 * </pre>
 * 
 * Inside the scanner, use createTag() to specify what tag needs to be created.
 */
public abstract class CompositeTagScanner extends TagScanner {
	protected String[] nameOfTagToMatch;

	private boolean allowSelfChildren;

	private Set tagEnderSet;

	private Set endTagEnderSet;

	private boolean balance_quotes;

	public CompositeTagScanner(String[] nameOfTagToMatch) {
		this(nameOfTagToMatch, new String[] {});
	}

	public CompositeTagScanner(String[] nameOfTagToMatch, String[] tagEnders) {
		this("", nameOfTagToMatch, tagEnders);
	}

	public CompositeTagScanner(String[] nameOfTagToMatch, String[] tagEnders, boolean allowSelfChildren) {
		this("", nameOfTagToMatch, tagEnders, allowSelfChildren);
	}

	public CompositeTagScanner(String filter, String[] nameOfTagToMatch) {
		this(filter, nameOfTagToMatch, new String[] {}, true);
	}

	public CompositeTagScanner(String filter, String[] nameOfTagToMatch, String[] tagEnders) {
		this(filter, nameOfTagToMatch, tagEnders, true);
	}

	public CompositeTagScanner(String filter, String[] nameOfTagToMatch, String[] tagEnders, boolean allowSelfChildren) {
		this(filter, nameOfTagToMatch, tagEnders, new String[] {}, allowSelfChildren);
	}

	public CompositeTagScanner(String filter, String[] nameOfTagToMatch, String[] tagEnders, String[] endTagEnders,
			boolean allowSelfChildren) {
		this(filter, nameOfTagToMatch, tagEnders, endTagEnders, allowSelfChildren, false);
	}

	/**
	 * Constructor specifying all member fields.
	 * 
	 * @param filter
	 *            A string that is used to match which tags are to be allowed to
	 *            pass through. This can be useful when one wishes to
	 *            dynamically filter out all tags except one type which may be
	 *            programmed later than the parser.
	 * @param nameOfTagToMatch
	 *            The tag names recognized by this scanner.
	 * @param tagEnders
	 *            The non-endtag tag names which signal that no closing end tag
	 *            was found. For example, encountering &lt;FORM&gt; while
	 *            scanning a &lt;A&gt; link tag would mean that no &lt;/A&gt;
	 *            was found and needs to be corrected.
	 * @param endTagEnders
	 *            The endtag names which signal that no closing end tag was
	 *            found. For example, encountering &lt;/HTML&gt; while scanning
	 *            a &lt;BODY&gt; tag would mean that no &lt;/BODY&gt; was found
	 *            and needs to be corrected. These items are not prefixed by a
	 *            '/'.
	 * @param allowSelfChildren
	 *            If <code>true</code> a tag of the same name is allowed
	 *            within this tag. Used to determine when an endtag is missing.
	 * @param balance_quotes
	 *            <code>true</code> if scanning string nodes needs to honour
	 *            quotes. For example, ScriptScanner defines this
	 *            <code>true</code> so that text within
	 *            &lt;SCRIPT&gt;&lt;/SCRIPT&gt; ignores tag-like text within
	 *            quotes.
	 */
	public CompositeTagScanner(String filter, String[] nameOfTagToMatch, String[] tagEnders, String[] endTagEnders,
			boolean allowSelfChildren, boolean balance_quotes) {
		super(filter);
		this.nameOfTagToMatch = nameOfTagToMatch;
		this.allowSelfChildren = allowSelfChildren;
		this.balance_quotes = balance_quotes;
		this.tagEnderSet = new HashSet();
		for (int i = 0; i < tagEnders.length; i++)
			tagEnderSet.add(tagEnders[i]);
		this.endTagEnderSet = new HashSet();
		for (int i = 0; i < endTagEnders.length; i++)
			endTagEnderSet.add(endTagEnders[i]);
	}

	public Tag scan(Tag tag, String url, NodeReader reader, String currLine) throws ParserException {
		CompositeTagScannerHelper helper = new CompositeTagScannerHelper(this, tag, url, reader, currLine,
				balance_quotes);
		return helper.scan();
	}

	/**
	 * Override this method if you wish to create any data structures or do
	 * anything before the start of the scan. This is just after a tag has
	 * triggered the scanner but before the scanner begins its processing.
	 */
	public void beforeScanningStarts() {
	}

	/**
	 * This method is called everytime a child to the composite is found. It is
	 * useful when we need to store special children seperately. Though, all
	 * children are collected anyway into a node list.
	 */
	public void childNodeEncountered(Node node) {
	}

	/**
	 * You must override this method to create the tag of your choice upon
	 * successful parsing. Data required for construction of your tag can be
	 * found within tagData and compositeTagData
	 */
	public abstract Tag createTag(TagData tagData, CompositeTagData compositeTagData) throws ParserException;

	public final boolean isTagToBeEndedFor(Tag tag)

	{
		boolean isEndTag = tag instanceof EndTag;
		String tagName = tag.getTagName();
		if ((isEndTag && endTagEnderSet.contains(tagName)) || (!isEndTag && tagEnderSet.contains(tagName)))
			return true;
		else
			return false;
	}

	public final boolean isAllowSelfChildren() {
		return allowSelfChildren;
	}

	/**
	 * Override this method to implement scanner logic that determines if the
	 * current scanner is to be allowed. This is useful when there are rules
	 * which dont allow recursive tags of the same type.
	 * 
	 * @see BulletScanner
	 * @return boolean true/false
	 */
	public boolean shouldCreateEndTagAndExit() {
		return false;
	}
}
