package org.apache.jmeter.testelement;

import java.io.Serializable;
import java.util.List;

/**
 * @author    Michael Stover
 * @created   March 13, 2001
 * @version   $Revision$
 */
public class WorkBench extends AbstractTestElement implements Serializable
{
    private static List itemsCanAdd = null;
    private boolean isRootNode;

    /**
     * Constructor for the WorkBench object.
     */
    public WorkBench(String name, boolean isRootNode)
    {
        setName(name);
    }

    public WorkBench()
    {
    }
}
