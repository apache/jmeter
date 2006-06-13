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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;

import org.htmlparser.Node;
import org.htmlparser.NodeReader;
import org.htmlparser.parserHelper.AttributeParser;
import org.htmlparser.parserHelper.TagParser;
import org.htmlparser.scanners.TagScanner;
import org.htmlparser.tags.data.TagData;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.NodeVisitor;

/**
 * Tag represents a generic tag. This class allows users to register specific
 * tag scanners, which can identify links, or image references. This tag asks
 * the scanners to run over the text, and identify. It can be used to
 * dynamically configure a parser.
 * 
 * @author Kaarle Kaila 23.10.2001
 */
public class Tag extends Node {
	private static final String TYPE = "TAG";

	/**
	 * Constant used as value for the value of the tag name in parseParameters
	 * (Kaarle Kaila 3.8.2001)
	 */
	public final static String TAGNAME = "$<TAGNAME>$";

	public final static String EMPTYTAG = "$<EMPTYTAG>$";

	private final static int TAG_BEFORE_PARSING_STATE = 1;

	private final static int TAG_BEGIN_PARSING_STATE = 2;

	private final static int TAG_FINISHED_PARSING_STATE = 3;

	private final static int TAG_ILLEGAL_STATE = 4;

	private final static int TAG_IGNORE_DATA_STATE = 5;

	private final static int TAG_IGNORE_BEGIN_TAG_STATE = 6;

	private final static String EMPTY_STRING = "";

	private static AttributeParser paramParser = new AttributeParser();

	private static TagParser tagParser;

	/**
	 * Tag contents will have the contents of the comment tag.
	 */
	protected StringBuffer tagContents;

	private boolean emptyXmlTag = false;

	/**
	 * tag parameters parsed into this hashtable not implemented yet added by
	 * Kaarle Kaila 23.10.2001
	 */
	protected Hashtable attributes = null;

	/**
	 * Scanner associated with this tag (useful for extraction of filtering data
	 * from a HTML node)
	 */
	protected TagScanner thisScanner = null;

	private java.lang.String tagLine;

	/**
	 * The combined text of all the lines spanned by this tag
	 */
	private String[] tagLines;

	/**
	 * The line number on which this tag starts
	 */
	private int startLine;

	/**
	 * Set of tags that breaks the flow.
	 */
	protected static HashSet breakTags;
	static {
		breakTags = new HashSet(30);
		breakTags.add("BLOCKQUOTE");
		breakTags.add("BODY");
		breakTags.add("BR");
		breakTags.add("CENTER");
		breakTags.add("DD");
		breakTags.add("DIR");
		breakTags.add("DIV");
		breakTags.add("DL");
		breakTags.add("DT");
		breakTags.add("FORM");
		breakTags.add("H1");
		breakTags.add("H2");
		breakTags.add("H3");
		breakTags.add("H4");
		breakTags.add("H5");
		breakTags.add("H6");
		breakTags.add("HEAD");
		breakTags.add("HR");
		breakTags.add("HTML");
		breakTags.add("ISINDEX");
		breakTags.add("LI");
		breakTags.add("MENU");
		breakTags.add("NOFRAMES");
		breakTags.add("OL");
		breakTags.add("P");
		breakTags.add("PRE");
		breakTags.add("TD");
		breakTags.add("TH");
		breakTags.add("TITLE");
		breakTags.add("UL");
	}

	/**
	 * Set the Tag with the beginning posn, ending posn and tag contents (in a
	 * tagData object.
	 * 
	 * @param tagData
	 *            The data for this tag
	 */
	public Tag(TagData tagData) {
		super(tagData.getTagBegin(), tagData.getTagEnd());
		this.startLine = tagData.getStartLine();
		this.tagContents = new StringBuffer();
		this.tagContents.append(tagData.getTagContents());
		this.tagLine = tagData.getTagLine();
		this.tagLines = new String[] { tagData.getTagLine() };
		this.emptyXmlTag = tagData.isEmptyXmlTag();
	}

	public void append(char ch) {
		tagContents.append(ch);
	}

	public void append(String ch) {
		tagContents.append(ch);
	}

	/**
	 * Locate the tag withing the input string, by parsing from the given
	 * position
	 * 
	 * @param reader
	 *            HTML reader to be provided so as to allow reading of next line
	 * @param input
	 *            Input String
	 * @param position
	 *            Position to start parsing from
	 */
	public static Tag find(NodeReader reader, String input, int position) {
		return tagParser.find(reader, input, position);
	}

	/**
	 * This method is not to be called by any scanner or tag. It is an expensive
	 * method, hence it has been made private. However, there might be some
	 * circumstances when a scanner wishes to force parsing of attributes over
	 * and above what has already been parsed. To make the choice clear - we
	 * have a method - redoParseAttributes(), which can be used.
	 * 
	 * @return Hashtable
	 */
	private Hashtable parseAttributes() {
		return paramParser.parseAttributes(this);
	}

	/**
	 * In case the tag is parsed at the scan method this will return value of a
	 * parameter not implemented yet
	 * 
	 * @param name
	 *            of parameter
	 */
	public String getAttribute(String name) {
		return (String) getAttributes().get(name.toUpperCase());
	}

	/**
	 * Set attribute with given key, value pair.
	 * 
	 * @param key
	 * @param value
	 */
	public void setAttribute(String key, String value) {
		attributes.put(key, value);
	}

	/**
	 * In case the tag is parsed at the scan method this will return value of a
	 * parameter not implemented yet
	 * 
	 * @param name
	 *            of parameter
	 * @deprecated use getAttribute instead
	 */
	public String getParameter(String name) {
		return (String) getAttributes().get(name.toUpperCase());
	}

	/**
	 * Gets the attributes in the tag.
	 * 
	 * @return Returns a Hashtable of attributes
	 */
	public Hashtable getAttributes() {
		if (attributes == null) {
			attributes = parseAttributes();
		}
		return attributes;
	}

	public String getTagName() {
		return (String) getAttributes().get(TAGNAME);
	}

	/**
	 * Returns the line where the tag was found
	 * 
	 * @return java.lang.String
	 */
	public String getTagLine() {
		return tagLine;
	}

	/**
	 * Returns the combined text of all the lines spanned by this tag
	 * 
	 * @return java.lang.String
	 */
	public String[] getTagLines() {
		return tagLines;
	}

	/**
	 * Return the text contained in this tag
	 */
	public String getText() {
		return tagContents.toString();
	}

	/**
	 * Return the scanner associated with this tag.
	 */
	public TagScanner getThisScanner() {
		return thisScanner;
	}

	/**
	 * Extract the first word from the given string. Words are delimited by
	 * whitespace or equals signs.
	 * 
	 * @param s
	 *            The string to get the word from.
	 * @return The first word.
	 */
	public static String extractWord(String s) {
		int length;
		boolean parse;
		char ch;
		StringBuffer ret;

		length = s.length();
		ret = new StringBuffer(length);
		parse = true;
		for (int i = 0; i < length && parse; i++) {
			ch = s.charAt(i);
			if (Character.isWhitespace(ch) || ch == '=')
				parse = false;
			else
				ret.append(Character.toUpperCase(ch));
		}

		return (ret.toString());
	}

	/**
	 * Scan the tag to see using the registered scanners, and attempt
	 * identification.
	 * 
	 * @param url
	 *            URL at which HTML page is located
	 * @param reader
	 *            The NodeReader that is to be used for reading the url
	 */
	public Node scan(Map scanners, String url, NodeReader reader) throws ParserException {
		if (tagContents.length() == 0)
			return this;
		try {
			boolean found = false;
			Node retVal = null;
			// Find the first word in the scanners
			String firstWord = extractWord(tagContents.toString());
			// Now, get the scanner associated with this.
			TagScanner scanner = (TagScanner) scanners.get(firstWord);

			// Now do a deep check
			if (scanner != null && scanner.evaluate(tagContents.toString(), reader.getPreviousOpenScanner())) {
				found = true;
				TagScanner save;
				save = reader.getPreviousOpenScanner();
				reader.setPreviousOpenScanner(scanner);
				retVal = scanner.createScannedNode(this, url, reader, tagLine);
				reader.setPreviousOpenScanner(save);
			}

			if (!found)
				return this;
			else {
				return retVal;
			}
		} catch (Exception e) {
			String errorMsg;
			if (tagContents != null)
				errorMsg = tagContents.toString();
			else
				errorMsg = "null";
			throw new ParserException("Tag.scan() : Error while scanning tag, tag contents = " + errorMsg
					+ ", tagLine = " + tagLine, e);
		}
	}

	/**
	 * Sets the parsed.
	 * 
	 * @param parsed
	 *            The parsed to set
	 */
	public void setAttributes(Hashtable attributes) {
		this.attributes = attributes;
	}

	/**
	 * Sets the nodeBegin.
	 * 
	 * @param nodeBegin
	 *            The nodeBegin to set
	 */
	public void setTagBegin(int tagBegin) {
		this.nodeBegin = tagBegin;
	}

	/**
	 * Gets the nodeBegin.
	 * 
	 * @return The nodeBegin value.
	 */
	public int getTagBegin() {
		return (nodeBegin);
	}

	/**
	 * Sets the nodeEnd.
	 * 
	 * @param nodeEnd
	 *            The nodeEnd to set
	 */
	public void setTagEnd(int tagEnd) {
		this.nodeEnd = tagEnd;
	}

	/**
	 * Gets the nodeEnd.
	 * 
	 * @return The nodeEnd value.
	 */
	public int getTagEnd() {
		return (nodeEnd);
	}

	/**
	 * Gets the line number on which this tag starts.
	 * 
	 * @return the start line number
	 */
	public int getTagStartLine() {
		return startLine;
	}

	/**
	 * Gets the line number on which this tag ends.
	 * 
	 * @return the end line number
	 */
	public int getTagEndLine() {
		return startLine + tagLines.length - 1;
	}

	public void setTagLine(java.lang.String newTagLine) {
		tagLine = newTagLine;

		// Note: Incur the overhead of resizing each time (versus
		// preallocating a larger array), since the average tag
		// generally doesn't span multiple lines
		String[] newTagLines = new String[tagLines.length + 1];
		for (int i = 0; i < tagLines.length; i++)
			newTagLines[i] = tagLines[i];
		newTagLines[tagLines.length] = newTagLine;
		tagLines = newTagLines;
	}

	public void setText(String text) {
		tagContents = new StringBuffer(text);
	}

	public void setThisScanner(TagScanner scanner) {
		thisScanner = scanner;
	}

	public String toPlainTextString() {
		return EMPTY_STRING;
	}

	/**
	 * A call to a tag's toHTML() method will render it in HTML Most tags that
	 * do not have children and inherit from Tag, do not need to override
	 * toHTML().
	 * 
	 * @see org.htmlparser.Node#toHTML()
	 */
	public String toHtml() {
		StringBuffer sb = new StringBuffer();
		sb.append("<");
		sb.append(getTagName());
		if (containsMoreThanOneKey())
			sb.append(" ");
		String key, value;
		String empty = null;
		int i = 0;
		for (Enumeration e = attributes.keys(); e.hasMoreElements();) {
			key = (String) e.nextElement();
			i++;
			if (!key.equals(TAGNAME)) {
				if (key.equals(EMPTYTAG)) {
					empty = "/";
				} else {
					value = getAttribute(key);
					sb.append(key + "=\"" + value + "\"");
					if (i < attributes.size())
						sb.append(" ");
				}
			}
		}
		if (empty != null)
			sb.append(empty);
		if (isEmptyXmlTag())
			sb.append("/");
		sb.append(">");
		return sb.toString();
	}

	private boolean containsMoreThanOneKey() {
		return attributes.keySet().size() > 1;
	}

	/**
	 * Print the contents of the tag
	 */
	public String toString() {
		return "Begin Tag : " + tagContents + "; begins at : " + elementBegin() + "; ends at : " + elementEnd();
	}

	/**
	 * Sets the tagParser.
	 * 
	 * @param tagParser
	 *            The tagParser to set
	 */
	public static void setTagParser(TagParser tagParser) {
		Tag.tagParser = tagParser;
	}

	/**
	 * Determines if the given tag breaks the flow of text.
	 * 
	 * @return <code>true</code> if following text would start on a new line,
	 *         <code>false</code> otherwise.
	 */
	public boolean breaksFlow() {
		return (breakTags.contains(getText().toUpperCase()));
	}

	/**
	 * This method verifies that the current tag matches the provided filter.
	 * The match is based on the string object and not its contents, so ensure
	 * that you are using static final filter strings provided in the tag
	 * classes.
	 * 
	 * @see org.htmlparser.Node#collectInto(NodeList, String)
	 */
	public void collectInto(NodeList collectionList, String filter) {
		if (thisScanner != null && thisScanner.getFilter().equals(filter))
			collectionList.add(this);
	}

	/**
	 * Returns table of attributes in the tag
	 * 
	 * @return Hashtable
	 * @deprecated This method is deprecated. Use getAttributes() instead.
	 */
	public Hashtable getParsed() {
		return attributes;
	}

	/**
	 * Sometimes, a scanner may need to request a re-evaluation of the
	 * attributes in a tag. This may happen when there is some correction
	 * activity. An example of its usage can be found in ImageTag. <br>
	 * <B>Note:<B> This is an intensive task, hence call only when really
	 * necessary
	 * 
	 * @return Hashtable
	 */
	public Hashtable redoParseAttributes() {
		return parseAttributes();
	}

	public void accept(NodeVisitor visitor) {
		visitor.visitTag(this);
	}

	public String getType() {
		return TYPE;
	}

	/**
	 * Is this an empty xml tag of the form<br>
	 * &lt;tag/&gt;
	 * 
	 * @return boolean
	 */
	public boolean isEmptyXmlTag() {
		return emptyXmlTag;
	}

	public void setEmptyXmlTag(boolean emptyXmlTag) {
		this.emptyXmlTag = emptyXmlTag;
	}

}
