package org.apache.jmeter.protocol.http.util.accesslog;

/**
 * Title:		Business Inference RuleML Editor<br>
 * Copyright:	Copyright (c) 2002<br>
 * Company:		Business Inference<br>
 * License:<br>
 * <br>
 * Stock license insert here.<br>
 * <br>
 * Description:<br>
 * <br>
 * Author:	Peter Lin<br>
 * Version: 	0.1<br>
 * Created on:	Jun 23, 2003<br>
 * Last Modified:	5:04:25 PM<br>
 */

public class NVPair {

	protected String NAME = new String();
	protected String VALUE = new String();
	
	public NVPair() {
	}
	
	/**
	 * The constructor takes a name and value
	 * which represent HTTP request parameters.
	 * @param name
	 * @param value
	 */
	public NVPair(String name, String value) {
		this.NAME = name;
		this.VALUE = value;
	}

	/**
	 * Set the name
	 * @param name
	 */	
	public void setName(String name) {
		this.NAME = name;
	}

	/**
	 * Set the value
	 * @param value
	 */	
	public void setValue(String value) {
		this.VALUE = value;
	}

	/**
	 * Return the name
	 * @return name
	 */	
	public String getName() {
		return this.NAME;
	}

	/**
	 * Return the value
	 * @return value
	 */	
	public String getValue() {
		return this.VALUE;
	}
}
