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

package org.apache.jmeter.protocol.http.util.accesslog;

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
 * LogParser is the base interface for classes
 * implementing concrete parse logic. For an
 * example of how to use the interface, look
 * at the Tomcat access log parser.<p>
 * The original log parser was written in 2
 * hours to parse access logs. Since then,
 * the design and implementation has been
 * rewritten from scratch several times to
 * make it more generic and extensible. The
 * first version was hard coded and written
 * over the weekend.<p>
 * Author:	Peter Lin<br>
 * Version: 	0.1<br>
 * Created on:	Jun 23, 2003<br>
 * Last Modified:	5:59:05 PM<br>
 */

public interface LogParser {

	/**
	 * close the any streams or readers.
	 */
	public void close();
	
	/**
	 * Concrete parsers need to have a generator
	 * to recieve the parsed result.
	 * @param generator
	 */
	public void setGenerator(Generator generator);
	
	/**
	 * the method will parse the given number of
	 * lines. Pass "-1" to parse the entire file.
	 * @param count
	 * @return int
	 */
	public int parse(int count);
	
	/**
	 * We allow for filters, so that users can
	 * simply point to an Access log without
	 * having to clean it up. This makes it
	 * significantly easier and reduces the
	 * amount of work. Plus I'm lazy, so going
	 * through a log file to clean it up is a
	 * bit tedious. One example of this is 
	 * using the filter to exclude any log
	 * entry that has a 505 response code.
	 * @param filter
	 */
	public void setFilter(Filter filter);
	
	/**
	 * The method is provided to make it easy to
	 * dynamically create new classes using
	 * Class.newInstance(). Then the access log
	 * file is set using this method.
	 * @param source
	 */
	public void setSourceFile(String source);
}
