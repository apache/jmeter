/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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
package org.apache.jmeter.protocol.java.sampler;

import org.apache.jmeter.config.Arguments;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/**
 * An abstract implementation of the JavaSamplerClient interface.
 * This implementation provides default implementations of most
 * of the methods in the interface, as well as some convenience
 * methods, in order to simplify development of JavaSamplerClient
 * implementations.
 * <p>
 * See {@link org.apache.jmeter.protocol.java.test.SleepTest} for an
 * example of how to extend this class.
 * <p>
 * While it may be necessary to make changes to the JavaSamplerClient
 * interface from time to time (therefore requiring changes to any
 * implementations of this interface), we intend to make this abstract
 * class provide reasonable implementations of any new methods so that
 * subclasses do not necessarily need to be updated for new versions.
 * Therefore, when creating a new JavaSamplerClient implementation,
 * developers are encouraged to subclass this abstract class rather
 * than implementing the JavaSamplerClient interface directly.
 * Implementing JavaSamplerClient directly will continue to be
 * supported for cases where extending this class is not possible
 * (for example, when the client class is already a subclass of some
 * other class).
 * <p>
 * The runTest() method of JavaSamplerClient does not have a default
 * implementation here, so subclasses must define at least this method.
 * It may be useful to override other methods as well.
 * 
 * @see JavaSamplerClient#runTest(JavaSamplerContext)
 * 
 * @author <a href="mailto:jeremy_a@bigfoot.com">Jeremy Arnold</a>
 * @version $Id$
 */
public abstract class AbstractJavaSamplerClient implements JavaSamplerClient
{
    /**
     * The Logger to be used by the Java protocol.  This can be used
     * by subclasses through the getLogger() method.
     * 
     * @see #getLogger()
     */
    private static transient Logger log =
        Hierarchy.getDefaultHierarchy().getLoggerFor("jmeter.protocol.java");

    /* Implements JavaSamplerClient.setupTest(JavaSamplerContext) */
    public void setupTest(JavaSamplerContext context)
    {
        log.debug(getClass().getName() + ": setupTest");
    }

    /* Implements JavaSamplerClient.teardownTest(JavaSamplerContext) */
    public void teardownTest(JavaSamplerContext context)
    {
        log.debug(getClass().getName() + ": teardownTest");
    }

    /* Implements JavaSamplerClient.getDefaultParameters() */
    public Arguments getDefaultParameters()
    {
        return null;
    }

    /**
     * Get a Logger instance which can be used by subclasses to log
     * information.  This is the same Logger which is used by the base
     * JavaSampler classes (jmeter.protocol.java).
     * 
     * @return a Logger instance which can be used for logging
     */
    protected Logger getLogger()
    {
        return log;
    }
}
