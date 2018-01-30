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

package org.apache.jmeter.protocol.http.util.accesslog;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.oro.text.MalformedCachePatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description:<br>
 * <br>
 * LogFilter is a basic implementation of Filter interface. This implementation
 * will keep a record of the filtered strings to avoid repeating the process
 * unnecessarily.
 * <p>
 * The current implementation supports replacing the file extension. The reason
 * for supporting this is from first hand experience porting an existing website
 * to Tomcat + JSP. Later on we may want to provide the ability to replace the
 * whole filename. If the need materializes, we can add it later.
 * <p>
 * Example of how to use it is provided in the main method. An example is
 * provided below.
 * <pre>
 * testf = new LogFilter();
 * String[] incl = { &quot;hello.html&quot;, &quot;index.html&quot;, &quot;/index.jsp&quot; };
 * String[] thefiles = { &quot;/test/hello.jsp&quot;, &quot;/test/one/hello.html&quot;, &quot;hello.jsp&quot;, &quot;hello.htm&quot;, &quot;/test/open.jsp&quot;,
 *      &quot;/test/open.html&quot;, &quot;/index.jsp&quot;, &quot;/index.jhtml&quot;, &quot;newindex.jsp&quot;, &quot;oldindex.jsp&quot;, &quot;oldindex1.jsp&quot;,
 *      &quot;oldindex2.jsp&quot;, &quot;oldindex3.jsp&quot;, &quot;oldindex4.jsp&quot;, &quot;oldindex5.jsp&quot;, &quot;oldindex6.jsp&quot;, &quot;/test/index.htm&quot; };
 * testf.excludeFiles(incl);
 * System.out.println(&quot; ------------ exclude test -------------&quot;);
 * for (int idx = 0; idx &lt; thefiles.length; idx++) {
 *  boolean fl = testf.isFiltered(thefiles[idx]);
 *  String line = testf.filter(thefiles[idx]);
 *  if (line != null) {
 *     System.out.println(&quot;the file: &quot; + line);
 *  }
 * }
 * </pre>
 *
 * As a general note. Both isFiltered and filter() have to be called. Calling
 * either one will not produce the desired result. isFiltered(string) will tell
 * you if a string should be filtered. The second step is to filter the string,
 * which will return null if it is filtered and replace any part of the string
 * that should be replaced.
 */
public class LogFilter implements Filter, Serializable {

    private static final long serialVersionUID = 241L;

    private static final Logger log = LoggerFactory.getLogger(LogFilter.class);

    // protected members used by class to filter

    protected boolean CHANGEEXT = false;

    protected String OLDEXT = null;

    protected String NEWEXT = null;

    protected String[] INCFILE = null;

    protected String[] EXCFILE = null;

    protected boolean FILEFILTER = false;

    protected boolean USEFILE = true;

    protected String[] INCPTRN = null;

    protected String[] EXCPTRN = null;

    protected boolean PTRNFILTER = false;

    protected ArrayList<Pattern> EXCPATTERNS = new ArrayList<>();

    protected ArrayList<Pattern> INCPATTERNS = new ArrayList<>();

    protected String NEWFILE = null;

    public LogFilter() {
        super();
    }

    /**
     * The method will replace the file extension with the new one. You can
     * either provide the extension without the period ".", or with. The method
     * will check for period and add it if it isn't present.
     *
     * @see org.apache.jmeter.protocol.http.util.accesslog.Filter#setReplaceExtension(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void setReplaceExtension(String oldext, String newext) {
        if (oldext != null && newext != null) {
            this.CHANGEEXT = true;
            if (!oldext.contains(".") && !newext.contains(".")) {
                this.OLDEXT = "." + oldext;
                this.NEWEXT = "." + newext;
            } else {
                this.OLDEXT = oldext;
                this.NEWEXT = newext;
            }
        }
    }

    /**
     * Give the filter a list of files to include
     *
     * @param filenames
     *            list of files to include
     * @see org.apache.jmeter.protocol.http.util.accesslog.Filter#includeFiles(java.lang.String[])
     */
    @Override
    public void includeFiles(String[] filenames) {
        if (filenames != null && filenames.length > 0) {
            INCFILE = filenames;
            this.FILEFILTER = true;
        }
    }

    /**
     * Give the filter a list of files to exclude
     *
     * @param filenames
     *            list of files to exclude
     * @see org.apache.jmeter.protocol.http.util.accesslog.Filter#excludeFiles(java.lang.String[])
     */
    @Override
    public void excludeFiles(String[] filenames) {
        if (filenames != null && filenames.length > 0) {
            EXCFILE = filenames;
            this.FILEFILTER = true;
        }
    }

    /**
     * Give the filter a set of regular expressions to filter with for
     * inclusion. This method hasn't been fully implemented and test yet. The
     * implementation is not complete.
     *
     * @param regexp
     *            list of regular expressions
     * @see org.apache.jmeter.protocol.http.util.accesslog.Filter#includePattern(String[])
     */
    @Override
    public void includePattern(String[] regexp) {
        if (regexp != null && regexp.length > 0) {
            INCPTRN = regexp;
            this.PTRNFILTER = true;
            // now we create the compiled pattern and
            // add it to the arraylist
            for (String includePattern : INCPTRN) {
                this.INCPATTERNS.add(this.createPattern(includePattern));
            }
        }
    }

    /**
     * Give the filter a set of regular expressions to filter with for
     * exclusion. This method hasn't been fully implemented and test yet. The
     * implementation is not complete.
     *
     * @param regexp
     *            list of regular expressions
     *
     * @see org.apache.jmeter.protocol.http.util.accesslog.Filter#excludePattern(String[])
     */
    @Override
    public void excludePattern(String[] regexp) {
        if (regexp != null && regexp.length > 0) {
            EXCPTRN = regexp;
            this.PTRNFILTER = true;
            // now we create the compiled pattern and
            // add it to the arraylist
            for (String excludePattern : EXCPTRN) {
                this.EXCPATTERNS.add(this.createPattern(excludePattern));
            }
        }
    }

    /**
     * In the case of log filtering the important thing is whether the log entry
     * should be used. Therefore, the method will only return true if the entry
     * should be used. Since the interface defines both inclusion and exclusion,
     * that means by default inclusion filtering assumes all entries are
     * excluded unless it matches. In the case of exclusion filtering, it assumes
     * all entries are included unless it matches, which means it should be
     * excluded.
     *
     * @see org.apache.jmeter.protocol.http.util.accesslog.Filter#isFiltered(String, TestElement)
     * @param path path to be tested
     * @return <code>true</code> if entry should be excluded
     */
    @Override
    public boolean isFiltered(String path,TestElement el) {
        if (this.FILEFILTER) {
            return filterFile(path);
        }
        if (this.PTRNFILTER) {
            return filterPattern(path);
        }
        return false;
    }

    /**
     * Filter the file. The implementation performs the exclusion first before
     * the inclusion. This means if a file name is in both string arrays, the
     * exclusion will take priority. Depending on how users expect this to work,
     * we may want to change the priority so that inclusion is performed first
     * and exclusion second. Another possible alternative is to perform both
     * inclusion and exclusion. Doing so would make the most sense if the method
     * throws an exception and tells the user the same filename is in both the
     * include and exclude array.
     *
     * @param file the file to filter
     * @return boolean
     */
    protected boolean filterFile(String file) {
        // double check this logic make sure it
        // makes sense
        if (this.EXCFILE != null) {
            return excFile(file);
        } else if (this.INCFILE != null) {
            return !incFile(file);
        }
        return false;
    }

    /**
     * Method implements the logic for filtering file name inclusion. The method
     * iterates through the array and uses indexOf. Once it finds a match, it
     * won't bother with the rest of the filenames in the array.
     *
     * @param text
     *            name of the file to tested (must not be <code>null</code>)
     * @return boolean include
     */
    public boolean incFile(String text) {
        // inclusion filter assumes most of
        // the files are not wanted, therefore
        // usefile is set to false unless it
        // matches.
        this.USEFILE = false;
        for (String includeFile : this.INCFILE) {
            if (text.contains(includeFile)) {
                this.USEFILE = true;
                break;
            }
        }
        return this.USEFILE;
    }

    /**
     * Method implements the logic for filtering file name exclusion. The method
     * iterates through the array and uses indexOf. Once it finds a match, it
     * won't bother with the rest of the filenames in the array.
     *
     * @param text
     *            name of the file to be tested (must not be null)
     * @return boolean exclude
     */
    public boolean excFile(String text) {
        // exclusion filter assumes most of
        // the files are used, therefore
        // usefile is set to true, unless
        // it matches.
        this.USEFILE = true;
        boolean exc = false;
        for (String excludeFile : this.EXCFILE) {
            if (text.contains(excludeFile)) {
                exc = true;
                this.USEFILE = false;
                break;
            }
        }
        return exc;
    }

    /**
     * The current implementation assumes the user has checked the regular
     * expressions so that they don't cancel each other. The basic assumption is
     * the method will return true if the text should be filtered. If not, it
     * will return false, which means it should not be filtered.
     *
     * @param text text to be checked
     * @return boolean
     */
    protected boolean filterPattern(String text) {
        if (this.INCPTRN != null) {
            return !incPattern(text);
        } else if (this.EXCPTRN != null) {
            return excPattern(text);
        }
        return false;
    }

    /**
     * By default, the method assumes the entry is not included, unless it
     * matches. In that case, it will return true.
     *
     * @param text text to be checked
     * @return <code>true</code> if text is included
     */
    protected boolean incPattern(String text) {
        this.USEFILE = false;
        for (Pattern includePattern : this.INCPATTERNS) {
            if (JMeterUtils.getMatcher().contains(text, includePattern)) {
                this.USEFILE = true;
                break;
            }
        }
        return this.USEFILE;
    }

    /**
     * The method assumes by default the text is not excluded. If the text
     * matches the pattern, it will then return true.
     *
     * @param text text to be checked
     * @return <code>true</code> if text is excluded
     */
    protected boolean excPattern(String text) {
        this.USEFILE = true;
        boolean exc = false;
        for (Pattern excludePattern : this.EXCPATTERNS) {
            if (JMeterUtils.getMatcher().contains(text, excludePattern)) {
                exc = true;
                this.USEFILE = false;
                break;
            }
        }
        return exc;
    }

    /**
     * Method uses indexOf to replace the old extension with the new extension.
     * It might be good to use regular expression, but for now this is a simple
     * method. The method isn't designed to replace multiple instances of the
     * text, since that isn't how file extensions work. If the string contains
     * more than one instance of the old extension, only the first instance will
     * be replaced.
     *
     * @param text
     *            name of the file in which the extension should be replaced
     *            (must not be null)
     * @return <code>true</code> if the extension could be replaced,
     *         <code>false</code> otherwise
     */
    public boolean replaceExtension(String text) {
        int pt = text.indexOf(this.OLDEXT);
        if (pt > -1) {
            int extsize = this.OLDEXT.length();
            this.NEWFILE = text.substring(0, pt) + this.NEWEXT + text.substring(pt + extsize);
            return true;
        } else {
            return false;
        }
    }

    /**
     * The current implementation checks the boolean if the text should be used
     * or not. isFilter( string) has to be called first.
     *
     * @see org.apache.jmeter.protocol.http.util.accesslog.Filter#filter(java.lang.String)
     */
    @Override
    public String filter(String text) {
        if (this.CHANGEEXT) {
            if (replaceExtension(text)) {
                return this.NEWFILE;
            } else {
                return text;
            }
        } else if (this.USEFILE) {
            return text;
        } else {
            return null;
        }
    }

    /**
     * create a new pattern object from the string.
     *
     * @param pattern
     *            string representation of the perl5 compatible regex pattern
     * @return compiled Pattern, or <code>null</code> if no pattern could be
     *         compiled
     */
    public Pattern createPattern(String pattern) {
        try {
            return JMeterUtils.getPatternCache().getPattern(pattern,
                    Perl5Compiler.READ_ONLY_MASK | Perl5Compiler.SINGLELINE_MASK);
        } catch (MalformedCachePatternException exception) {
            log.error("Problem with pattern: "+pattern,exception);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {

    }
}
