package org.apache.jmeter.testelement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class ListElement extends AbstractTestElement implements List
{
    public final static String LIST = "listelement.list";
    
    public ListElement()
    {
        setProperty(LIST,new ArrayList());
    }
    
    public List getList()
    {
        return (List)getProperty(LIST);
    }

    /**
     * @see java.util.Collection#size()
     */
    public int size()
    {
        return getList().size();
    }

    /**
     * @see java.util.Collection#isEmpty()
     */
    public boolean isEmpty()
    {
        return getList().isEmpty();
    }

    /**
     * @see java.util.Collection#contains(java.lang.Object)
     */
    public boolean contains(Object arg0)
    {
        return getList().contains(arg0);
    }

    /**
     * @see java.util.Collection#iterator()
     */
    public Iterator iterator()
    {
        return getList().iterator();
    }

    /**
     * @see java.util.Collection#toArray()
     */
    public Object[] toArray()
    {
        return getList().toArray();
    }

    /**
     * @see java.util.Collection#toArray(java.lang.Object)
     */
    public Object[] toArray(Object[] arg0)
    {
        return getList().toArray(arg0);
    }

    /**
     * @see java.util.Collection#add(java.lang.Object)
     */
    public boolean add(Object arg0)
    {
        return getList().add(arg0);
    }

    /**
     * @see java.util.Collection#remove(java.lang.Object)
     */
    public boolean remove(Object arg0)
    {
        return getList().remove(arg0);
    }

    /**
     * @see java.util.Collection#containsAll(java.util.Collection)
     */
    public boolean containsAll(Collection arg0)
    {
        return getList().containsAll(arg0);
    }

    /**
     * @see java.util.Collection#addAll(java.util.Collection)
     */
    public boolean addAll(Collection arg0)
    {
        return getList().addAll(arg0);
    }

    /**
     * @see java.util.List#addAll(int, java.util.Collection)
     */
    public boolean addAll(int arg0, Collection arg1)
    {
        return getList().addAll(arg0,arg1);
    }

    /**
     * @see java.util.Collection#removeAll(java.util.Collection)
     */
    public boolean removeAll(Collection arg0)
    {
        return getList().removeAll(arg0);
    }

    /**
     * @see java.util.Collection#retainAll(java.util.Collection)
     */
    public boolean retainAll(Collection arg0)
    {
        return getList().retainAll(arg0);
    }

    /**
     * @see java.util.Collection#clear()
     */
    public void clear()
    {
        getList().clear();
    }

    /**
     * @see java.util.List#get(int)
     */
    public Object get(int arg0)
    {
        return getList().get(arg0);
    }

    /**
     * @see java.util.List#set(int, java.lang.Object)
     */
    public Object set(int arg0, Object arg1)
    {
        return getList().set(arg0,arg1);
    }

    /**
     * @see java.util.List#add(int, java.lang.Object)
     */
    public void add(int arg0, Object arg1)
    {
        getList().add(arg0,arg1);
    }

    /**
     * @see java.util.List#remove(int)
     */
    public Object remove(int arg0)
    {
        return getList().remove(arg0);
    }

    /**
     * @see java.util.List#indexOf(java.lang.Object)
     */
    public int indexOf(Object arg0)
    {
        return getList().indexOf(arg0);
    }

    /**
     * @see java.util.List#lastIndexOf(java.lang.Object)
     */
    public int lastIndexOf(Object arg0)
    {
        return getList().lastIndexOf(arg0);
    }

    /**
     * @see java.util.List#listIterator()
     */
    public ListIterator listIterator()
    {
        return getList().listIterator();
    }

    /**
     * @see java.util.List#listIterator(int)
     */
    public ListIterator listIterator(int arg0)
    {
        return getList().listIterator(arg0);
    }

    /**
     * @see java.util.List#subList(int, int)
     */
    public List subList(int arg0, int arg1)
    {
        return getList().subList(arg0,arg1);
    }

}
