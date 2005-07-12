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
// 
// This class was contributed by 
// Derrick Oswald
//
package org.htmlparser.util;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.RemarkNode;
import org.htmlparser.StringNode;
import org.htmlparser.tags.EndTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.Tag;

/**
 * Create a character reference translation class source file. Usage:
 * 
 * <pre>
 * 
 *      java -classpath .:lib/htmlparser.jar Generate &gt; Translate.java
 *  
 * </pre>
 * 
 * Derived from HTMLStringFilter.java provided as an example with the
 * htmlparser.jar file available at <a
 * href="http://htmlparser.sourceforge.net">htmlparser.sourceforge.net</a>
 * written by Somik Raha ( <a href='mailto:somik@industriallogic.com?
 * subject=htmlparser'>somik@industriallogic. com</a> <a
 * href="http://industriallogic.com">http://industriallogic.com</a>).
 * 
 * @author <a href='mailto:DerrickOswald@users.sourceforge.net?subject=Character
 *         Reference Translation class'>Derrick Oswald</a>
 */
public class Generate {
	/**
	 * The working parser.
	 */
	protected Parser parser;

	/**
	 * The system specific line separator string.
	 */
	protected static final String nl = System.getProperty("line.separator", "\n");

	/**
	 * Create a Generate object. Sets up the generation by creating a new
	 * <code>Parser</code> pointed at <a
	 * href="http://www.w3.org/TR/REC-html40/sgml/entities.html">http://www.w3.org/TR/REC-html40/sgml/entities.html</a>
	 * with the standard scanners registered.
	 */
	public Generate() throws ParserException {
		parser = new Parser("http://www.w3.org/TR/REC-html40/sgml/entities.html");
		parser.registerScanners();
	}

	/**
	 * Translate character references. After generating the Translate class we
	 * could use it to do this job, but that would involve a bootstrap problem,
	 * so this method does the reference conversion for a very tiny subset
	 * (enough to understand the w3.org page).
	 * 
	 * @param string
	 *            The raw string.
	 * @return The string with character references fixed.
	 */
	public String translate(String string) {
		int index;
		int amp;
		StringBuffer ret;

		ret = new StringBuffer(4096);

		index = 0;
		while ((index < string.length()) && (-1 != (amp = string.indexOf('&', index)))) {
			// include the part before the special character
			ret.append(string.substring(index, amp));
			if (string.startsWith("&nbsp;", amp)) {
				ret.append(" ");
				index = amp + 6;
			} else if (string.startsWith("&lt;", amp)) {
				ret.append("<");
				index = amp + 4;
			} else if (string.startsWith("&gt;", amp)) {
				ret.append(">");
				index = amp + 4;
			} else if (string.startsWith("&amp;", amp)) {
				ret.append("&");
				index = amp + 5;
			} else if (string.startsWith("&quote;", amp)) {
				ret.append("\"");
				index = amp + 7;
			} else if (string.startsWith("&divide;", amp)) {
				ret.append('\u00F7');
				index = amp + 8;
			} else if (string.startsWith("&copy;", amp)) {
				ret.append('\u00A9');
				index = amp + 6;
			} else {
				System.out.println("unknown special character starting with " + string.substring(amp, amp + 7));
				ret.append("&");
				index = amp + 1;
			}
		}
		ret.append(string.substring(index));

		return (ret.toString());
	}

	/**
	 * Pull out text elements from the HTML.
	 */
	public void parse() throws ParserException {
		Node node;
		StringBuffer buffer = new StringBuffer(4096);

		// Run through an enumeration of html elements, and pick up
		// only those that are plain string.
		for (NodeIterator e = parser.elements(); e.hasMoreNodes();) {
			node = e.nextNode();

			if (node instanceof StringNode) {
				// Node is a plain string
				// Cast it to an HTMLStringNode
				StringNode stringNode = (StringNode) node;
				// Retrieve the data from the object
				buffer.append(stringNode.getText());
			} else if (node instanceof LinkTag) {
				// Node is a link
				// Cast it to an HTMLLinkTag
				LinkTag linkNode = (LinkTag) node;
				// Retrieve the data from the object and print it
				buffer.append(linkNode.getLinkText());
			} else if (node instanceof Tag) {
				String contents = ((Tag) node).getText();
				if (contents.equals("BR") || contents.equals("P"))
					buffer.append(nl);
			} else if (node instanceof EndTag) {
				String contents = ((EndTag) node).getText();
				if (contents.equals("BR") || contents.equals("P"))
					buffer.append(nl);
			} else if (node instanceof RemarkNode) {
			} else {
				System.out.println();
				System.out.println(node.toString());
			}
		}

		String text = translate(buffer.toString());
		sgml(text);
	}

	/**
	 * Find the lowest index of whitespace (space or newline).
	 * 
	 * @param string
	 *            The string to look in.
	 * @param index
	 *            Where to start looking.
	 * @return -1 if there is no whitespace, the minimum index otherwise.
	 */
	public int indexOfWhitespace(String string, int index) {
		int space;
		int cr;
		int ret;

		space = string.indexOf(" ", index);
		cr = string.indexOf(nl, index);
		if (-1 == space)
			ret = cr;
		else if (-1 == cr)
			ret = space;
		else
			ret = Math.min(space, cr);

		return (ret);
	}

	/**
	 * Rewrite the comment string. In the sgml table, the comments are of the
	 * form:
	 * 
	 * <pre>
	 * 
	 *  -- latin capital letter I with diaeresis,
	 *              U+00CF ISOlat1
	 *  
	 * </pre>
	 * 
	 * so we just want to make a one-liner without the spaces and newlines.
	 * 
	 * @param string
	 *            The raw comment.
	 * @return The single line comment.
	 */
	public String pack(String string) {
		int index;
		int spaces;
		StringBuffer ret;

		ret = new StringBuffer(string.length());

		if (string.startsWith("-- "))
			string = string.substring(3);
		// remove doublespaces
		index = 0;
		while ((index < string.length()) && (-1 != (spaces = indexOfWhitespace(string, index)))) {
			ret.append(string.substring(index, spaces));
			ret.append(" ");
			while ((spaces < string.length()) && Character.isWhitespace(string.charAt(spaces)))
				spaces++;
			index = spaces;
		}
		if (index < string.length())
			ret.append(string.substring(index));

		return (ret.toString());
	}

	/**
	 * Pretty up a comment string.
	 * 
	 * @param string
	 *            The comment to operate on.
	 * @return The beautiful comment string.
	 */
	public String pretty(String string) {
		int index;
		int spaces;
		StringBuffer ret;

		ret = new StringBuffer(string.length());

		// newline instead of doublespaces
		index = 0;
		while ((index < string.length()) && (-1 != (spaces = string.indexOf("  ", index)))) {
			ret.append("        // " + string.substring(index, spaces));
			if (!string.substring(index, spaces).endsWith(nl))
				ret.append(nl);
			while ((spaces < string.length()) && Character.isWhitespace(string.charAt(spaces)))
				spaces++;
			index = spaces;
		}
		if (index < string.length())
			ret.append("        // " + string.substring(index));

		return (ret.toString());
	}

	/**
	 * Pad a string on the left with the given character to the length
	 * specified.
	 * 
	 * @param string
	 *            The string to pad
	 * @param character
	 *            The character to pad with.
	 * @param length
	 *            The size to pad to.
	 * @return The padded string.
	 */
	public String pad(String string, char character, int length) {
		StringBuffer ret;

		ret = new StringBuffer(length);
		ret.append(string);
		while (length > ret.length())
			ret.insert(0, character);

		return (ret.toString());
	}

	/**
	 * Convert the textual representation of the numeric character reference to
	 * a character.
	 * 
	 * @param string
	 *            The numeric character reference (in quotes).
	 * @return The character represented by the numeric character reference.
	 * 
	 */
	public String unicode(String string) {
		int code;

		if (string.startsWith("\"&#") && string.endsWith(";\"")) {
			string = string.substring(3, string.length() - 2);
			try {
				code = Integer.parseInt(string);
				string = "new Character ('\\u" + pad(Integer.toHexString(code), '0', 4) + "')";
			} catch (Exception e) {
				e.printStackTrace();
			}
			return (string);
		} else
			return (string);
	}

	/**
	 * Parse the sgml declaration for character entity reference name,
	 * equivalent numeric character reference and a comment. Emit a java hash
	 * table 'put' with the name as the key, the numeric character as the value
	 * and comment the insertion with the comment.
	 * 
	 * @param string
	 *            The contents of the sgml declaration.
	 */
	public void extract(String string) {
		int space;
		String token;
		String code;
		int comment;
		String description;

		if (string.startsWith("<!--"))
			System.out.println(pretty(string.substring(4, string.length() - 3).trim()));
		else if (string.startsWith("<!ENTITY")) {
			string = string.substring(8, string.length() - 3).trim();
			if (-1 != (space = string.indexOf(" "))) {
				token = string.substring(0, space);
				string = string.substring(space).trim();
				if (string.startsWith("CDATA")) {
					string = string.substring(5).trim();
					if (-1 != (space = string.indexOf(" "))) {
						code = string.substring(0, space).trim();
						code = unicode(code);
						string = string.substring(space).trim();
						System.out.println("        mRefChar.put (\"" + token + "\","
						// no token is larger than 8 characters - yet
								+ pad(code, ' ', code.length() + 9 - token.length()) + ");" + " // " + pack(string));
					} else
						System.out.println(string);
				} else
					System.out.println(string);
			} else
				System.out.println(string);
		} else
			System.out.println(string);
	}

	/**
	 * Extract special characters. Scan the string looking for substrings of the
	 * form:
	 * 
	 * <pre>
	 * 
	 *  &lt;!ENTITY nbsp   CDATA &quot;&amp;#160;&quot; -- no-break space = non-breaking space, U+00A0 ISOnum --&gt;
	 *  
	 * </pre>
	 * 
	 * and emit a java definition for each.
	 * 
	 * @param string
	 *            The raw string from w3.org.
	 */
	public void sgml(String string) {
		int index;
		int begin;
		int end;
		StringBuffer ret;

		ret = new StringBuffer(4096);

		index = 0;
		while (-1 != (begin = string.indexOf("<", index))) {
			if (-1 != (end = string.indexOf("-->", begin))) {
				extract(string.substring(begin, end + 3));
				index = end + 3;
			} else
				index = begin + 1;
		}
	}

	/**
	 * Generator program.
	 * 
	 * <pre>
	 * 
	 *      java -classpath .:lib/htmlparser.jar Generate &gt; Translate.java
	 *  
	 * </pre>
	 * 
	 * @param args
	 *            <em>Not used.</em>
	 */
	public static void main(String[] args) throws ParserException {
		Generate filter = new Generate();
		System.out.println("import java.util.Hashtable;");
		System.out.println("import java.util.Iterator;");
		System.out.println();
		System.out.println("/**");
		System.out
				.println(" * Translate numeric character references and character entity references to unicode characters.");
		System.out
				.println(" * Based on tables found at <a href=\"http://www.w3.org/TR/REC-html40/sgml/entities.html\">");
		System.out.println(" * http://www.w3.org/TR/REC-html40/sgml/entities.html</a>");
		System.out.println(" * <p><b>Note: Do not edit! This class is created by the Generate class.</b>");
		System.out.println(" * <p>Typical usage:");
		System.out.println(" * <pre>");
		System.out.println(" *      String s = Translate.decode (getTextFromHtmlPage ());");
		System.out.println(" * </pre>");
		System.out
				.println(" * @author <a href='mailto:DerrickOswald@users.sourceforge.net?subject=Character Reference Translation class'>Derrick Oswald</a>");
		System.out.println(" */");
		System.out.println("public class Translate");
		System.out.println("{");
		System.out.println("    /**");
		System.out.println("     * Table mapping entity reference kernel to character.");
		System.out.println("     * <p><code>String</code>-><code>Character</code>");
		System.out.println("     */");
		System.out.println("    protected static Hashtable mRefChar;");
		System.out.println("    static");
		System.out.println("    {");
		System.out.println("        mRefChar = new Hashtable (1000);");
		System.out.println();
		filter.parse();
		System.out.println("    }");
		System.out.println();
		System.out.println("    /**");
		System.out.println("     * Table mapping character to entity reference kernel.");
		System.out.println("     * <p><code>Character</code>-><code>String</code>");
		System.out.println("     */");
		System.out.println("    protected static Hashtable mCharRef;");
		System.out.println("    static");
		System.out.println("    {");
		System.out.println("        mCharRef = new Hashtable (mRefChar.size ());");
		System.out.println();
		System.out.println("        Iterator iterator = mRefChar.keySet ().iterator ();");
		System.out.println("        while (iterator.hasNext ())");
		System.out.println("        {");
		System.out.println("            String key = (String)iterator.next ();");
		System.out.println("            Character character = (Character)mRefChar.get (key);");
		System.out.println("            mCharRef.put (character, key);");
		System.out.println("        }");
		System.out.println("    }");
		System.out.println();
		System.out.println("    /**");
		System.out.println("     * Private constructor.");
		System.out.println("     * This class is fully static and thread safe.");
		System.out.println("     */");
		System.out.println("    private Translate ()");
		System.out.println("    {");
		System.out.println("    }");
		System.out.println();
		System.out.println("    /**");
		System.out.println("     * Convert a reference to a unicode character.");
		System.out.println("     * Convert a single numeric character reference or character entity reference");
		System.out.println("     * to a unicode character.");
		System.out.println("     * @param string The string to convert. Of the form &xxxx; or &amp;#xxxx; with");
		System.out.println("     * or without the leading ampersand or trailing semi-colon.");
		System.out.println("     * @return The converted character or '\\0' (zero) if the string is an");
		System.out.println("     * invalid reference.");
		System.out.println("     */");
		System.out.println("    public static char convertToChar (String string)");
		System.out.println("    {");
		System.out.println("        int length;");
		System.out.println("        Character item;");
		System.out.println("        char ret;");
		System.out.println();
		System.out.println("        ret = 0;");
		System.out.println();
		System.out.println("        length = string.length ();");
		System.out.println("        if (0 < length)");
		System.out.println("        {");
		System.out.println("            if ('&' == string.charAt (0))");
		System.out.println("            {");
		System.out.println("                string = string.substring (1);");
		System.out.println("                length--;");
		System.out.println("            }");
		System.out.println("            if (0 < length)");
		System.out.println("            {");
		System.out.println("                if (';' == string.charAt (length - 1))");
		System.out.println("                    string = string.substring (0, --length);");
		System.out.println("                if (0 < length)");
		System.out.println("                {");
		System.out.println("                    if ('#' == string.charAt (0))");
		System.out.println("                        try");
		System.out.println("                        {");
		System.out.println("                            ret = (char)Integer.parseInt (string.substring (1));");
		System.out.println("                        }");
		System.out.println("                        catch (NumberFormatException nfe)");
		System.out.println("                        {");
		System.out.println("                            /* failed conversion, return 0 */");
		System.out.println("                        }");
		System.out.println("                    else");
		System.out.println("                    {");
		System.out.println("                        item = (Character)refChar.get (string);");
		System.out.println("                        if (null != item)");
		System.out.println("                            ret = item.charValue ();");
		System.out.println("                    }");
		System.out.println("                }");
		System.out.println("            }");
		System.out.println("        }");
		System.out.println();
		System.out.println("        return (ret);");
		System.out.println("    }");
		System.out.println();
		System.out.println("    /**");
		System.out.println("     * Decode a string containing references.");
		System.out.println("     * Change all numeric character reference and character entity references");
		System.out.println("     * to unicode characters.");
		System.out.println("     * @param string The string to translate.");
		System.out.println("     */");
		System.out.println("    public static String decode (String string)");
		System.out.println("    {");
		System.out.println("        int index;");
		System.out.println("        int length;");
		System.out.println("        int amp;");
		System.out.println("        int semi;");
		System.out.println("        String code;");
		System.out.println("        char character;");
		System.out.println("        StringBuffer ret;");
		System.out.println();
		System.out.println("        ret = new StringBuffer (string.length ());");
		System.out.println();
		System.out.println("        index = 0;");
		System.out.println("        length = string.length ();");
		System.out.println("        while ((index < length) && (-1 != (amp = string.indexOf ('&', index))))");
		System.out.println("        {");
		System.out.println("            ret.append (string.substring (index, amp));");
		System.out.println("            index = amp + 1;");
		System.out.println("            if (amp < length - 1)");
		System.out.println("            {");
		System.out.println("                semi = string.indexOf (';', amp);");
		System.out.println("                if (-1 != semi)");
		System.out.println("                    code = string.substring (amp, semi + 1);");
		System.out.println("                else");
		System.out.println("                    code = string.substring (amp);");
		System.out.println("                if (0 != (character = convertToChar (code)))");
		System.out.println("                    index += code.length () - 1;");
		System.out.println("                else");
		System.out.println("                    character = '&';");
		System.out.println("            }");
		System.out.println("            else");
		System.out.println("                character = '&';");
		System.out.println("            ret.append (character);");
		System.out.println("        }");
		System.out.println("        if (index < length)");
		System.out.println("            ret.append (string.substring (index));");
		System.out.println();
		System.out.println("        return (ret.toString ());");
		System.out.println("    }");
		System.out.println();
		System.out.println("    /**");
		System.out.println("     * Convert a character to a character entity reference.");
		System.out.println("     * Convert a unicode character to a character entity reference of");
		System.out.println("     * the form &xxxx;.");
		System.out.println("     * @param character The character to convert.");
		System.out.println("     * @return The converted character or <code>null</code> if the character");
		System.out.println("     * is not one of the known entity references.");
		System.out.println("     */");
		System.out.println("    public static String convertToString (Character character)");
		System.out.println("    {");
		System.out.println("        StringBuffer buffer;");
		System.out.println("        String ret;");
		System.out.println();
		System.out.println("        if (null != (ret = (String)mCharRef.get (character)))");
		System.out.println("        {");
		System.out.println("            buffer = new StringBuffer (ret.length () + 2);");
		System.out.println("            buffer.append ('&');");
		System.out.println("            buffer.append (ret);");
		System.out.println("            buffer.append (';');");
		System.out.println("            ret = buffer.toString ();");
		System.out.println("        }");
		System.out.println();
		System.out.println("        return (ret);");
		System.out.println("    }");
		System.out.println();
		System.out.println("    /**");
		System.out.println("     * Convert a character to a numeric character reference.");
		System.out.println("     * Convert a unicode character to a numeric character reference of");
		System.out.println("     * the form &amp;#xxxx;.");
		System.out.println("     * @param character The character to convert.");
		System.out.println("     * @return The converted character.");
		System.out.println("     */");
		System.out.println("    public static String convertToString (int character)");
		System.out.println("    {");
		System.out.println("        StringBuffer ret;");
		System.out.println();
		System.out.println("        ret = new StringBuffer (13); /* &#2147483647; */");
		System.out.println("        ret.append (\"&#\");");
		System.out.println("        ret.append (character);");
		System.out.println("        ret.append (';');");
		System.out.println();
		System.out.println("        return (ret.toString ());");
		System.out.println("    }");
		System.out.println();
		System.out.println("    /**");
		System.out.println("     * Encode a string to use references.");
		System.out.println("     * Change all characters that are not ASCII to their numeric character");
		System.out.println("     * reference or character entity reference.");
		System.out.println("     * This implementation is inefficient, allocating a new");
		System.out.println("     * <code>Character</code> for each character in the string,");
		System.out.println("     * but this class is primarily intended to decode strings");
		System.out.println("     * so efficiency and speed in the encoding was not a priority.");
		System.out.println("     * @param string The string to translate.");
		System.out.println("     */");
		System.out.println("    public static String encode (String string)");
		System.out.println("    {");
		System.out.println("        int length;");
		System.out.println("        char c;");
		System.out.println("        Character character;");
		System.out.println("        String value;");
		System.out.println("        StringBuffer ret;");
		System.out.println();
		System.out.println("        ret = new StringBuffer (string.length () * 6);");
		System.out.println("        length  = string.length ();");
		System.out.println("        for (int i = 0; i < length; i++)");
		System.out.println("        {");
		System.out.println("            c = string.charAt (i);");
		System.out.println("            character = new Character (c);");
		System.out.println("            if (null != (value = convertToString (character)))");
		System.out.println("                ret.append (value);");
		System.out.println("            else if (!((c > 0x001F) && (c < 0x007F)))");
		System.out.println("            {");
		System.out.println("                value = convertToString (c);");
		System.out.println("                ret.append (value);");
		System.out.println("            }");
		System.out.println("            else");
		System.out.println("                ret.append (character);");
		System.out.println("        }");
		System.out.println();
		System.out.println("        return (ret.toString ());");
		System.out.println("    }");
		System.out.println("}");
	}
}
