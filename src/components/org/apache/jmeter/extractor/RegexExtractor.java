/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.apache.jmeter.extractor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.apache.oro.text.MalformedCachePatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Util;

// @see org.apache.jmeter.extractor.TestRegexExtractor for unit tests

public class RegexExtractor extends AbstractTestElement implements PostProcessor, Serializable {


	private static final Logger log = LoggingManager.getLoggerForClass();

	// What to match against. N.B. do not change the string value or test plans will break!
	private static final String MATCH_AGAINST = "RegexExtractor.useHeaders"; // $NON-NLS-1$
	/*
	 * Permissible values: 
	 *  true - match against headers
	 *  false or absent - match against body (this was the original default)
	 *  URL - match against URL
	 *  These are passed to the setUseField() method
	 *  
	 *  Do not change these values!
	*/
	public static final String USE_HDRS = "true"; // $NON-NLS-1$
	public static final String USE_BODY = "false"; // $NON-NLS-1$
    public static final String USE_URL = "URL"; // $NON-NLS-1$

	private static final String REGEX = "RegexExtractor.regex"; // $NON-NLS-1$

	private static final String REFNAME = "RegexExtractor.refname"; // $NON-NLS-1$

	private static final String MATCH_NUMBER = "RegexExtractor.match_number"; // $NON-NLS-1$

	private static final String DEFAULT = "RegexExtractor.default"; // $NON-NLS-1$

	private static final String TEMPLATE = "RegexExtractor.template"; // $NON-NLS-1$

    private static final String REF_MATCH_NR = "_matchNr"; // $NON-NLS-1$

    private static final String UNDERSCORE = "_";  // $NON-NLS-1$


    private Object[] template = null;

	/**
	 * Parses the response data using regular expressions and saving the results
	 * into variables for use later in the test.
	 * 
	 * @see org.apache.jmeter.processor.PostProcessor#process()
	 */
	public void process() {
		initTemplate();
		JMeterContext context = getThreadContext();
		SampleResult previousResult = context.getPreviousResult();
		if (previousResult == null) {
			return;
		}
		log.debug("RegexExtractor processing result");

		// Fetch some variables
		JMeterVariables vars = context.getVariables();
		String refName = getRefName();
		int matchNumber = getMatchNumber();

		final String defaultValue = getDefaultValue();
        if (defaultValue.length() > 0){// Only replace default if it is provided
            vars.put(refName, defaultValue);
        }

		Perl5Matcher matcher = JMeterUtils.getMatcher();
		String inputString = 
			useUrl() ? previousResult.getUrlAsString() // Bug 39707 
			:
			useHeaders() ? previousResult.getResponseHeaders()
		    : previousResult.getResponseDataAsString() // Bug 36898
		    ; 
   		if (log.isDebugEnabled()) {
   			log.debug("Input = " + inputString);
   		}
		PatternMatcherInput input = new PatternMatcherInput(inputString);
   		String regex = getRegex();
		if (log.isDebugEnabled()) {
			log.debug("Regex = " + regex);
   		}
		try {
			Pattern pattern = JMeterUtils.getPatternCache().getPattern(regex, Perl5Compiler.READ_ONLY_MASK);
			List matches = new ArrayList();
			int x = 0;
			boolean done = false;
			do {
				if (matcher.contains(input, pattern)) {
					log.debug("RegexExtractor: Match found!");
					matches.add(matcher.getMatch());
				} else {
					done = true;
				}
				x++;
			} while (x != matchNumber && !done);

			try {
				MatchResult match;
				if (matchNumber >= 0) {// Original match behaviour
					match = getCorrectMatch(matches, matchNumber);
					if (match != null) {
						vars.put(refName, generateResult(match));
						saveGroups(vars, refName, match);
					} else {
                        vars.remove(refName + "_g"); // $NON-NLS-1$
                        vars.remove(refName + "_g0"); // $NON-NLS-1$
                        vars.remove(refName + "_g1"); // $NON-NLS-1$
                        //TODO - remove other groups if present?
                    }
				} else // < 0 means we save all the matches
				{
					int prevCount = 0;
					String prevString = vars.get(refName + REF_MATCH_NR);
					if (prevString != null) {
						try {
							prevCount = Integer.parseInt(prevString);
						} catch (NumberFormatException e1) {
                            log.warn("Could not parse "+prevString+" "+e1);
						}
					}
					vars.put(refName + REF_MATCH_NR, "" + matches.size());// Save the count
					for (int i = 1; i <= matches.size(); i++) {
						match = getCorrectMatch(matches, i);
						if (match != null) {
							vars.put(refName + UNDERSCORE + i, generateResult(match));
							saveGroups(vars, refName + UNDERSCORE + i, match);
						}
					}
					for (int i = matches.size() + 1; i <= prevCount; i++) {
						vars.remove(refName + UNDERSCORE + i);
                        // Remove known groups
						vars.remove(refName + UNDERSCORE + i + "_g0"); // $NON-NLS-1$
						vars.remove(refName + UNDERSCORE + i + "_g1"); // $NON-NLS-1$
						// TODO remove other groups if present?
					}
				}
			} catch (RuntimeException e) {
				log.warn("Error while generating result");
			}
		} catch (MalformedCachePatternException e) {
			log.warn("Error in pattern: " + regex);
		}
	}

	private void saveGroups(JMeterVariables vars, String basename, MatchResult match) {
		StringBuffer buf = new StringBuffer();
        buf.append(basename);
        buf.append("_g"); // $NON-NLS-1$
        int pfxlen=buf.length();
        //Note: match.groups() includes group 0
		for (int x = 0; x < match.groups(); x++) {
			buf.append(x);
			vars.put(buf.toString(), match.group(x));
			buf.setLength(pfxlen);
		}
        vars.put(buf.toString(), Integer.toString(match.groups()-1));
	}

	public Object clone() {
		RegexExtractor cloned = (RegexExtractor) super.clone();
		cloned.template = this.template;
		return cloned;
	}

	private String generateResult(MatchResult match) {
		StringBuffer result = new StringBuffer();
		for (int a = 0; a < template.length; a++) {
			log.debug("RegexExtractor: Template piece #" + a + " = " + template[a]);
			if (template[a] instanceof String) {
				result.append(template[a]);
			} else {
				result.append(match.group(((Integer) template[a]).intValue()));
			}
		}
		log.debug("Regex Extractor result = " + result.toString());
		return result.toString();
	}

	private void initTemplate() {
		if (template != null) {
			return;
		}
		List pieces = new ArrayList();
		List combined = new LinkedList();
		String rawTemplate = getTemplate();
		PatternMatcher matcher = JMeterUtils.getMatcher();
		Pattern templatePattern = JMeterUtils.getPatternCache().getPattern("\\$(\\d+)\\$"  // $NON-NLS-1$
                , Perl5Compiler.READ_ONLY_MASK
				& Perl5Compiler.SINGLELINE_MASK);
		log.debug("Pattern = " + templatePattern);
		log.debug("template = " + rawTemplate);
		Util.split(pieces, matcher, templatePattern, rawTemplate);
		PatternMatcherInput input = new PatternMatcherInput(rawTemplate);
		boolean startsWith = isFirstElementGroup(rawTemplate);
		log.debug("template split into " + pieces.size() + " pieces, starts with = " + startsWith);
		if (startsWith) {
			pieces.remove(0);// Remove initial empty entry
		}
		Iterator iter = pieces.iterator();
		while (iter.hasNext()) {
			boolean matchExists = matcher.contains(input, templatePattern);
			if (startsWith) {
				if (matchExists) {
					combined.add(new Integer(matcher.getMatch().group(1)));
				}
				combined.add(iter.next());
			} else {
				combined.add(iter.next());
				if (matchExists) {
					combined.add(new Integer(matcher.getMatch().group(1)));
				}
			}
		}
		if (matcher.contains(input, templatePattern)) {
			log.debug("Template does end with template pattern");
			combined.add(new Integer(matcher.getMatch().group(1)));
		}
		template = combined.toArray();
	}

	private boolean isFirstElementGroup(String rawData) {
		try {
			Pattern pattern = JMeterUtils.getPatternCache().getPattern("^\\$\\d+\\$" // $NON-NLS-1$
                    , Perl5Compiler.READ_ONLY_MASK
					& Perl5Compiler.SINGLELINE_MASK);
			return (JMeterUtils.getMatcher()).contains(rawData, pattern);
		} catch (RuntimeException e) {
			log.error("", e);
			return false;
		}
	}

	/**
	 * Grab the appropriate result from the list.
	 * 
	 * @param matches
	 *            list of matches
	 * @param entry
	 *            the entry number in the list
	 * @return MatchResult
	 */
	private MatchResult getCorrectMatch(List matches, int entry) {
		int matchSize = matches.size();

		if (matchSize <= 0 || entry > matchSize)
			return null;

		if (entry == 0) // Random match
		{
			return (MatchResult) matches.get(JMeterUtils.getRandomInt(matchSize));
		}

		return (MatchResult) matches.get(entry - 1);
	}

	public void setRegex(String regex) {
		setProperty(REGEX, regex);
	}

	public String getRegex() {
		return getPropertyAsString(REGEX);
	}

	public void setRefName(String refName) {
		setProperty(REFNAME, refName);
	}

	public String getRefName() {
		return getPropertyAsString(REFNAME);
	}

	/**
	 * Set which Match to use. This can be any positive number, indicating the
	 * exact match to use, or 0, which is interpreted as meaning random.
	 * 
	 * @param matchNumber
	 */
	public void setMatchNumber(int matchNumber) {
		setProperty(new IntegerProperty(MATCH_NUMBER, matchNumber));
	}

	public void setMatchNumber(String matchNumber) {
		setProperty(MATCH_NUMBER, matchNumber);
	}

	public int getMatchNumber() {
		return getPropertyAsInt(MATCH_NUMBER);
	}

	public String getMatchNumberAsString() {
		return getPropertyAsString(MATCH_NUMBER);
	}

	/**
	 * Sets the value of the variable if no matches are found
	 * 
	 * @param defaultValue
	 */
	public void setDefaultValue(String defaultValue) {
		setProperty(DEFAULT, defaultValue);
	}

	public String getDefaultValue() {
		return getPropertyAsString(DEFAULT);
	}

	public void setTemplate(String template) {
		setProperty(TEMPLATE, template);
	}

	public String getTemplate() {
		return getPropertyAsString(TEMPLATE);
	}

	public boolean useHeaders() {
		return USE_HDRS.equalsIgnoreCase( getPropertyAsString(MATCH_AGAINST));
	}

	// Allow for property not yet being set (probably only applies to Test cases)
	public boolean useBody() {
    	String body = getPropertyAsString(MATCH_AGAINST);
        return body.length()==0 || USE_BODY.equalsIgnoreCase(body);// $NON-NLS-1$
    }

	public boolean useUrl() {
    	String body = getPropertyAsString(MATCH_AGAINST);
        return USE_URL.equalsIgnoreCase(body);
    }
	public void setUseField(String actionCommand) {
		setProperty(MATCH_AGAINST,actionCommand);
	}
}
