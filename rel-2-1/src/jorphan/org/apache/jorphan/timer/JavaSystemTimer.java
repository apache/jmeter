// $Header$
/*
 * Copyright 2002-2004 The Apache Software Foundation.
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

package org.apache.jorphan.timer;

/**
 * A package-private implementation of {@link ITimer} based around Java system
 * timer [<code>System.currentTimeMillis()</code> method]. It is used when
 * <code>HRTimer</code> implementation is unavailable.
 * <p>
 * {@link TimerFactory} acts as the Factory for this class.
 * <p>
 * MT-safety: an instance of this class is safe to be used within the same
 * thread only.
 * 
 * @author <a href="mailto:vroubtsov@illinoisalumni.org">Vlad Roubtsov</a>
 * @author Originally published in <a
 *         href="http://www.javaworld.com/javaworld/javaqa/2003-01/01-qa-0110-timing.html">JavaWorld</a>
 * @author <a href="mailto:jeremy_a@bigfoot.com">Jeremy Arnold</a>
 * @version $Revision$
 */
final class JavaSystemTimer extends AbstractTimer {
	protected double getCurrentTime() {
		return System.currentTimeMillis();
	}
}
