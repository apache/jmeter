package org.apache.jmeter.functions;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Util;

/**
 * @author mstover
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class RegexFunction extends AbstractFunction {
	
	public static final String ALL = "ALL";
	public static final String RAND = "RAND";
	public static final String KEY = "__regexFunction";	
	
	private static Random rand = new Random();
	private static List desc = new LinkedList();
	Pattern searchPattern;
	Object[] template;
	String valueIndex,defaultValue,between;
	PatternCompiler compiler = new Perl5Compiler();
	Pattern templatePattern;
	
	static
	{
		desc.add(JMeterUtils.getResString("regexfunc_param_1"));
		desc.add(JMeterUtils.getResString("regexfunc_param_2"));
		desc.add(JMeterUtils.getResString("regexfunc_param_3"));
		desc.add(JMeterUtils.getResString("regexfunc_param_4"));
		desc.add(JMeterUtils.getResString("regexfunc_param_5"));
	}
	
	public RegexFunction()
	{
		try {
			templatePattern = compiler.compile("\\$(\\d+)\\$");
		} catch(MalformedPatternException e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * @see Variable#getValue(SampleResult, Sampler)
	 */
	public String execute(SampleResult previousResult,Sampler currentSampler) 
	{
		if(previousResult == null || previousResult.getResponseData() == null)
		{
			return defaultValue;
		}
		List collectAllMatches = new ArrayList();
		try {
			PatternMatcher matcher = new Perl5Matcher();
			String responseText = new String(previousResult.getResponseData());
			PatternMatcherInput input = new PatternMatcherInput(responseText);
			while(matcher.contains(input,searchPattern))
			{
				MatchResult match = matcher.getMatch();
				collectAllMatches.add(match);
			}
		} catch(NumberFormatException e) {
			e.printStackTrace();
		}
		if(collectAllMatches.size() == 0)
		{
			return defaultValue;
		}
		if(valueIndex.equals(ALL))
		{
			StringBuffer value = new StringBuffer();
			Iterator it = collectAllMatches.iterator();
			boolean first = true;
			while(it.hasNext())
			{
				if(!first)
				{
					value.append(between);
					first = false;
				}
				value.append(generateResult((MatchResult)it.next()));
			}
			return value.toString();
		}
		else if(valueIndex.equals(RAND))
		{
			return generateResult((MatchResult)collectAllMatches.get(
					rand.nextInt(collectAllMatches.size())));
		}
		else
		{
			try {
				int index = Integer.parseInt(valueIndex) - 1;
				return generateResult((MatchResult)collectAllMatches.get(index));
			} catch(NumberFormatException e) {
				float ratio = Float.parseFloat(valueIndex);
				return generateResult((MatchResult)collectAllMatches.get(
						(int)(collectAllMatches.size() * ratio + .5) - 1));
			}
		}			
	}
	
	public List getArgumentDesc()
	{
		return desc;
	}
	
	private String generateResult(MatchResult match)
	{
		StringBuffer result = new StringBuffer();
		for(int a = 0;a < template.length;a++)
		{
			if(template[a] instanceof String)
			{
				result.append(template[a]);
			}
			else
			{
				result.append(match.group(((Integer)template[a]).intValue()));
			}
		}
		return result.toString();
	}
	
	public String getReferenceKey()
	{
		return KEY;
	}
	
	public void setJMeterVariables(JMeterVariables xxx)
	{
	}
	
	public void setParameters(String params) throws InvalidVariableException
	{
		try
		{
			Iterator tk = parseArguments(params).iterator();
			valueIndex = "1";
			between = "";
			defaultValue = URLDecoder.decode(params);
			searchPattern = compiler.compile((String)tk.next());			
			generateTemplate((String)tk.next());
			if(tk.hasNext())
			{
				valueIndex = (String)tk.next();
			}
			if(tk.hasNext())
			{
				between = (String)tk.next();
			}
			if(tk.hasNext())
			{
				defaultValue = (String)tk.next();
			}
		} catch(MalformedPatternException e) {
				e.printStackTrace();
				throw new InvalidVariableException("Bad regex pattern");
		}
		catch(Exception e)
		{
			throw new InvalidVariableException(e.getMessage());
		}
	}
	
	private void generateTemplate(String rawTemplate)
	{
		List pieces = new ArrayList();
		List combined = new LinkedList();
		PatternMatcher matcher = new Perl5Matcher();
		Util.split(pieces,new Perl5Matcher(),templatePattern,rawTemplate);		
		PatternMatcherInput input = new PatternMatcherInput(rawTemplate);
		int count = 0;
		Iterator iter = pieces.iterator();
		boolean startsWith = isFirstElementGroup(rawTemplate);
		while(iter.hasNext())
		{
			boolean matchExists = matcher.contains(input,templatePattern);
			if(startsWith)
			{
				if(matchExists)
				{
					combined.add(new Integer(matcher.getMatch().group(1)));
				}
				combined.add(iter.next());
			}
			else
			{
				combined.add(iter.next());
				if(matchExists)
				{
					combined.add(new Integer(matcher.getMatch().group(1)));
				}
			}
		}
		if(matcher.contains(input,templatePattern))
		{
			combined.add(new Integer(matcher.getMatch().group(1)));
		}	
		template = combined.toArray();	
	}
	
	private boolean isFirstElementGroup(String rawData)
	{
		try {
			Pattern pattern = compiler.compile("^\\$\\d+\\$");
			return new Perl5Matcher().contains(rawData,pattern);
		} catch(MalformedPatternException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static class Test extends TestCase
	{
		RegexFunction variable;
		SampleResult result;
		
		public Test(String name)
		{
			super(name);
		}
		
		public void setUp()
		{
			variable = new RegexFunction();		
			result = new SampleResult();
			String data = "<company-xmlext-query-ret><row><value field=\"RetCode\">LIS_OK</value><value"+
			" field=\"RetCodeExtension\"></value><value field=\"alias\"></value><value"+
			" field=\"positioncount\"></value><value field=\"invalidpincount\">0</value><value"+
			" field=\"pinposition1\">1</value><value"+
			" field=\"pinpositionvalue1\"></value><value"+
			" field=\"pinposition2\">5</value><value"+
			" field=\"pinpositionvalue2\"></value><value"+
			" field=\"pinposition3\">6</value><value"+
			" field=\"pinpositionvalue3\"></value></row></company-xmlext-query-ret>";
			result.setResponseData(data.getBytes());
		}
		
		public void testVariableExtraction() throws Exception
		{
			variable.setParameters(URLEncoder.encode("<value field=\"(pinposition\\d+)\">(\\d+)</value>")+",$2$,2");
			
			String match = variable.execute(result,null);
			assertEquals("5",match);			
		}
		
		public void testVariableExtraction2() throws Exception
		{
			variable.setParameters(URLEncoder.encode("<value field=\"(pinposition\\d+)\">(\\d+)</value>")+",$1$,3");
			String match = variable.execute(result,null);
			assertEquals("pinposition3",match);			
		}
		
		public void testComma() throws Exception
		{
			variable.setParameters(URLEncoder.encode("<value,? field=\"(pinposition\\d+)\">(\\d+)</value>")+",$1$,3");
			String match = variable.execute(result,null);
			assertEquals("pinposition3",match);			
		}
		
		public void testVariableExtraction3() throws Exception
		{
			variable.setParameters(URLEncoder.encode("<value field=\"(pinposition\\d+)\">(\\d+)</value>")+
					",_$1$,.5");
			String match = variable.execute(result,null);
			assertEquals("_pinposition2",match);			
		}
		
		public void testVariableExtraction4() throws Exception
		{
			variable.setParameters(URLEncoder.encode(
					"<value field=\"(pinposition\\d+)\">(\\d+)</value>")+","+URLEncoder.encode("$2$, ")+
					",.333");
			
			String match = variable.execute(result,null);
			assertEquals("1, ",match);			
		}
		
		public void testDefaultValue() throws Exception
		{
			variable.setParameters(URLEncoder.encode(
					"<value,, field=\"(pinposition\\d+)\">(\\d+)</value>")+","+URLEncoder.encode("$2$, ")+
					",.333,,No Value Found");
			
			String match = variable.execute(result,null);
			assertEquals("No Value Found",match);			
		}
	}

}
