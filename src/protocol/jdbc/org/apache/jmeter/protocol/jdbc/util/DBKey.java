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
package org.apache.jmeter.protocol.jdbc.util;

import java.io.Serializable;

public class DBKey implements Serializable
{
    private final String driver;
    private final String url;
    private final String username;
    private final String password;

    /**
     * Cache for the hashCode value since this class will frequently be used
     * as a Map key.
     */ 
    private transient int hashCode = 0;

    
    public DBKey(
        String driver,
        String url,
        String username,
        String password)
    {
        if (driver == null)
        {
            throw new IllegalArgumentException(
                    "DBKey 'driver' must be non-null");
        }

        if (url == null)
        {
            throw new IllegalArgumentException("DBKey 'url' must be non-null");
        }

        // Other fields are allowed to be null
        
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
    }
    // Dummy constructor to allow JMeter test suite to work
    DBKey(){
    	this("","","","");
    }

    public String getUrl()
    {
        return url;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    public String getDriver()
    {
        return driver;
    }

    /**
     * Determines if the two DBKey objects have the same property values.
     * @param o2 DBKey to compare with this one.
     * @return bool True if equal, false otherwise.
     */
    public boolean equals(Object o2)
    {
        if (this == o2)
        {
            return true;
        }
        
        if (!(o2 instanceof DBKey))
        {
            return false;
        }
        
        DBKey key = (DBKey)o2;
        
        if (!driver.equals(key.driver))
        {
            return false;
        }
        
        if (!url.equals(key.url))
        {
            return false;
        }
        
        if (username == null)
        {
            if (key.username != null)
            {
                return false;
            }
        }
        else
        {
            if (!username.equals(key.username))
            {
                return false;
            }
        }
        
        if (password == null)
        {
            if (key.password != null)
            {
                return false;
            }
        }
        else
        {
            if (!password.equals(key.password))
            {
                return false;
            }
        }
        
        return true;
    }

    public int hashCode()
    {
        if (hashCode == 0)
        {
            hashCode = calculateHashCode();
        }
        return hashCode;
    }
    
    private int calculateHashCode()
    {
        // Implementation based on Joshua Bloch's _Effective Java_
        // http://developer.java.sun.com/developer/Books/effectivejava/Chapter3.pdf
        int result = 17;
        
        result = 37 * result + driver.hashCode();
        result = 37 * result + url.hashCode();
        result = 37 * result + (username == null ? 0 : username.hashCode());
        result = 37 * result + (password == null ? 0 : password.hashCode());
        
        return result;
    }

    public String toString()
    {
        StringBuffer ret = new StringBuffer();
        ret.append("Class=DBKey(" + "\n");
        ret.append("driver=" + driver + "\n");
        ret.append("url=" + url + "\n");
        ret.append("username=" + username + "\n");
        ret.append(")");
        return ret.toString();
    }
}
