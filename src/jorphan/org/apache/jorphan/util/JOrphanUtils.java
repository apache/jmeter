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

package org.apache.jorphan.util;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * This class contains frequently-used static utility methods.
 *
 */

// @see TestJorphanUtils for unit tests

public final class JOrphanUtils {

    private static final int DEFAULT_CHUNK_SIZE = 4096;
    /**
     * Private constructor to prevent instantiation.
     */
    private JOrphanUtils() {
    }

    /**
     * This is _almost_ equivalent to the String.split method in JDK 1.4. It is
     * here to enable us to support earlier JDKs.
     *
     * Note that unlike JDK1.4 split(), it optionally ignores leading split Characters,
     * and the splitChar parameter is not a Regular expression
     *
     * <P>
     * This piece of code used to be part of JMeterUtils, but was moved here
     * because some JOrphan classes use it too.
     *
     * @param splittee
     *            String to be split
     * @param splitChar
     *            Character(s) to split the string on, these are treated as a single unit
     * @param truncate
     *            Should adjacent and leading/trailing splitChars be removed?
     *
     * @return Array of all the tokens; empty if the input string is null or the splitChar is null
     *
     * @see #split(String, String, String)
     *
     */
    public static String[] split(String splittee, String splitChar,boolean truncate) {
        if (splittee == null || splitChar == null) {
            return new String[0];
        }
        final String EMPTY_ELEMENT = "";
        int spot;
        final int splitLength = splitChar.length();
        final String adjacentSplit = splitChar + splitChar;
        final int adjacentSplitLength = adjacentSplit.length();
        if(truncate) {
            while ((spot = splittee.indexOf(adjacentSplit)) != -1) {
                splittee = splittee.substring(0, spot + splitLength)
                        + splittee.substring(spot + adjacentSplitLength, splittee.length());
            }
            if(splittee.startsWith(splitChar)) {
                splittee = splittee.substring(splitLength);
            }
            if(splittee.endsWith(splitChar)) { // Remove trailing splitter
                splittee = splittee.substring(0,splittee.length()-splitLength);
            }
        }
        List<String> returns = new ArrayList<>();
        final int length = splittee.length(); // This is the new length
        int start = 0;
        spot = 0;
        while (start < length && (spot = splittee.indexOf(splitChar, start)) > -1) {
            if (spot > 0) {
                returns.add(splittee.substring(start, spot));
            }
            else
            {
                returns.add(EMPTY_ELEMENT);
            }
            start = spot + splitLength;
        }
        if (start < length) {
            returns.add(splittee.substring(start));
        } else if (spot == length - splitLength){// Found splitChar at end of line
            returns.add(EMPTY_ELEMENT);
        }
        return returns.toArray(new String[returns.size()]);
    }

    public static String[] split(String splittee,String splitChar)
    {
        return split(splittee,splitChar,true);
    }

    /**
     * Takes a String and a tokenizer character string, and returns a new array of
     * strings of the string split by the tokenizer character(s).
     *
     * Trailing delimiters are significant (unless the default = null)
     *
     * @param splittee
     *            String to be split.
     * @param delims
     *            Delimiter character(s) to split the string on
     * @param def
     *            Default value to place between two split chars that have
     *            nothing between them. If null, then ignore omitted elements.
     *
     * @return Array of all the tokens.
     *
     * @throws NullPointerException if splittee or delims are null
     *
     * @see #split(String, String, boolean)
     * @see #split(String, String)
     *
     * This is a rewritten version of JMeterUtils.split()
     */
    public static String[] split(String splittee, String delims, String def) {
        StringTokenizer tokens = new StringTokenizer(splittee,delims,def!=null);
        boolean lastWasDelim=false;
        List<String> strList = new ArrayList<>();
        while (tokens.hasMoreTokens()) {
            String tok=tokens.nextToken();
            if (   tok.length()==1 // we have a single character; could be a token
                && delims.contains(tok)) // it is a token
            {
                if (lastWasDelim) {// we saw a delimiter last time
                    strList.add(def);// so add the default
                }
                lastWasDelim=true;
            } else {
                lastWasDelim=false;
                strList.add(tok);
            }
        }
        if (lastWasDelim) {
            strList.add(def);
        }
        return strList.toArray(new String[strList.size()]);
    }


    private static final char[] SPACES_CHARS = "                                 ".toCharArray();
    private static final int SPACES_LEN = SPACES_CHARS.length;

    /**
     * Right aligns some text in a StringBuilder N.B. modifies the input buffer
     *
     * @param in
     *            StringBuilder containing some text
     * @param len
     *            output length desired
     * @return input StringBuilder, with leading spaces
     */
    public static StringBuilder rightAlign(StringBuilder in, int len) {
        int pfx = len - in.length();
        if (pfx <= 0) {
            return in;
        }
        if (pfx > SPACES_LEN) {
            pfx = SPACES_LEN;
        }
        in.insert(0, SPACES_CHARS, 0, pfx);
        return in;
    }

    /**
     * Left aligns some text in a StringBuilder N.B. modifies the input buffer
     *
     * @param in
     *            StringBuilder containing some text
     * @param len
     *            output length desired
     * @return input StringBuilder, with trailing spaces
     */
    public static StringBuilder leftAlign(StringBuilder in, int len) {
        int sfx = len - in.length();
        if (sfx <= 0) {
            return in;
        }
        if (sfx > SPACES_LEN) {
            sfx = SPACES_LEN;
        }
        in.append(SPACES_CHARS, 0, sfx);
        return in;
    }

    /**
     * Convert a boolean to its upper case string representation.
     * Equivalent to Boolean.valueOf(boolean).toString().toUpperCase().
     *
     * @param value
     *            boolean to convert
     * @return "TRUE" or "FALSE"
     */
    public static String booleanToSTRING(boolean value) {
        return value ? "TRUE" : "FALSE";
    }

    /**
     * Simple-minded String.replace() for JDK1.3 Should probably be recoded...
     *
     * @param source
     *            input string
     * @param search
     *            string to look for (no regular expressions)
     * @param replace
     *            string to replace the search string
     * @return the output string
     */
    public static String replaceFirst(String source, String search, String replace) {
        int start = source.indexOf(search);
        int len = search.length();
        if (start == -1) {
            return source;
        }
        if (start == 0) {
            return replace + source.substring(len);
        }
        return source.substring(0, start) + replace + source.substring(start + len);
    }

    /**
     * Version of String.replaceAll() for JDK1.3
     * See below for another version which replaces strings rather than chars
     * and provides a fast path which does not allocate memory
     * @param source
     *            input string
     * @param search
     *            char to look for (no regular expressions)
     * @param replace
     *            string to replace the search string
     * @return the output string
     */
    public static String replaceAllChars(String source, char search, String replace) {
        int indexOf = source.indexOf(search);
        if(indexOf == -1) {
            return source;
        }
        
        int offset = 0;
        char[] chars = source.toCharArray();
        StringBuilder sb = new StringBuilder(source.length()+20);
        while(indexOf != -1) {
            sb.append(chars, offset, indexOf-offset);
            sb.append(replace);
            offset = indexOf +1;
            indexOf = source.indexOf(search, offset);
        }
        sb.append(chars, offset, chars.length- offset);
        
        return sb.toString();
    }

    /**
     * Replace all patterns in a String
     *
     * @see String#replaceAll(String regex,String replacement) - JDK1.4 only
     *
     * @param input - string to be transformed
     * @param pattern - pattern to replace
     * @param sub - replacement
     * @return the updated string
     */
    public static String substitute(final String input, final String pattern, final String sub) {
        StringBuilder ret = new StringBuilder(input.length());
        int start = 0;
        int index = -1;
        final int length = pattern.length();
        while ((index = input.indexOf(pattern, start)) >= start) {
            ret.append(input.substring(start, index));
            ret.append(sub);
            start = index + length;
        }
        ret.append(input.substring(start));
        return ret.toString();
    }

    /**
     * Trim a string by the tokens provided.
     *
     * @param input string to trim
     * @param delims list of delimiters
     * @return input trimmed at the first delimiter
     */
    public static String trim(final String input, final String delims){
        StringTokenizer tokens = new StringTokenizer(input,delims);
        return tokens.hasMoreTokens() ? tokens.nextToken() : "";
    }

    /**
     * Returns a slice of a byte array.
     *
     * TODO - add bounds checking?
     *
     * @param array -
     *            input array
     * @param begin -
     *            start of slice
     * @param end -
     *            end of slice
     * @return slice from the input array
     */
    public static byte[] getByteArraySlice(byte[] array, int begin, int end) {
        byte[] slice = new byte[end - begin + 1];
        System.arraycopy(array, begin, slice, 0, slice.length);
        return slice;
    }

    // N.B. Commons IO IOUtils has equivalent methods; these were added before IO was included
    // TODO - perhaps deprecate these in favour of Commons IO?
    /**
     * Close a Closeable with no error thrown
     * @param cl - Closeable (may be null)
     */
    public static void closeQuietly(Closeable cl){
        try {
            if (cl != null) {
                cl.close();
            }
        } catch (IOException ignored) {
            // NOOP
        }
    }

    /**
     * close a Socket with no error thrown
     * @param sock - Socket (may be null)
     */
    public static void closeQuietly(Socket sock){
        try {
            if (sock!= null) {
                sock.close();
            }
        } catch (IOException ignored) {
            // NOOP
        }
    }

    /**
     * close a Socket with no error thrown
     * @param sock - ServerSocket (may be null)
     */
    public static void closeQuietly(ServerSocket sock){
        try {
            if (sock!= null) {
                sock.close();
            }
        } catch (IOException ignored) {
            // NOOP
        }
    }

    /**
     * Check if a byte array starts with the given byte array.
     *
     * @see String#startsWith(String, int)
     *
     * @param target array to scan
     * @param search array to search for
     * @param offset starting offset (&gt;=0)
     * @return true if the search array matches the target at the current offset
     */
    public static boolean startsWith(byte [] target, byte [] search, int offset){
        final int targetLength = target.length;
        final int searchLength = search.length;
        if (offset < 0 || searchLength > targetLength+offset){
            return false;
        }
        for(int i=0;i < searchLength; i++){
            if (target[i+offset] != search[i]){
                return false;
            }
        }
        return true;
    }

    private static final byte[] XML_PFX = {'<','?','x','m','l'};// "<?xml "

    /**
     * Detects if some content starts with the standard XML prefix.
     *
     * @param target the content to check
     * @return true if the document starts with the standard XML prefix.
     */
    public static boolean isXML(byte [] target){
        return startsWith(target, XML_PFX,0);
    }

    /**
     * Convert binary byte array to hex string.
     *
     * @param ba input binary byte array
     * @return hex representation of binary input
     */
    public static String baToHexString(byte[] ba) {
        StringBuilder sb = new StringBuilder(ba.length*2);
        for (byte b : ba) {
            int j = b & 0xff;
            if (j < 16) {
                sb.append('0'); // $NON-NLS-1$ add zero padding
            }
            sb.append(Integer.toHexString(j));
        }
        return sb.toString();
    }

    /**
     * Convert binary byte array to hex string.
     *
     * @param ba input binary byte array
     * @param separator the separator to be added between pairs of hex digits
     * @return hex representation of binary input
     */
    public static String baToHexString(byte[] ba, char separator) {
        StringBuilder sb = new StringBuilder(ba.length*2);
        for (int i = 0; i < ba.length; i++) {
            if (i > 0 && separator != 0) {
                sb.append(separator);
            }
            int j = ba[i] & 0xff;
            if (j < 16) {
                sb.append('0'); // $NON-NLS-1$ add zero padding
            }
            sb.append(Integer.toHexString(j));
        }
        return sb.toString();
    }

    /**
     * Convert binary byte array to hex string.
     *
     * @param ba input binary byte array
     * @return hex representation of binary input
     */
    public static byte[] baToHexBytes(byte[] ba) {
        byte[] hb = new byte[ba.length*2];
        for (int i = 0; i < ba.length; i++) {
            byte upper = (byte) ((ba[i] & 0xf0) >> 4);
            byte lower = (byte) (ba[i] & 0x0f);
            hb[2*i]=toHexChar(upper);
            hb[2*i+1]=toHexChar(lower);
        }
        return hb;
    }

    private static byte toHexChar(byte in){
        if (in < 10) {
            return (byte) (in+'0');
        }
        return (byte) ((in-10)+'a');
    }

    /**
     * Read as much as possible into buffer.
     *
     * @param is the stream to read from
     * @param buffer output buffer
     * @param offset offset into buffer
     * @param length number of bytes to read
     *
     * @return the number of bytes actually read
     * @throws IOException if some I/O errors occur
     */
    public static int read(InputStream is, byte[] buffer, int offset, int length) throws IOException {
        int remaining = length;
        while ( remaining > 0 ) {
            int location = length - remaining;
            int count = is.read( buffer, location, remaining );
            if ( -1 == count ) { // EOF
                break;
            }
            remaining -= count;
        }
        return length - remaining;
    }

    /**
     * Display currently running threads on system.out
     * This may be expensive to run.
     * Mainly designed for use at the end of a non-GUI test to check for threads that might prevent the JVM from exiting.
     *
     * @param includeDaemons whether to include daemon threads or not.
     */
    public static void displayThreads(boolean includeDaemons) {
        Map<Thread, StackTraceElement[]> m = Thread.getAllStackTraces();
        String lineSeparator = System.getProperty("line.separator");
        StringBuilder builder = new StringBuilder();
        for(Map.Entry<Thread, StackTraceElement[]> e : m.entrySet()) {
            boolean daemon = e.getKey().isDaemon();
            if (includeDaemons || !daemon){
                builder.setLength(0);
                StackTraceElement[] ste = e.getValue();
                for (StackTraceElement stackTraceElement : ste) {
                    int lineNumber = stackTraceElement.getLineNumber();
                    builder.append(stackTraceElement.getClassName()+"#"+stackTraceElement.getMethodName()+
                            (lineNumber >=0 ? " at line:"+ stackTraceElement.getLineNumber() : "")+lineSeparator);
                }
                System.out.println(e.getKey().toString()+(daemon ? " (daemon)" : "")+", stackTrace:"+ builder.toString());
            }
        }
    }

    /**
     * Returns null if input is empty, null or contains spaces
     * @param input String
     * @return String
     */
    public static String nullifyIfEmptyTrimmed(final String input) {
        if (input == null) {
            return null;
        }
        String trimmed = input.trim();
        if (trimmed.length() == 0) {
            return null;
        }
        return trimmed;
    }

    /**
     * Check that value is empty (""), null or whitespace only.
     * @param value Value
     * @return true if the String is not empty (""), not null and not whitespace only.
     */
    public static boolean isBlank(final String value) {
        return StringUtils.isBlank(value);
    }

    /**
     * Write data to an output stream in chunks with a maximum size of 4K.
     * This is to avoid OutOfMemory issues if the data buffer is very large
     * and the JVM needs to copy the buffer for use by native code.
     *
     * @param data the buffer to be written
     * @param output the output stream to use
     * @throws IOException if there is a problem writing the data
     */
    // Bugzilla 54990
    public static void write(byte[] data, OutputStream output) throws IOException {
        int bytes = data.length;
        int offset = 0;
        while(bytes > 0) {
            int chunk = Math.min(bytes, DEFAULT_CHUNK_SIZE);
            output.write(data, offset, chunk);
            bytes -= chunk;
            offset += chunk;
        }
    }

    /**
     * @param elapsedSec long elapsed time in seconds
     * @return String formatted with format HH:mm:ss
     */
    @SuppressWarnings("boxing")
    public static String formatDuration(long elapsedSec) {
        return String.format("%02d:%02d:%02d",
                elapsedSec / 3600, (elapsedSec % 3600) / 60, elapsedSec % 60);
    }

    /**
     * Throw {@link IllegalArgumentException} if folder cannot be written to either:
     * <ul>
     *  <li>Because it exists but is not a folder</li>
     *  <li>Because it exists but is not empty</li>
     *  <li>Because it does not exist but cannot be created</li>
     * </ul>
     * @param folder {@link File} 
     * 
     * @throws IllegalArgumentException when folder can't be written to
     */
    public static void canSafelyWriteToFolder(File folder) {
        canSafelyWriteToFolder(folder, false);
    }
    /**
     * Check whether we can write to a folder.
     *
     * @param folder which should be checked for writability and emptyness
     * @param deleteFolderIfExists flag whether the folder should be emptied or a file with the same name deleted
     *
     * @throws IllegalArgumentException when folder can't be written to. That could have the following reasons:
     * <ul>
     *  <li>it exists but is not a folder</li>
     *  <li>it exists but is not empty</li>
     *  <li>it does not exist but cannot be created</li>
     * </ul>
     */
    public static void canSafelyWriteToFolder(File folder, boolean deleteFolderIfExists) {
        if(folder.exists()) {
            if (folder.isFile()) {
                if(deleteFolderIfExists) {
                    if(!folder.delete()) {
                        throw new IllegalArgumentException("Cannot write to '"
                                +folder.getAbsolutePath()+"' as it is an existing file and delete failed");
                    }
                } else {
                    throw new IllegalArgumentException("Cannot write to '"
                        +folder.getAbsolutePath()+"' as it is an existing file");
                }
            } else {
                File[] listedFiles = folder.listFiles();
                if(listedFiles != null && listedFiles.length > 0) {
                    if(deleteFolderIfExists) {
                        try {
                            FileUtils.deleteDirectory(folder);
                            folder.mkdir();
                        } catch(IOException ex) {
                            throw new IllegalArgumentException("Cannot write to '"
                                    +folder.getAbsolutePath()+"' as folder is not empty and cleanup failed with error:"+ex.getMessage(), ex);
                        }
                    } else {
                        throw new IllegalArgumentException("Cannot write to '"
                            +folder.getAbsolutePath()+"' as folder is not empty");
                    }
                }
            }
        } else {
            // check we can create it
            if(!folder.getAbsoluteFile().getParentFile().canWrite()) {
                throw new IllegalArgumentException("Cannot write to '"
                        +folder.getAbsolutePath()+"' as folder does not exist and parent folder is not writable");
            }
        }
    }

    /**
     * Replace in source all matches of regex by replacement taking 
     * into account case if caseSensitive is true
     * @param source Source text
     * @param regex Regular expression
     * @param replacement Replacement text to which function applies a quoting
     * @param caseSensitive is case taken into account
     * @return array of Object where first row is the replaced text, second row is the number of replacement that occurred
     */
    public static Object[] replaceAllWithRegex(
            String source, String regex, String replacement, boolean caseSensitive) {
        java.util.regex.Pattern pattern = caseSensitive ? 
                java.util.regex.Pattern.compile(regex) :  
                java.util.regex.Pattern.compile(regex, java.util.regex.Pattern.CASE_INSENSITIVE);
        final String replacementQuoted = Matcher.quoteReplacement(replacement);
        Matcher matcher = pattern.matcher(source);
        int totalReplaced = 0;
        StringBuffer result = new StringBuffer(); // NOSONAR Matcher#appendReplacement needs a StringBuffer
        while(matcher.find()) {
            matcher.appendReplacement(result, replacementQuoted);
            totalReplaced++;
        }
        matcher.appendTail(result);

        return new Object[]{
                result.toString(),
                totalReplaced
        };
    }
}
