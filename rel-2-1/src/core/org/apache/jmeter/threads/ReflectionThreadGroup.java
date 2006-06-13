// $Header$
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

// TODO - is this used?
package org.apache.jmeter.threads;

/**
 * ThreadGroup used for reflection purposes. {@link ThreadGroup} has a
 * {@link org.apache.jmeter.control.LoopController} which loops as many times as
 * specified on the ThreadGroup gui. During reflection, we don't need the
 * <code>LoopController</code> because we need to run the Sampler (for
 * example, <code>JNDISampler</code>) only once. Thus we create this
 * <code>ReflectionThreadGroup</code> which has a {link #nextEntry()} which
 * doesn't make use of the <code>LoopController</code>.
 * 
 * @author Khor Soon Hin
 * @version $Revision$
 */
public class ReflectionThreadGroup extends ThreadGroup {

}
