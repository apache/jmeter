/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
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
 * Simple command like parser/handler.
 * A dashed argument is one preceded by a dash character.
 * In a sequence of arguments:
 * 1) If a dashed argument starts with a command character
 *    the rest of the argument, if any, is assume to be a value.
 * 2) If a dashed argument is followed by a non-dashed
 *    argument value. The value is assumed to be associated
 *    with the preceding dashed argument name.
 * 2) If an argument with a dash prefix is not followed by
 *    a non-dashed value, and does not use a command character,
 *    it is assumed to be a flag.
 * 3) If none of the above is true, the argument is a name.
 *
 * Command characters can be added with the addCommand method.
 * Values can be retrieved with the getValue method.
 * Flag states can be retrieved with the getFlag method.
 * Names can be retieved with the getNameCount and getName methods.
 *
 * @author Claude Duguay
**/

public class CommandLine
{
    public static boolean VERBOSE = false;

    protected List commands = new ArrayList();

    protected List flags = new ArrayList();
    protected List names = new ArrayList();
    protected Map values = new HashMap();

    public CommandLine(String chars, String[] args)
    {
        for (int i = 0; i < chars.length(); i++)
        {
            addCommand(chars.charAt(i));
        }
        parse(args);
    }

    public CommandLine(String[] args)
    {
        parse(args);
    }

    protected void parse(String[] args)
    {
        for (int i = 0; i < args.length; i++)
        {
            String thisArg = args[i];
            String nextArg = null;
            if (i < args.length - 1)
            {
                nextArg = args[i + 1];
            }

            if (thisArg.startsWith("-"))
            {
                if (thisArg.length() > 2)
                {
                    Character chr = new Character(thisArg.charAt(1));
                    if (commands.contains(chr))
                    {
                        String key = chr.toString();
                        String val = thisArg.substring(2);
                        if (VERBOSE)
                        {
                            System.out.println("Value " + key + "=" + val);
                        }
                        values.put(key, val);
                    }
                }
                if (nextArg != null && !nextArg.startsWith("-"))
                {
                    String key = thisArg.substring(1);
                    String val = nextArg;
                    if (VERBOSE)
                    {
                        System.out.println("Value " + key + "=" + val);
                    }
                    values.put(key, val);
                    i++;
                }
                else
                {
                    String flag = thisArg.substring(1);
                    flags.add(flag);
                    if (VERBOSE)
                    {
                        System.out.println("Flag " + flag);
                    }
                }
            }
            else
            {
                if (VERBOSE)
                {
                    System.out.println("Name " + thisArg);
                }
                names.add(thisArg);
            }
        }
    }

    public void addCommand(char command)
    {
        commands.add(new Character(command));
    }

    public boolean hasValue(String key)
    {
        return values.containsKey(key);
    }

    public String getValue(String key)
    {
        return (String) values.get(key);
    }

    public boolean getFlag(String key)
    {
        return flags.contains(key);
    }

    public int getNameCount()
    {
        return names.size();
    }

    public String getName(int index)
    {
        return (String) names.get(index);
    }

    public static void main(String[] args)
    {
        CommandLine cmd = new CommandLine("f", args);
    }
}
