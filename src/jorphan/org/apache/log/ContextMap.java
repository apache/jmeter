/* 
 * Copyright 1999-2004 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.log;

import java.io.Serializable;
import java.util.Hashtable;

/**
 * The ContextMap contains non-hierarchical context information
 * relevant to a particular LogEvent. It may include information
 * such as;
 *
 * <ul>
 * <li>user      -&gt;fred</li>
 * <li>hostname  -&gt;helm.realityforge.org</li>
 * <li>ipaddress -&gt;1.2.3.4</li>
 * <li>interface -&gt;127.0.0.1</li>
 * <li>caller    -&gt;com.biz.MyCaller.method(MyCaller.java:18)</li>
 * <li>source    -&gt;1.6.3.2:33</li>
 * </ul>
 * The context is bound to a thread (and inherited by sub-threads) but
 * it can also be added to by LogTargets.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @author Peter Donald
 * @deprecated Will be dropped in 3.3 
 */
@Deprecated
@SuppressWarnings({"unchecked","rawtypes"}) // will be dropped in 3.3
public final class ContextMap
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    ///Thread local for holding instance of map associated with current thread
    private static final ThreadLocal c_localContext = new InheritableThreadLocal();

    private final ContextMap m_parent;

    ///Container to hold map of elements
    private Hashtable m_map = new Hashtable();

    ///Flag indicating whether this map should be readonly
    private transient boolean m_readOnly;

    /**
     * Get the Current ContextMap.
     * This method returns a ContextMap associated with current thread. If the
     * thread doesn't have a ContextMap associated with it then a new
     * ContextMap is created.
     *
     * @return the current ContextMap
     */
    public static final ContextMap getCurrentContext()
    {
        return getCurrentContext( true );
    }

    /**
     * Get the Current ContextMap.
     * This method returns a ContextMap associated with current thread.
     * If the thread doesn't have a ContextMap associated with it and
     * autocreate is true then a new ContextMap is created.
     *
     * @param autocreate true if a ContextMap is to be created if it doesn't exist
     * @return the current ContextMap
     */
    public static final ContextMap getCurrentContext( final boolean autocreate )
    {
        //Check security permission here???
        ContextMap context = (ContextMap)c_localContext.get();

        if( null == context && autocreate )
        {
            context = new ContextMap();
            c_localContext.set( context );
        }

        return context;
    }

    /**
     * Bind a particular ContextMap to current thread.
     *
     * @param context the context map (may be null)
     */
    public static final void bind( final ContextMap context )
    {
        //Check security permission here??
        c_localContext.set( context );
    }

    /**
     * Default constructor.
     */
    public ContextMap()
    {
        this( null );
    }

    /**
     * Constructor that sets parent contextMap.
     *
     * @param parent the parent ContextMap
     */
    public ContextMap( final ContextMap parent )
    {
        m_parent = parent;
    }

    /**
     * Make the context read-only.
     * This makes it safe to allow untrusted code reference
     * to ContextMap.
     */
    public void makeReadOnly()
    {
        m_readOnly = true;
    }

    /**
     * Determine if context is read-only.
     *
     * @return true if Context is read only, false otherwise
     */
    public boolean isReadOnly()
    {
        return m_readOnly;
    }

    /**
     * Empty the context map.
     *
     */
    public void clear()
    {
        checkReadable();

        m_map.clear();
    }

    /**
     * Get an entry from the context.
     *
     * @param key the key to map
     * @param defaultObject a default object to return if key does not exist
     * @return the object in context
     */
    public Object get( final String key, final Object defaultObject )
    {
        final Object object = get( key );

        if( null != object )
        {
            return object;
        }
        else
        {
            return defaultObject;
        }
    }

    /**
     * Get an entry from the context.
     *
     * @param key the key to map
     * @return the object in context or null if none with specified key
     */
    public Object get( final String key )
    {
        if( key == null )
        {
            return null;
        }
            
        final Object result = m_map.get( key );

        if( null == result && null != m_parent )
        {
            return m_parent.get( key );
        }

        return result;
    }

    /**
     * Set a value in context
     *
     * @param key the key
     * @param value the value (may be null)
     */
    public void set( final String key, final Object value )
    {
        checkReadable();

        if( value == null )
        {
            m_map.remove( key );
        }
        else
        {
            m_map.put( key, value );
        }
    }

    /**
     * Retrieve keys of entries into context map.
     *
     * @return the keys of items in context
     */
    /*
    public String[] getKeys()
    {
        return (String[])m_map.keySet().toArray( new String[ 0 ] );
    }
    */

    /**
     * Get the number of contexts in map.
     *
     * @return the number of contexts in map
     */
    public int getSize()
    {
        return m_map.size();
    }

    /**
     * Helper method that sets context to read-only after de-serialization.
     *
     * @return the corrected object version
     */
    private Object readResolve()
    {
        makeReadOnly();
        return this;
    }

    /**
     * Utility method to verify that Context is read-only.
     */
    private void checkReadable()
    {
        if( isReadOnly() )
        {
            throw new IllegalStateException( "ContextMap is read only and can not be modified" );
        }
    }
}
