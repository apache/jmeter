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

import org.htmlparser.tags.data.TagData;

/**
 * Identifies an bgsound tag
 */
public class BgSoundTag extends Tag {
	/**
	 * The URL where the image is stored.
	 */
	protected String bgsoundURL;

	public BgSoundTag(TagData tagData, String bgsoundURL) {
		super(tagData);
		this.bgsoundURL = bgsoundURL;
	}

	/**
	 * Print the contents of the Tag
	 */
	public String toString() {
		return "BGSOUND TAG : at " + bgsoundURL + "; begins at : " + elementBegin() + "; ends at : " + elementEnd();
	}
}
