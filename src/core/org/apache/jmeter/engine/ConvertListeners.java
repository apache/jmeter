package org.apache.jmeter.engine;

import java.rmi.RemoteException;
import java.util.Iterator;

import org.apache.jmeter.samplers.RemoteListenerWrapper;
import org.apache.jmeter.samplers.RemoteSampleListener;
import org.apache.jmeter.samplers.RemoteSampleListenerImpl;
import org.apache.jmeter.samplers.RemoteSampleListenerWrapper;
import org.apache.jmeter.samplers.RemoteTestListenerWrapper;
import org.apache.jmeter.samplers.Remoteable;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/**
 * @author mstover
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class ConvertListeners implements HashTreeTraverser {

	transient private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.engine");
    /* (non-Javadoc)
	 * @see HashTreeTraverser#addNode(Object, HashTree)
	 */
	public void addNode(Object node, HashTree subTree) {
		if(node instanceof ThreadGroup)
			{
				log.info("num threads = "+((ThreadGroup)node).getNumThreads());
			}
		Iterator iter = subTree.list().iterator();
		while(iter.hasNext())
		{
			Object item = iter.next();
			if(item instanceof ThreadGroup)
			{
				log.info("num threads = "+((ThreadGroup)item).getNumThreads());
			}
			if(item instanceof Remoteable && (item instanceof TestListener || item instanceof SampleListener))
			{
				try {
					RemoteSampleListener rtl = new RemoteSampleListenerImpl(item);
					if(item instanceof TestListener && item instanceof SampleListener)
					{
						RemoteListenerWrapper wrap = new RemoteListenerWrapper(rtl);
						subTree.replace(item,wrap);
					}
					else if(item instanceof TestListener)
					{
						RemoteTestListenerWrapper wrap = new RemoteTestListenerWrapper(rtl);
						subTree.replace(item,wrap);
					}
					else
					{
						RemoteSampleListenerWrapper wrap = new RemoteSampleListenerWrapper(rtl);
						subTree.replace(item,wrap);
					}
				} catch(RemoteException e) {
					log.error("",e);
				}
			}
		}
	}

    /* (non-Javadoc)
	 * @see HashTreeTraverser#subtractNode()
	 */
	public void subtractNode() {
	}

    /* (non-Javadoc)
	 * @see HashTreeTraverser#processPath()
	 */
	public void processPath() {
	}

}
