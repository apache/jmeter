/*
 * Created on May 20, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.apache.jmeter.engine;

import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;

/**
 * @author mstover
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TurnElementsOn implements HashTreeTraverser
{

   /* (non-Javadoc)
    * @see org.apache.jorphan.collections.HashTreeTraverser#addNode(java.lang.Object, org.apache.jorphan.collections.HashTree)
    */
   public void addNode(Object node, HashTree subTree)
   {
      if (node instanceof TestElement && !(node instanceof TestPlan))
      {
         ((TestElement)node).setRunningVersion(true);
      }

   }

   /* (non-Javadoc)
    * @see org.apache.jorphan.collections.HashTreeTraverser#subtractNode()
    */
   public void subtractNode()
   {
   }

   /* (non-Javadoc)
    * @see org.apache.jorphan.collections.HashTreeTraverser#processPath()
    */
   public void processPath()
   {
   }

}
