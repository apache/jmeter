package org.apache.jmeter.protocol.http.modifier;
import java.io.Serializable;
import junit.framework.TestCase;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ResponseBasedModifier;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
/**
 * @author mstover
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class URLRewritingModifier
	extends AbstractTestElement
	implements Serializable, ResponseBasedModifier
{
	transient private static Logger log =
		Hierarchy.getDefaultHierarchy().getLoggerFor("jmeter.protocol.http");
	private Pattern case1, case2, case3;
	transient Perl5Compiler compiler = new Perl5Compiler();
	private final static String ARGUMENT_NAME = "argument_name";
	private final static String PATH_EXTENSION = "path_extension";
	/**
	 * @see ResponseBasedModifier#modifyEntry(Sampler, SampleResult)
	 */
	public boolean modifyEntry(Sampler sampler, SampleResult responseText)
	{
		if(case1 == null)
		{
			initRegex(getArgumentName());
		}
		String text = new String(responseText.getResponseData());
		Perl5Matcher matcher = new Perl5Matcher();
		String value = "";
		if (matcher.contains(text, case1))
		{
			MatchResult result = matcher.getMatch();
			value = result.group(1);
		}
		else if (matcher.contains(text, case2))
		{
			MatchResult result = matcher.getMatch();
			value = result.group(1);
		}
		else if (matcher.contains(text, case3))
		{
			MatchResult result = matcher.getMatch();
			value = result.group(1);
		}
		modify((HTTPSampler) sampler, value);
		if (value.length() > 0)
		{
			return true;
		}
		return false;
	}
	private void modify(HTTPSampler sampler, String value)
	{
		if (isPathExtension())
		{
			sampler.setPath(
				sampler.getPath() + ";" + getArgumentName() + "=" + value);
		}
		else
		{
			sampler.getArguments().removeArgument(getArgumentName());
			sampler.getArguments().addArgument(
				new HTTPArgument(getArgumentName(), value, true));
		}
	}
	public void setArgumentName(String argName)
	{
		setProperty(ARGUMENT_NAME, argName);
		case1 = case2 = case3 = null;
	}
	private void initRegex(String argName)
	{
		try
		{
			case1 = compiler.compile(argName + "=([^\"'>& \n\r]*)[& \\n\\r\"'>]?$?");
			case2 =
				compiler.compile(
					"[Nn][Aa][Mm][Ee]=\""
						+ argName
						+ "\"[^>]+[vV][Aa][Ll][Uu][Ee]=\"([^\"]*)\"");
			case3 =
				compiler.compile(
					"[vV][Aa][Ll][Uu][Ee]=\"([^\"]*)\"[^>]+[Nn][Aa][Mm][Ee]=\""
						+ argName
						+ "\"");
		}
		catch (MalformedPatternException e)
		{
			log.error("", e);
		}
	}
	public String getArgumentName()
	{
		return getPropertyAsString(ARGUMENT_NAME);
	}
	public void setPathExtension(boolean pathExt)
	{
		setProperty(PATH_EXTENSION, new Boolean(pathExt));
	}
	public boolean isPathExtension()
	{
		return getPropertyAsBoolean(PATH_EXTENSION);
	}
	public static class Test extends TestCase
	{
		SampleResult response;
		public Test(String name)
		{
			super(name);
		}
		public void setUp()
		{
		}
		public void testGrabSessionId() throws Exception
		{
			String html =
				"location: http://server.com/index.html?session_id=jfdkjdkf%jddkfdfjkdjfdf";
			response = new SampleResult();
			response.setResponseData(html.getBytes());
			URLRewritingModifier mod = new URLRewritingModifier();
			mod.setArgumentName("session_id");
			HTTPSampler sampler = new HTTPSampler();
			sampler.setDomain("server.com");
			sampler.setPath("index.html");
			sampler.setMethod(HTTPSampler.GET);
			sampler.setProtocol("http");
			sampler.addArgument("session_id", "adfasdfdsafasdfasd");
			mod.modifyEntry(sampler, response);
			Arguments args = sampler.getArguments();
			assertEquals(
				"jfdkjdkf%jddkfdfjkdjfdf",
				((Argument) args.getArguments().get(0)).getValue());
			assertEquals(
				"http://server.com:80/index.html?session_id=jfdkjdkf%jddkfdfjkdjfdf",
				sampler.toString());
		}
		public void testGrabSessionId2() throws Exception
		{
			String html =
				"<a href=\"http://server.com/index.html?session_id=jfdkjdkfjddkfdfjkdjfdf\">";
			response = new SampleResult();
			response.setResponseData(html.getBytes());
			URLRewritingModifier mod = new URLRewritingModifier();
			mod.setArgumentName("session_id");
			HTTPSampler sampler = new HTTPSampler();
			sampler.setDomain("server.com");
			sampler.setPath("index.html");
			sampler.setMethod(HTTPSampler.GET);
			sampler.setProtocol("http");
			mod.modifyEntry(sampler, response);
			Arguments args = sampler.getArguments();
			assertEquals(
				"jfdkjdkfjddkfdfjkdjfdf",
				((Argument) args.getArguments().get(0)).getValue());
		}
		
		public void testGrabSessionId3() throws Exception
		{
			String html =
				"href='index.html?session_id=jfdkjdkfjddkfdfjkdjfdf'";
			response = new SampleResult();
			response.setResponseData(html.getBytes());
			URLRewritingModifier mod = new URLRewritingModifier();
			mod.setArgumentName("session_id");
			HTTPSampler sampler = new HTTPSampler();
			sampler.setDomain("server.com");
			sampler.setPath("index.html");
			sampler.setMethod(HTTPSampler.GET);
			sampler.setProtocol("http");
			mod.modifyEntry(sampler, response);
			Arguments args = sampler.getArguments();
			assertEquals(
				"jfdkjdkfjddkfdfjkdjfdf",
				((Argument) args.getArguments().get(0)).getValue());
		}
	}
}
