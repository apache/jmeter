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

import java.util.Stack;

import org.htmlparser.tags.Bullet;
import org.htmlparser.tags.Tag;
import org.htmlparser.tags.data.CompositeTagData;
import org.htmlparser.tags.data.TagData;
import org.htmlparser.util.ParserException;

/**
 * This scanner is created by BulletListScanner. It shares a stack to maintain
 * the parent-child relationship with BulletListScanner. The rules implemented
 * are :<br>
 * [1] A &lt;ul&gt; can have &lt;li&gt; under it<br>
 * [2] A &lt;li&gt; can have &lt;ul&gt; under it<br>
 * [3] A &lt;li&gt; cannot have &lt;li&gt; under it<br>
 * <p>
 * These rules are implemented easily through the shared stack.
 */
public class BulletScanner extends CompositeTagScanner {
	private static final String[] MATCH_STRING = { "LI" };

	private final static String ENDERS[] = { "BODY", "HTML" };

	private final static String END_TAG_ENDERS[] = { "UL" };

	private Stack ulli;

	public BulletScanner(Stack ulli) {
		this("", ulli);
	}

	public BulletScanner(String filter, Stack ulli) {
		super(filter, MATCH_STRING, ENDERS, END_TAG_ENDERS, false);
		this.ulli = ulli;
	}

	public Tag createTag(TagData tagData, CompositeTagData compositeTagData) throws ParserException {
		return new Bullet(tagData, compositeTagData);
	}

	public String[] getID() {
		return MATCH_STRING;
	}

	/**
	 * This is the logic that decides when a bullet tag can be allowed
	 */
	public boolean shouldCreateEndTagAndExit() {
		if (ulli.size() == 0)
			return false;
		CompositeTagScanner parentScanner = (CompositeTagScanner) ulli.peek();
		if (parentScanner == this) {
			ulli.pop();
			return true;
		} else
			return false;
	}

	public void beforeScanningStarts() {
		ulli.push(this);
	}

}
