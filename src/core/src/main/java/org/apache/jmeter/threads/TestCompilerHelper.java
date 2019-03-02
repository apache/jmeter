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

package org.apache.jmeter.threads;

import org.apache.jmeter.testelement.TestElement;

/**
 * Bug 53796 - TestCompiler uses static Set which can grow huge
 *
 * This interface is a means to allow the pair data to be saved with the parent
 * instance, thus allowing it to be garbage collected when the thread completes.
 *
 * This uses a bit more memory, as each controller test element includes the data
 * structure to contain the child element. However, there is no need to store the
 * parent element.
 *
 * @since 2.8
 */
public interface TestCompilerHelper {

    /**
     * Add child test element only if it has not already been added.
     * <p>
     * Only for use by TestCompiler.
     *
     * @param child
     *            the {@link TestElement} to be added
     * @return <code>true</code> if the child was added
     */
    boolean addTestElementOnce(TestElement child);

}
