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
package org.htmlparser.tests.utilTests;

import junit.framework.TestCase;

import org.htmlparser.util.Translate;

public class CharacterTranslationTest extends TestCase {
	public CharacterTranslationTest(String name) {
		super(name);
	}

	public void testInitialCharacterEntityReference() {
		assertEquals("character entity reference at start of string doesn't work", "÷ is the division sign.", Translate
				.decode("&divide; is the division sign."));
	}

	public void testInitialNumericCharacterReference() {
		assertEquals("numeric character reference at start of string doesn't work", "÷ is the division sign.",
				Translate.decode("&#247; is the division sign."));
	}

	public void testInitialCharacterEntityReferenceWithoutSemi() {
		assertEquals("character entity reference without a semicolon at start of string doesn't work",
				"÷ is the division sign.", Translate.decode("&divide; is the division sign."));
	}

	public void testInitialNumericCharacterReferenceWithoutSemi() {
		assertEquals("numeric character reference without a semicolon at start of string doesn't work",
				"÷ is the division sign.", Translate.decode("&#247; is the division sign."));
	}

	public void testFinalCharacterEntityReference() {
		assertEquals("character entity reference at end of string doesn't work", "The division sign (÷) is ÷",
				Translate.decode("The division sign (÷) is &divide;"));
	}

	public void testFinalNumericCharacterReference() {
		assertEquals("numeric character reference at end of string doesn't work", "The division sign (÷) is ÷",
				Translate.decode("The division sign (÷) is &#247;"));
	}

	public void testFinalCharacterEntityReferenceWithoutSemi() {
		assertEquals("character entity reference without a semicolon at end of string doesn't work",
				"The division sign (÷) is ÷", Translate.decode("The division sign (÷) is &divide"));
	}

	public void testFinalNumericCharacterReferenceWithoutSemi() {
		assertEquals("numeric character reference without a semicolon at end of string doesn't work",
				"The division sign (÷) is ÷", Translate.decode("The division sign (÷) is &#247"));
	}

	public void testReferencesInString() {
		assertEquals(
				"character references within a string don't work",
				"Thus, the character entity reference ÷ is a more convenient form than ÷ for obtaining the division sign (÷)",
				Translate
						.decode("Thus, the character entity reference &divide; is a more convenient form than &#247; for obtaining the division sign (÷)"));
	}

	public void testBogusCharacterEntityReference() {
		assertEquals("bogus character entity reference doesn't work",
				"The character entity reference &divode; is bogus", Translate
						.decode("The character entity reference &divode; is bogus"));
	}

	public void testBogusNumericCharacterReference() {
		assertEquals("bogus numeric character reference doesn't work",
				"The numeric character reference &#2F7; is bogus", Translate
						.decode("The numeric character reference &#2F7; is bogus"));
	}

	public void testEncode() {
		assertEquals("encode doesn't work",
				"Character entity reference: &divide;, another: &nbsp;, numeric character reference: &#9831;.",
				Translate
						.encode("Character entity reference: ÷, another: \u00a0, numeric character reference: \u2667."));
	}

	public void testEncodeLink() {
		assertEquals(
				"encode link doesn't work",
				"&lt;a href=&quot;http://www.w3.org/TR/REC-html40/sgml/entities.html&quot;&gt;http://www.w3.org/TR/REC-html40/sgml/entities.html&lt;/a&gt;",
				Translate
						.encode("<a href=\"http://www.w3.org/TR/REC-html40/sgml/entities.html\">http://www.w3.org/TR/REC-html40/sgml/entities.html</a>"));
	}
}