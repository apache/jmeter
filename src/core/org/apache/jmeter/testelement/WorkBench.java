package org.apache.jmeter.testelement;

import java.io.Serializable;

/**
 * @author    Michael Stover
 * Created   March 13, 2001
 * @version   $Revision$ Last updated: $Date$
 */
public class WorkBench extends AbstractTestElement implements Serializable
{

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
