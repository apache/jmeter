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
package org.apache.jmeter.config;

import java.util.*;

/************************************************************
 *  Title: Apache JMeter Description: Copyright: Copyright (c) 2000 Company:
 *  Apache Foundation
 *
 *@author     Michael Stover
 *@created    March 13, 2001
 *@version    1.0
 ***********************************************************/

public interface ConfigElement extends Cloneable
{

	/************************************************************
	 *  Gets a property of the config element. Object getProperty(String key); Sets
	 *  a property of the config element. void putProperty(String key,Object
	 *  value); Get the names of all valid properties for this config element.
	 *  Collection getPropertyNames();
	 *
	 *  This allows config elements to combine and give a "layered" effect. for
	 *  example, say there are two HTTPConfigElements, which have properties for
	 *  domain, path, method, and parameters. If element A has everything filled
	 *  in, but null for domain, and element B is added, which has only domain
	 *  filled in, then after adding B to A, A will have the domain from B. If A
	 *  already had a domain, then the correct behavior is for A to ignore the
	 *  addition of element B.
	 *
	 *@param  config  The feature to be added to the ConfigElement attribute
	 ***********************************************************/
	void addConfigElement(ConfigElement config);

	/************************************************************
	 *  If your config element expects to be modified in the process of a test run,
	 *  and you want those modifications to carry over from sample to sample (as in
	 *  a cookie manager - you want to save all cookies that gets set throughout
	 *  the test), then return true for this method. You config element will not be
	 *  cloned for each sample. If your config elements are more static in nature,
	 *  return false. If in doubt, return false;
	 *
	 *@return    Description of the Returned Value
	 ***********************************************************/
	public boolean expectsModification();

	Object clone();
}
