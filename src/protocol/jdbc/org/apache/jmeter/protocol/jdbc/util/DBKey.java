// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.jmeter.protocol.jdbc.util;

import java.io.Serializable;

public class DBKey implements Serializable //TODO does it need to be serializable?
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
        
        // All the fields used by the hashCode are now fixed, so calculate it
        hashCode = calculateHashCode();
    }
    
    // Dummy constructor to allow JMeter test suite to work
    public DBKey(){
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
        hashCode = calculateHashCode();
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
