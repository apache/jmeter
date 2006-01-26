/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.jmeter.protocol.http.modifier;

import java.io.FileInputStream;
import java.net.URL;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jorphan.io.TextFile;

/**
 * @author Michael Stover
 * @version $Revision$
 */
public class TestAnchorModifier extends JMeterTestCase {
		public TestAnchorModifier(String name) {
			super(name);
		}

		private JMeterContext jmctx = null;

		public void setUp() {
			jmctx = JMeterContextService.getContext();
		}

		public void testProcessingHTMLFile(String HTMLFileName) throws Exception {
			HTTPSamplerBase config = (HTTPSamplerBase) SaveService.loadTree(
					new FileInputStream(System.getProperty("user.dir") + "/testfiles/load_bug_list.jmx")).getArray()[0];
			config.setRunningVersion(true);
			HTTPSampleResult result = new HTTPSampleResult();
			HTTPSamplerBase context = (HTTPSamplerBase) SaveService.loadTree(
					new FileInputStream(System.getProperty("user.dir") + "/testfiles/Load_JMeter_Page.jmx")).getArray()[0];
			jmctx.setCurrentSampler(context);
			jmctx.setCurrentSampler(config);
			result.setResponseData(new TextFile(System.getProperty("user.dir") + HTMLFileName).getText().getBytes());
			result.setSampleLabel(context.toString());
			result.setSamplerData(context.toString());
			result.setURL(new URL("http://issues.apache.org/fakepage.html"));
			jmctx.setPreviousResult(result);
			AnchorModifier modifier = new AnchorModifier();
			modifier.setThreadContext(jmctx);
			modifier.process();
			assertEquals("http://issues.apache.org/bugzilla/buglist.cgi?"
					+ "bug_status=NEW&bug_status=ASSIGNED&bug_status=REOPENED"
					+ "&email1=&emailtype1=substring&emailassigned_to1=1"
					+ "&email2=&emailtype2=substring&emailreporter2=1" + "&bugidtype=include&bug_id=&changedin=&votes="
					+ "&chfieldfrom=&chfieldto=Now&chfieldvalue="
					+ "&product=JMeter&short_desc=&short_desc_type=substring"
					+ "&long_desc=&long_desc_type=substring&bug_file_loc=" + "&bug_file_loc_type=substring&keywords="
					+ "&keywords_type=anywords" + "&field0-0-0=noop&type0-0-0=noop&value0-0-0="
					+ "&cmdtype=doit&order=Reuse+same+sort+as+last+time", config.toString());
			config.recoverRunningVersion();
			assertEquals("http://issues.apache.org/bugzilla/buglist.cgi?"
					+ "bug_status=.*&bug_status=.*&bug_status=.*&email1="
					+ "&emailtype1=substring&emailassigned_to1=1&email2=" + "&emailtype2=substring&emailreporter2=1"
					+ "&bugidtype=include&bug_id=&changedin=&votes=" + "&chfieldfrom=&chfieldto=Now&chfieldvalue="
					+ "&product=JMeter&short_desc=&short_desc_type=substring"
					+ "&long_desc=&long_desc_type=substring&bug_file_loc=" + "&bug_file_loc_type=substring&keywords="
					+ "&keywords_type=anywords&field0-0-0=noop" + "&type0-0-0=noop&value0-0-0=&cmdtype=doit"
					+ "&order=Reuse+same+sort+as+last+time", config.toString());
		}

		public void testModifySampler() throws Exception {
			testProcessingHTMLFile("/testfiles/jmeter_home_page.html");
		}

		public void testModifySamplerWithRelativeLink() throws Exception {
			testProcessingHTMLFile("/testfiles/jmeter_home_page_with_relative_links.html");
		}

		// * Feature not yet implemented. TODO: implement it.
		public void testModifySamplerWithBaseHRef() throws Exception {
			testProcessingHTMLFile("/testfiles/jmeter_home_page_with_base_href.html");
		}
		// */
}
