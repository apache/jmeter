package org.apache.jmeter.protocol.http.util;

import java.net.URLEncoder;

import org.apache.oro.util.Cache;
import org.apache.oro.util.CacheLRU;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class EncoderCache
{
    Cache cache;

    public EncoderCache(int cacheSize)
    {
       cache = new CacheLRU(cacheSize);
    }
    
    public String getEncoded(String k)
    {
        Object encodedValue = cache.getElement(k);
        if(encodedValue != null)
        {
            return (String)encodedValue;
        }
        encodedValue = URLEncoder.encode(k);
        cache.addElement(k,encodedValue);
        return (String)encodedValue;
    }
    
    

}
