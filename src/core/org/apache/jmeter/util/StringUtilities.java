package org.apache.jmeter.util;

import junit.framework.TestCase;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class StringUtilities
{
	
	public static String substitute(String input,String pattern,String sub)
	{
		StringBuffer ret = new StringBuffer();
		int start = 0;
		int index = -1;
		while((index = input.indexOf(pattern,start)) >= start)
		{
			ret.append(input.substring(start,index));
			ret.append(sub);
			start = index + pattern.length();
		}
		ret.append(input.substring(start));
		return ret.toString();
	}
	
	public static class Test extends TestCase
	{
		public Test(String name)
		{
			super(name);
		}
		
		public void testSub1() throws Exception
		{
			String input = "http://jakarta.apache.org/jmeter/index.html";
			String pattern = "jakarta.apache.org";
			String sub = "${server}";
			assertEquals("http://${server}/jmeter/index.html",
					StringUtilities.substitute(input,pattern,sub));
		}
		
		public void testSub2() throws Exception
		{
			String input = "arg1=param1;param1";
			String pattern = "param1";
			String sub = "${value}";
			assertEquals("arg1=${value};${value}",
					StringUtilities.substitute(input,pattern,sub));
		}
		
		public void testSub3() throws Exception
		{
			String input = "jakarta.apache.org";
			String pattern = "jakarta.apache.org";
			String sub = "${server}";
			assertEquals("${server}",
					StringUtilities.substitute(input,pattern,sub));
		}
		
	}
}
