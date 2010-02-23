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
