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
package org.htmlparser.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple command like parser/handler. A dashed argument is one preceded by a
 * dash character. In a sequence of arguments: 1) If a dashed argument starts
 * with a command character the rest of the argument, if any, is assume to be a
 * value. 2) If a dashed argument is followed by a non-dashed argument value.
 * The value is assumed to be associated with the preceding dashed argument
 * name. 2) If an argument with a dash prefix is not followed by a non-dashed
 * value, and does not use a command character, it is assumed to be a flag. 3)
 * If none of the above is true, the argument is a name.
 * 
 * Command characters can be added with the addCommand method. Values can be
 * retrieved with the getValue method. Flag states can be retrieved with the
 * getFlag method. Names can be retieved with the getNameCount and getName
 * methods.
 * 
 * @author Claude Duguay
 */

public class CommandLine {
	public static boolean VERBOSE = false;

	protected List commands = new ArrayList();

	protected List flags = new ArrayList();

	protected List names = new ArrayList();

	protected Map values = new HashMap();

	public CommandLine(String chars, String[] args) {
		for (int i = 0; i < chars.length(); i++) {
			addCommand(chars.charAt(i));
		}
		parse(args);
	}

	public CommandLine(String[] args) {
		parse(args);
	}

	protected void parse(String[] args) {
		for (int i = 0; i < args.length; i++) {
			String thisArg = args[i];
			String nextArg = null;
			if (i < args.length - 1) {
				nextArg = args[i + 1];
			}

			if (thisArg.startsWith("-")) {
				if (thisArg.length() > 2) {
					Character chr = new Character(thisArg.charAt(1));
					if (commands.contains(chr)) {
						String key = chr.toString();
						String val = thisArg.substring(2);
						if (VERBOSE) {
							System.out.println("Value " + key + "=" + val);
						}
						values.put(key, val);
					}
				}
				if (nextArg != null && !nextArg.startsWith("-")) {
					String key = thisArg.substring(1);
					String val = nextArg;
					if (VERBOSE) {
						System.out.println("Value " + key + "=" + val);
					}
					values.put(key, val);
					i++;
				} else {
					String flag = thisArg.substring(1);
					flags.add(flag);
					if (VERBOSE) {
						System.out.println("Flag " + flag);
					}
				}
			} else {
				if (VERBOSE) {
					System.out.println("Name " + thisArg);
				}
				names.add(thisArg);
			}
		}
	}

	public void addCommand(char command) {
		commands.add(new Character(command));
	}

	public boolean hasValue(String key) {
		return values.containsKey(key);
	}

	public String getValue(String key) {
		return (String) values.get(key);
	}

	public boolean getFlag(String key) {
		return flags.contains(key);
	}

	public int getNameCount() {
		return names.size();
	}

	public String getName(int index) {
		return (String) names.get(index);
	}

	public static void main(String[] args) {
		CommandLine cmd = new CommandLine("f", args);
	}
}
