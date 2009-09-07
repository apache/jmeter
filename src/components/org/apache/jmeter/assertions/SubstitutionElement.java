package org.apache.jmeter.assertions;

import org.apache.jmeter.testelement.AbstractTestElement;

public class SubstitutionElement extends AbstractTestElement {
	private static final long serialVersionUID = 1;
	
	// These constants are used both for the JMX file and for the setters/getters
	public static final String REGEX = "regex"; // $NON-NLS-1$
	
	public static final String SUBSTITUTE = "substitute"; // $NON-NLS-1$

	public SubstitutionElement() {
		super();
	}
	
	public String getRegex()
	{
		return getProperty(REGEX).getStringValue();
	}
	
	public void setRegex(String regex)
	{
		setProperty(REGEX,regex);
	}
	
	public String getSubstitute()
	{
		return getProperty(SUBSTITUTE).getStringValue();
	}
	
	public void setSubstitute(String sub)
	{
		setProperty(SUBSTITUTE,sub);
	}
	
	

}
