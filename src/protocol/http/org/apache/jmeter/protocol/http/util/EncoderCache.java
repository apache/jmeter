package org.apache.jmeter.protocol.http.util;

import java.io.UnsupportedEncodingException;

import org.apache.jorphan.util.JOrphanUtils;
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
        try
        {
            encodedValue = JOrphanUtils.encode(k, "utf8");
        }
        catch (UnsupportedEncodingException e)
        {
            // This can't happen (how should utf8 not be supported!?!),
            // so just throw an Error:
            throw new Error("Should not happen: "+e.toString());
        }
        cache.addElement(k,encodedValue);
        return (String)encodedValue;
    }
}
