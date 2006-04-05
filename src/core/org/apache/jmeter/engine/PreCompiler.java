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

package org.apache.jmeter.engine;

import java.util.Map;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.engine.util.ValueReplacer;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author Michael Stover
 * @author <a href="mailto:jsalvata@apache.org">Jordi Salvat i Alabart</a>
 * @version $Revision$ updated on $Date$
 */
public class PreCompiler implements HashTreeTraverser {
	private static final Logger log = LoggingManager.getLoggerForClass();

	private ValueReplacer replacer;

	public PreCompiler() {
		replacer = new ValueReplacer();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see HashTreeTraverser#addNode(Object, HashTree)
	 */
	public void addNode(Object node, HashTree subTree) {
        if(node instanceof TestElement)
        {
            try {
                replacer.replaceValues((TestElement) node);
            } catch (InvalidVariableException e) {
                log.error("invalid variables", e);
            }
        }
		if (node instanceof TestPlan) {
            ((TestPlan)node).prepareForPreCompile(); //A hack to make user-defined variables in the testplan element more dynamic
            Map args = ((TestPlan) node).getUserDefinedVariables();
			replacer.setUserDefinedVariables(args);
			JMeterVariables vars = new JMeterVariables();
			vars.putAll(args);
			JMeterContextService.getContext().setVariables(vars);
		} 

		if (node instanceof Arguments) {
            ((Arguments)node).setRunningVersion(true);
			Map args = ((Arguments) node).getArgumentsAsMap();
			replacer.addVariables(args);
			JMeterContextService.getContext().getVariables().putAll(args);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see HashTreeTraverser#subtractNode()
	 */
	public void subtractNode() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see HashTreeTraverser#processPath()
	 */
	public void processPath() {
	}
}
