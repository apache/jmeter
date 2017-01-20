/* 
 * Copyright 1999-2004 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.log.format;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Stack;
import org.apache.log.ContextMap;
import org.apache.log.LogEvent;
import org.apache.log.Priority;

/**
 * This formater formats the LogEvents according to a input pattern
 * string.
 * <p>
 * The format of each pattern element can be
 * <code>%[+|-][#[.#]]{field:subformat}</code>.
 * </p>
 * <ul>
 *   <li><p>The <code>+|-</code> indicates left or right justify.
 *   </p></li>
 *   <li><p>The <code>#.#</code> indicates the minimum and maximum
 *     size of output.  You may omit the values and the field will be
 *     formatted without size restriction.<br />
 *     You may specify <code>#</code>, or <code>#.</code> to only
 *     define the minimum size.<br />
 *     You may specify <code>.#</code> to only define the maximum
 *     size.
 *   </p></li>
 *   <li><p><code>field</code> indicates which field is to be output and must be
 *     one of properties of LogEvent.  The following fields are
 *     currently supported:
 *     <table border="0" cellpadding="4" cellspacing="0">
 *       <tr>
 *         <td><b>category</b></td>
 *         <td>Category value of the logging event.</td>
 *       </tr><tr>
 *         <td><b>context</b></td>
 *         <td>Context value of the logging event.</td>
 *       </tr><tr>
 *         <td><b>message</b></td>
 *         <td>Message value of the logging event.</td>
 *       </tr><tr>
 *         <td><b>time</b></td>
 *         <td>Time value of the logging event.</td>
 *       </tr><tr>
 *         <td><b>rtime</b></td>
 *         <td>Relative time value of the logging event.</td>
 *       </tr><tr>
 *         <td><b>throwable</b></td>
 *         <td>Throwable value of the logging event.</td>
 *       </tr><tr>
 *         <td><b>priority</b></td>
 *         <td>Priority value of the logging event.</td>
 *       </tr><tr>
 *         <td><b>thread</b></td>
 *         <td>Name of the thread which logged the event.</td>
 *       </tr>
 *     </table>
 *   </p></li>
 *
 *   <li><p><code>subformat</code> indicates a particular subformat to
 *     use on the specified field, and is currently only supported by:
 *     <table border="0" cellpadding="4" cellspacing="0">
 *       <tr>
 *         <td><b>context</b></td>
 *         <td>Specifies the context map parameter name.</td>
 *       </tr><tr>
 *         <td><b>time</b></td>
 *         <td>Specifies the pattern to be pass to
 *         {@link java.text.SimpleDateFormat SimpleDateFormat} to format the time.</td>
 *       </tr>
 *     </table>
 *   </p></li>
 * </ul>
 * <p>A simple example of a typical PatternFormatter format would be:
 * <pre><code>
 *   %{time} %5.5{priority}[%-10.10{category}]: %{message}
 * </code></pre>
 * </p><p>
 * This would produce a line like:
 * <pre><code>
 *   1000928827905 DEBUG [     junit]: Sample message
 * </code></pre>
 * </p><p>
 * The format string specifies that the logger should first print the
 * time value of the log event without size restriction, then the
 * priority of the log event with a minimum and maximum size of 5,
 * then the category of the log event right justified with a minimum
 * and maximum size of 10, followed by the message of the log event
 * without any size restriction.
 * </p>
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @author Peter Donald
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:leif@tanukisoftware.com">Leif Mortenson</a>
 * @version $Id: PatternFormatter.java 30977 2004-07-30 03:57:54 -0500 (Fri, 30 Jul 2004) niclas $
 * @deprecated
 */
@Deprecated
public class PatternFormatter
    implements Formatter
{
    private static final int TYPE_TEXT = 1;
    private static final int TYPE_CATEGORY = 2;
    private static final int TYPE_CONTEXT = 3;
    private static final int TYPE_MESSAGE = 4;
    private static final int TYPE_TIME = 5;
    private static final int TYPE_RELATIVE_TIME = 6;
    private static final int TYPE_THROWABLE = 7;
    private static final int TYPE_PRIORITY = 8;
    private static final int TYPE_THREAD = 9;

    /**
     * The maximum value used for TYPEs. Subclasses can define their own TYPEs
     * starting at <code>MAX_TYPE + 1</code>.
     */
    protected static final int MAX_TYPE = TYPE_PRIORITY;

    private static final String TYPE_CATEGORY_STR = "category";
    private static final String TYPE_CONTEXT_STR = "context";
    private static final String TYPE_MESSAGE_STR = "message";
    private static final String TYPE_TIME_STR = "time";
    private static final String TYPE_RELATIVE_TIME_STR = "rtime";
    private static final String TYPE_THROWABLE_STR = "throwable";
    private static final String TYPE_PRIORITY_STR = "priority";
    private static final String TYPE_THREAD_STR = "thread";

    private static final String SPACE_16 = "                ";
    private static final String SPACE_8 = "        ";
    private static final String SPACE_4 = "    ";
    private static final String SPACE_2 = "  ";
    private static final String SPACE_1 = " ";

    private static final String EOL = System.getProperty( "line.separator", "\n" );

    protected static class PatternRun
    {
        public String m_data;
        public boolean m_rightJustify;
        public int m_minSize;
        public int m_maxSize;
        public int m_type;
        public String m_format;
    }

    private PatternRun m_formatSpecification[];

    private SimpleDateFormat m_simpleDateFormat;
    private final Date m_date = new Date();

    /**
     * Creation of a new patter formatter baseed on a supplied pattern.
     * @param pattern the patter
     */
    public PatternFormatter( final String pattern )
    {
        parse( pattern );
    }

    /**
     * Extract and build a pattern from input string.
     *
     * @param stack the stack on which to place patterns
     * @param pattern the input string
     * @param index the start of pattern run
     * @return the number of characters in pattern run
     */
    private int addPatternRun( final Stack stack,
                               final char pattern[],
                               int index )
    {
        final PatternRun run = new PatternRun();
        final int start = index++;

        //first check for a +|- sign
        if( '+' == pattern[ index ] )
        {
            index++;
        }
        else if( '-' == pattern[ index ] )
        {
            run.m_rightJustify = true;
            index++;
        }

        if( Character.isDigit( pattern[ index ] ) )
        {
            int total = 0;
            while( Character.isDigit( pattern[ index ] ) )
            {
                total = total * 10 + ( pattern[ index ] - '0' );
                index++;
            }
            run.m_minSize = total;
        }

        //check for . sign indicating a maximum is to follow
        if( index < pattern.length && '.' == pattern[ index ] )
        {
            index++;

            if( Character.isDigit( pattern[ index ] ) )
            {
                int total = 0;
                while( Character.isDigit( pattern[ index ] ) )
                {
                    total = total * 10 + ( pattern[ index ] - '0' );
                    index++;
                }
                run.m_maxSize = total;
            }
        }

        if( index >= pattern.length || '{' != pattern[ index ] )
        {
            throw new IllegalArgumentException(
                "Badly formed pattern at character " + index );
        }

        int typeStart = index;

        while( index < pattern.length
            && pattern[ index ] != ':' && pattern[ index ] != '}' )
        {
            index++;
        }

        int typeEnd = index - 1;

        final String type =
            new String( pattern, typeStart + 1, typeEnd - typeStart );

        run.m_type = getTypeIdFor( type );

        if( index < pattern.length && pattern[ index ] == ':' )
        {
            index++;
            while( index < pattern.length && pattern[ index ] != '}' ) index++;

            final int length = index - typeEnd - 2;

            if( 0 != length )
            {
                run.m_format = new String( pattern, typeEnd + 2, length );
            }
        }

        if( index >= pattern.length || '}' != pattern[ index ] )
        {
            throw new IllegalArgumentException(
                "Unterminated type in pattern at character "
                + index );
        }

        index++;

        stack.push( run );

        return index - start;
    }

    /**
     * Extract and build a text run  from input string.
     * It does special handling of '\n' and '\t' replaceing
     * them with newline and tab.
     *
     * @param stack the stack on which to place runs
     * @param pattern the input string
     * @param index the start of the text run
     * @return the number of characters in run
     */
    private int addTextRun( final Stack stack,
                            final char pattern[],
                            int index )
    {
        final PatternRun run = new PatternRun();
        final int start = index;
        boolean escapeMode = false;

        if( '%' == pattern[ index ] )
        {
            index++;
        }

        final StringBuffer sb = new StringBuffer();

        while( index < pattern.length && pattern[ index ] != '%' )
        {
            if( escapeMode )
            {
                if( 'n' == pattern[ index ] )
                {
                    sb.append( EOL );
                }
                else if( 't' == pattern[ index ] )
                {
                    sb.append( '\t' );
                }
                else
                {
                    sb.append( pattern[ index ] );
                }
                escapeMode = false;
            }
            else if( '\\' == pattern[ index ] )
            {
                escapeMode = true;
            }
            else
            {
                sb.append( pattern[ index ] );
            }
            index++;
        }

        run.m_data = sb.toString();
        run.m_type = TYPE_TEXT;

        stack.push( run );

        return index - start;
    }

    /**
     * Utility to append a string to buffer given certain constraints.
     *
     * @param sb the StringBuffer
     * @param minSize the minimum size of output (0 to ignore)
     * @param maxSize the maximum size of output (0 to ignore)
     * @param rightJustify true if the string is to be right justified in it's box.
     * @param output the input string
     */
    private void append( final StringBuffer sb,
                         final int minSize,
                         final int maxSize,
                         final boolean rightJustify,
                         final String output )
    {
        final int size = output.length();

        if( size < minSize )
        {
            //assert( minSize > 0 );
            if( rightJustify )
            {
                appendWhiteSpace( sb, minSize - size );
                sb.append( output );
            }
            else
            {
                sb.append( output );
                appendWhiteSpace( sb, minSize - size );
            }
        }
        else if( maxSize > 0 && maxSize < size )
        {
            if( rightJustify )
            {
                sb.append( output.substring( size - maxSize ) );
            }
            else
            {
                sb.append( output.substring( 0, maxSize ) );
            }
        }
        else
        {
            sb.append( output );
        }
    }

    /**
     * Append a certain number of whitespace characters to a StringBuffer.
     *
     * @param sb the StringBuffer
     * @param length the number of spaces to append
     */
    private void appendWhiteSpace( final StringBuffer sb, int length )
    {
        while( length >= 16 )
        {
            sb.append( SPACE_16 );
            length -= 16;
        }

        if( length >= 8 )
        {
            sb.append( SPACE_8 );
            length -= 8;
        }

        if( length >= 4 )
        {
            sb.append( SPACE_4 );
            length -= 4;
        }

        if( length >= 2 )
        {
            sb.append( SPACE_2 );
            length -= 2;
        }

        if( length >= 1 )
        {
            sb.append( SPACE_1 );
            length -= 1;
        }
    }

    /**
     * Format the event according to the pattern.
     *
     * @param event the event
     * @return the formatted output
     */
    public String format( final LogEvent event )
    {
        final StringBuffer sb = new StringBuffer();

        for( int i = 0; i < m_formatSpecification.length; i++ )
        {
            final PatternRun run = m_formatSpecification[ i ];

            //treat text differently as it doesn't need min/max padding
            if( run.m_type == TYPE_TEXT )
            {
                sb.append( run.m_data );
            }
            else
            {
                final String data = formatPatternRun( event, run );
                if( null != data )
                {
                    append( sb, run.m_minSize, run.m_maxSize, run.m_rightJustify, data );
                }
            }
        }

        return sb.toString();
    }

    /**
     * Formats a single pattern run (can be extended in subclasses).
     *
     * @param  run the pattern run to format.
     * @return the formatted result.
     */
    protected String formatPatternRun( final LogEvent event, final PatternRun run )
    {
        switch( run.m_type )
        {
            case TYPE_RELATIVE_TIME:
                return getRTime( event.getRelativeTime(), run.m_format );
            case TYPE_TIME:
                return getTime( event.getTime(), run.m_format );
            case TYPE_THROWABLE:
                return getStackTrace( event.getThrowable(), run.m_format );
            case TYPE_MESSAGE:
                return getMessage( event.getMessage(), run.m_format );
            case TYPE_CATEGORY:
                return getCategory( event.getCategory(), run.m_format );
            case TYPE_PRIORITY:
                return getPriority( event.getPriority(), run.m_format );

            case TYPE_CONTEXT:
                return getContextMap( event.getContextMap(), run.m_format );

            case TYPE_THREAD:
                return getThread( run.m_format );

            default:
                throw new IllegalStateException( "Unknown Pattern specification." + run.m_type );
        }
    }

    /**
     * Utility method to format category.
     *
     * @param category the category string
     * @param format ancilliary format parameter - allowed to be null
     * @return the formatted string
     */
    protected String getCategory( final String category, final String format )
    {
        return category;
    }

    /**
     * Get formatted priority string.
     */
    protected String getPriority( final Priority priority, final String format )
    {
        return priority.getName();
    }

    /**
     * Get formatted thread string.
     */
    protected String getThread( final String format )
    {
        return Thread.currentThread().getName();
    }

    /**
     * Utility method to format context map.
     *
     * @param map the context map
     * @param format ancilliary format parameter - allowed to be null
     * @return the formatted string
     */
    protected String getContextMap( final ContextMap map, final String format )
    {
        if( null == map ) return "";
        return map.get( format, "" ).toString();
    }

    /**
     * Utility method to format message.
     *
     * @param message the message string
     * @param format ancilliary format parameter - allowed to be null
     * @return the formatted string
     */
    protected String getMessage( final String message, final String format )
    {
        return message;
    }

    /**
     * Utility method to format stack trace.
     *
     * @param throwable the throwable instance
     * @param format ancilliary format parameter - allowed to be null
     * @return the formatted string
     */
    protected String getStackTrace( final Throwable throwable, final String format )
    {
        if( null == throwable ) return "";
        final StringWriter sw = new StringWriter();
        throwable.printStackTrace( new java.io.PrintWriter( sw ) );
        return sw.toString();
    }

    /**
     * Utility method to format relative time.
     *
     * @param time the time
     * @param format ancilliary format parameter - allowed to be null
     * @return the formatted string
     */
    protected String getRTime( final long time, final String format )
    {
        return getTime( time, format );
    }

    /**
     * Utility method to format time.
     *
     * @param time the time
     * @param format ancilliary format parameter - allowed to be null
     * @return the formatted string
     */
    protected String getTime( final long time, final String format )
    {
        if( null == format )
        {
            return Long.toString( time );
        }
        else
        {
            synchronized( m_date )
            {
                if( null == m_simpleDateFormat )
                {
                    m_simpleDateFormat = new SimpleDateFormat( format );
                }
                m_date.setTime( time );
                return m_simpleDateFormat.format( m_date );
            }
        }
    }

    /**
     * Retrieve the type-id for a particular string.
     *
     * @param type the string
     * @return the type-id
     */
    protected int getTypeIdFor( final String type )
    {
        if( type.equalsIgnoreCase( TYPE_CATEGORY_STR ) )
        {
            return TYPE_CATEGORY;
        }
        else if( type.equalsIgnoreCase( TYPE_CONTEXT_STR ) )
        {
            return TYPE_CONTEXT;
        }
        else if( type.equalsIgnoreCase( TYPE_MESSAGE_STR ) )
        {
            return TYPE_MESSAGE;
        }
        else if( type.equalsIgnoreCase( TYPE_PRIORITY_STR ) )
        {
            return TYPE_PRIORITY;
        }
        else if( type.equalsIgnoreCase( TYPE_TIME_STR ) )
        {
            return TYPE_TIME;
        }
        else if( type.equalsIgnoreCase( TYPE_RELATIVE_TIME_STR ) )
        {
            return TYPE_RELATIVE_TIME;
        }
        else if( type.equalsIgnoreCase( TYPE_THREAD_STR ) )
        {
            return TYPE_THREAD;
        }
        else if( type.equalsIgnoreCase( TYPE_THROWABLE_STR ) )
        {
            return TYPE_THROWABLE;
        }
        else
        {
            throw new IllegalArgumentException( "Unknown Type in pattern - " +
                                                type );
        }
    }

    /**
     * Parse the input pattern and build internal data structures.
     *
     * @param patternString the pattern
     */
    protected final void parse( final String patternString )
    {
        final Stack stack = new Stack();
        final int size = patternString.length();
        final char pattern[] = new char[ size ];
        int index = 0;

        patternString.getChars( 0, size, pattern, 0 );

        while( index < size )
        {
            if( pattern[ index ] == '%'
                && !( index != size - 1 && pattern[ index + 1 ] == '%' ) )
            {
                index += addPatternRun( stack, pattern, index );
            }
            else
            {
                index += addTextRun( stack, pattern, index );
            }
        }

        final int elementCount = stack.size();

        m_formatSpecification = new PatternRun[ elementCount ];

        for( int i = 0; i < elementCount; i++ )
        {
            m_formatSpecification[ i ] = (PatternRun)stack.elementAt( i );
        }
    }

}
