package org.apache.jmeter.threads;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.jmeter.threads.JMeterContext;

/**
 * @author Thad Smith
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class JMeterContextService implements Serializable {

	static private JMeterContextService _instance = null;
	static private Map contextMap = new HashMap();
	
	private JMeterContextService() {
	}
	
	static private void init() {
		if ( _instance == null ) {
			_instance = new JMeterContextService(); 
		}
	}

	static public JMeterContext getContext() {
		
		init();
	
		JMeterContext context = (JMeterContext)contextMap.get(Thread.currentThread().getName());

		if ( context == null ) {
			context = new JMeterContext();
			setContext(context);
		}
		
		return context;
		
	}

	static void setContext(JMeterContext context) {
		init();
		contextMap.put(Thread.currentThread().getName(),context);
	}	

}
