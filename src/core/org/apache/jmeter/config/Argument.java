/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001,2003 The Apache Software Foundation.  All rights
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

import java.io.Serializable;

import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.StringProperty;

//Mark Walsh, 2002-08-03, add metadata attribute
// add constructor Argument(String name, Object value, Object metadata)
// add MetaData get and set methods

/**
 * Class representing an argument.  Each argument consists of a name/value pair,
 * as well as (optional) metadata.
 * 
 * @author    Michael Stover
 * @author    Mark Walsh
 * @version   $Revision$
 */
public class Argument extends AbstractTestElement implements Serializable
{
    /** Name used to store the argument's name. */
    public static final String NAME = "Argument.name";

    /** Name used to store the argument's value. */
    public static final String VALUE = "Argument.value";

    /** Name used to store the argument's metadata. */
    public static final String METADATA = "Argument.metadata";

    /**
     * Create a new Argument without a name, value, or metadata.
     */
    public Argument()
    {
    }

    /**
     * Create a new Argument with the specified name and value, and no
     * metadata.
     *
     * @param name   the argument name
     * @param value  the argument value
     */
    public Argument(String name, String value)
    {
        setProperty(new StringProperty(NAME, name));
        setProperty(new StringProperty(VALUE, value));
    }

    /**
     * Create a new Argument with the specified name, value, and metadata.
     *
     * @param name     the argument name
     * @param value    the argument value
     * @param metadata the argument metadata
     */
    public Argument(String name, String value, String metadata)
    {
        setProperty(new StringProperty(NAME, name));
        setProperty(new StringProperty(VALUE, value));
        setProperty(new StringProperty(METADATA, metadata));
    }

    /**
     * Set the name of the Argument.
     *
     * @param newName  the new name
     */
    public void setName(String newName)
    {
        setProperty(new StringProperty(NAME, newName));
    }

    /**
     * Get the name of the Argument.
     * 
     * @return the attribute's name
     */
    public String getName()
    {
        return getPropertyAsString(NAME);
    }

    /**
     * Sets the value of the Argument.
     *
     * @param newValue  the new value
     */
    public void setValue(String newValue)
    {
        setProperty(new StringProperty(VALUE, newValue));
    }

    /**
     * Gets the value of the Argument object.
     *
     * @return the attribute's value
     */
    public String getValue()
    {
        return getPropertyAsString(VALUE);
    }

    /**
     * Sets the Meta Data attribute of the Argument.
     *
     * @param newMetaData  the new metadata
     */
    public void setMetaData(String newMetaData)
    {
        setProperty(new StringProperty(METADATA, newMetaData));
    }

    /**
     * Gets the Meta Data attribute of the Argument.
     *
     * @return   the MetaData value
     */
    public String getMetaData()
    {
        return getPropertyAsString(METADATA);
    }
}
