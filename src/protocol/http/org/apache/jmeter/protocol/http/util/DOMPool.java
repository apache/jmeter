/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.jmeter.protocol.http.util;

import java.util.HashMap;
import org.w3c.dom.Document;

/**
 * Title:		JMeter Access Log utilities<br>
 * Copyright:	Apache.org<br>
 * Company:		nobody<br>
 * License:<br>
 * <br>
 * Look at the apache license at the top.<br>
 * <br>
 * Description:<br>
 * <br>
 * The purpose of this class is to cache the DOM
 * Documents in memory and by-pass parsing. For
 * old systems or laptops, it's not practical to
 * parse the XML documents every time. Therefore
 * using a memory cache can reduce the CPU usage
 * .<p>
 * For now this is a simple version to test the
 * feasibility of caching. If it works, this
 * class will be replaced with an Apache commons
 * or something equivalent. If I was familiar
 * with Apache Commons Pool, I would probably
 * use it, but since I don't know the API, it
 * is quicker for Proof of Concept to just
 * write a dumb one. If the number documents
 * in the pool exceed several hundred, it will
 * take a long time for the lookup.
 * <p>
 * Author:	Peter Lin<br>
 * Version: 	0.1<br>
 * Created on:	Jun 17, 2003<br>
 * Last Modified:	4:59:07 PM<br>
 */

public class DOMPool {

	/**
	 * The cache is created with an initial size
	 * of 50. Running a webservice test on an
	 * old system will likely run into memory
	 * or CPU problems long before the HashMap
	 * is an issue.
	 */
	protected static HashMap MEMCACHE = new HashMap(50);

	/**
	 * return a document
	 * @param Object key
	 * @return Document
	 */	
	public static Document getDocument(Object key){
		return (Document)MEMCACHE.get(key);
	}

	/**
	 * add an object to the cache
	 * @param Object key
	 * @param Object data
	 */	
	public static void putDocument(Object key, Object data){
		MEMCACHE.put(key,data);
	}
	
}
