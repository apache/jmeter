/*
 * Created on May 25, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.apache.jorphan.math;

import java.util.Comparator;

/**
 * @author pete
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class NumberComparator implements Comparator {

	/**
	 * 
	 */
	public NumberComparator() {
		super();
	}

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object val1, Object val2) {
		Number[] n1 = (Number[])val1;
		Number[] n2 = (Number[])val2;
		if (n1[0].longValue() < n2[0].longValue()){
			return -1;
		} else if (n1[0].longValue() == n2[0].longValue()){
			return 0;
		} else{
			return 1;
		}
	}

}
