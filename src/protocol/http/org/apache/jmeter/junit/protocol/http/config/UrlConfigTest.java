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

 package org.apache.jmeter.junit.protocol.http.config;



import junit.framework.TestCase;




import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;



/**

 * Title:        Apache JMeter

 * Description:

 * Copyright:    Copyright (c) 2000

 * Company:      Apache Foundation

 * @author Michael Stover

 * @version 1.0

 */



public class UrlConfigTest extends TestCase

{

	HTTPSampler config;

	HTTPSampler defaultConfig;

	HTTPSampler partialConfig;



	public UrlConfigTest(String name)

	{

		super(name);

	}



	protected void setUp()

	{

		Arguments args = new Arguments();

		args.addArgument("username","mstover");

		args.addArgument("password","pass");

		args.addArgument("action","login");

		config = new HTTPSampler();

		config.setName("Full Config");

		config.setProperty(HTTPSampler.DOMAIN,"www.lazer.com");

		config.setProperty(HTTPSampler.PATH,"login.jsp");

		config.setProperty(HTTPSampler.METHOD,HTTPSampler.POST);

		config.setProperty(HTTPSampler.ARGUMENTS,args);



		defaultConfig = new HTTPSampler();

		defaultConfig.setName("default");

		defaultConfig.setProperty(HTTPSampler.DOMAIN,"www.xerox.com");

		defaultConfig.setProperty(HTTPSampler.PATH,"default.html");



		partialConfig = new HTTPSampler();

		partialConfig.setProperty(HTTPSampler.PATH,"main.jsp");

		partialConfig.setProperty(HTTPSampler.METHOD,HTTPSampler.GET);

	}



	public void testSimpleConfig()

	{

		assertTrue(config.getName().equals("Full Config"));

		assertEquals(config.getDomain(),"www.lazer.com");

	}



	public void testOverRide()
	{
		this.assertNull(partialConfig.getProperty(HTTPSampler.DOMAIN));
		partialConfig.addTestElement(defaultConfig);
		assertEquals(partialConfig.getProperty(HTTPSampler.DOMAIN),"www.xerox.com");
		assertEquals(partialConfig.getProperty(HTTPSampler.PATH),"main.jsp");
	}





	public static void main(String[] args)

	{

	}

}