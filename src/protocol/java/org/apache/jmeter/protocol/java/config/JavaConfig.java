/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002,2003 The Apache Software Foundation.  All rights
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
package org.apache.jmeter.protocol.java.config;

import java.io.Serializable;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.protocol.java.sampler.JavaSampler;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/**
 * The <code>JavaConfig</code> class contains the configuration data
 * necessary for the Java protocol.  This data is used to configure a
 * {@link org.apache.jmeter.protocol.java.sampler.JavaSamplerClient}
 * instance to perform performance test samples.
 *
 * @author     Brad Kiewel
 * @author <a href="mailto:jeremy_a@bigfoot.com">Jeremy Arnold</a>
 * @version $Id$
 */
public class JavaConfig extends ConfigTestElement implements Serializable
{
    /** Logging */
    private static transient Logger log =
        Hierarchy.getDefaultHierarchy().getLoggerFor("jmeter.protocol.java");

    /**
     *  Constructor for the JavaConfig object
     */
    public JavaConfig()
    {
        setArguments(new Arguments());
    }

    /**
     * Sets the class name attribute of the JavaConfig object.  This is the
     * class name of the
     * {@link org.apache.jmeter.protocol.java.sampler.JavaSamplerClient}
     * implementation which will be used to execute the test.
     *
     * @param classname  the new classname value
     */
    public void setClassname(String classname)
    {
        setProperty(JavaSampler.CLASSNAME, classname);
    }

    /**
     * Gets the class name attribute of the JavaConfig object.  This is the
     * class name of the
     * {@link org.apache.jmeter.protocol.java.sampler.JavaSamplerClient}
     * implementation which will be used to execute the test.
     *
     * @return           the classname value
     */
    public String getClassname()
    {
        return getPropertyAsString(JavaSampler.CLASSNAME);
    }

    /**
     * Adds an argument to the list of arguments for this JavaConfig object.
     * The {@link org.apache.jmeter.protocol.java.sampler.JavaSamplerClient}
     * implementation can access these arguments through the
     * {@link org.apache.jmeter.protocol.java.sampler.JavaSamplerContext}.
     * 
     * @param name       the name of the argument to be added
     * @param value      the value of the argument to be added
     */
    public void addArgument(String name, String value)
    {
        Arguments args = this.getArguments();
        args.addArgument(name, value);
    }

    /**
     * Removes all of the arguments associated with this JavaConfig object.
     */
    public void removeArguments()
    {
        setProperty(
            new TestElementProperty(JavaSampler.ARGUMENTS, new Arguments()));
    }

    /**
     * Set all of the arguments for this JavaConfig object.  This will replace
     * any previously added arguments.  The
     * {@link org.apache.jmeter.protocol.java.sampler.JavaSamplerClient}
     * implementation can access these arguments through the
     * {@link org.apache.jmeter.protocol.java.sampler.JavaSamplerContext}.
     * 
     * @param args       the new arguments
     */
    public void setArguments(Arguments args)
    {
        setProperty(new TestElementProperty(JavaSampler.ARGUMENTS, args));
    }

    /**
     * Gets the arguments for this JavaConfig object.  The
     * {@link org.apache.jmeter.protocol.java.sampler.JavaSamplerClient}
     * implementation can access these arguments through the
     * {@link org.apache.jmeter.protocol.java.sampler.JavaSamplerContext}.
     *
     * @return           the arguments
     */
    public Arguments getArguments()
    {
        return (Arguments) getProperty(JavaSampler.ARGUMENTS).getObjectValue();
    }
}
