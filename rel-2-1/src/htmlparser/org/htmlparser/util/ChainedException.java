// $Header$
/*
 * ====================================================================
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

// The developers of JMeter and Apache are greatful to the developers
// of HTMLParser for giving Apache Software Foundation a non-exclusive
// license. The performance benefits of HTMLParser are clear and the
// users of JMeter will benefit from the hard work the HTMLParser
// team. For detailed information about HTMLParser, the project is
// hosted on sourceforge at http://htmlparser.sourceforge.net/.
//
// HTMLParser was originally created by Somik Raha in 2000. Since then
// a healthy community of users has formed and helped refine the
// design so that it is able to tackle the difficult task of parsing
// dirty HTML. Derrick Oswald is the current lead developer and was kind
// enough to assist JMeter.
// 
// This class was contributed by 
// Claude Duguay
//
package org.htmlparser.util;

/**
 * Support for chained exceptions in code that predates Java 1.4.
 * A chained exception can use a Throwable argument to reference
 * a lower level exception. The chained exception provides a
 * stack trace that includes the message and any throwable
 * exception included as an argument in the chain.
 *
 * For example:
 *
 *   ApplicationException: Application problem encountered;
 *   ProcessException: Unable to process document;
 *   java.io.IOException: Unable to open 'filename.ext'
 *     at ChainedExceptionTest.openFile(ChainedExceptionTest.java:19)
 *     at ChainedExceptionTest.processFile(ChainedExceptionTest.java:27)
 *     at ChainedExceptionTest.application(ChainedExceptionTest.java:40)
 *     at ChainedExceptionTest.main(ChainedExceptionTest.java:52)
 *
 * Represents the output from two nested exceptions. The outside
 * exception is a subclass of ChainedException called
 * ApplicationException, which includes a throwable reference.
 * The throwable reference is also a subclass of ChainedException,
 * called ProcessException, which in turn includes a reference to
 * a standard IOException. In each case, the message is increasingly
 * specific about the nature of the problem. The end user may only
 * see the application exception, but debugging is greatly
 * enhanced by having more details in the stack trace.
 *
 * @author Claude Duguay
 **/

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ChainedException extends Exception {
	protected Throwable throwable;

	public ChainedException() {
	}

	public ChainedException(String message) {
		super(message);
	}

	public ChainedException(Throwable throwable) {
		this.throwable = throwable;
	}

	public ChainedException(String message, Throwable throwable) {
		super(message);
		this.throwable = throwable;
	}

	public String[] getMessageChain() {
		List list = getMessageList();
		String[] chain = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			chain[i] = (String) list.get(i);
		}
		return chain;
	}

	public List getMessageList() {
		ArrayList list = new ArrayList();
		list.add(getMessage());
		if (throwable != null) {
			if (throwable instanceof ChainedException) {
				ChainedException chain = (ChainedException) throwable;
				list.addAll(chain.getMessageList());
			} else {
				String message = throwable.getMessage();
				if (message != null && !message.equals("")) {
					list.add(message);
				}
			}
		}
		return list;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public void printStackTrace() {
		printStackTrace(System.err);
	}

	public void printStackTrace(PrintStream out) {
		synchronized (out) {
			if (throwable != null) {
				out.println(getClass().getName() + ": " + getMessage() + ";");
				throwable.printStackTrace(out);
			} else {
				super.printStackTrace(out);
			}
		}
	}

	public void printStackTrace(PrintWriter out) {
		synchronized (out) {
			if (throwable != null) {
				out.println(getClass().getName() + ": " + getMessage() + ";");
				throwable.printStackTrace(out);
			} else {
				super.printStackTrace(out);
			}
		}
	}
}
