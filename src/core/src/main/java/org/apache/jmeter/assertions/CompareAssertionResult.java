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

package org.apache.jmeter.assertions;

import java.io.Serializable;

public class CompareAssertionResult extends AssertionResult {
    private static final long serialVersionUID = 1;

    private final ResultHolder comparedResults = new ResultHolder();

    /**
     * For testing only
     * @deprecated Use the other ctor
     */
    @Deprecated
    public CompareAssertionResult() { // needs to be public for testing
        super();
    }

    public CompareAssertionResult(String name) {
        super(name);
    }

    public void addToBaseResult(String resultData)
    {
        comparedResults.addToBaseResult(resultData);
    }

    public void addToSecondaryResult(String resultData)
    {
        comparedResults.addToSecondaryResult(resultData);
    }

    public String getBaseResult()
    {
        return comparedResults.baseResult;
    }

    public String getSecondaryResult()
    {
        return comparedResults.secondaryResult;
    }

    private static class ResultHolder implements Serializable
    {
        private static final long serialVersionUID = 1L;
        private String baseResult;
        private String secondaryResult;

        public ResultHolder()
        {
        }

        public void addToBaseResult(String r)
        {
            if(baseResult == null)
            {
                baseResult = r;
            }
            else
            {
                baseResult = baseResult + "\n\n" + r; //$NON-NLS-1$
            }
        }

        public void addToSecondaryResult(String r)
        {
            if(secondaryResult == null)
            {
                secondaryResult = r;
            }
            else
            {
                secondaryResult = secondaryResult + "\n\n" + r; //$NON-NLS-1$
            }
        }
    }
}
