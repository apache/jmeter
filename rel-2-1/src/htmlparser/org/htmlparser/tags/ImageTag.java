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
import org.htmlparser.visitors.NodeVisitor;

/**
 * Identifies an image tag
 */
public class ImageTag extends Tag {
	public static final String IMAGE_TAG_FILTER = "-i";

	/**
	 * The URL where the image is stored.
	 */
	protected String imageURL;

	/**
	 * Constructor creates an HTMLImageNode object, which stores the location
	 * where the image is to be found.
	 * 
	 * @param tagData
	 *            Specifies character position and content of the tag.
	 * @param imageURL
	 *            Location of the image.
	 */
	public ImageTag(TagData tagData, String imageURL) {
		super(tagData);
		this.imageURL = imageURL;
	}

	/**
	 * Returns the location of the image
	 */
	public String getImageURL() {
		return imageURL;
	}

	public String toString() {
		return "IMAGE TAG : Image at " + imageURL + "; begins at : " + elementBegin() + "; ends at : " + elementEnd();
	}

	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
		attributes.put("SRC", imageURL);
	}

	public void accept(NodeVisitor visitor) {
		visitor.visitImageTag(this);
	}

}
