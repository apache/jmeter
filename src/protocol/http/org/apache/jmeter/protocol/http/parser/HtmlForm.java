package org.apache.jmeter.protocol.http.parser;

import java.util.Collection;
import java.util.Collections;

/**
 * @created    June 29, 2001
 * @version    $Revision$
 */
class HtmlForm
{
    /**
     * Constructor for the HtmlForm object.
     */
    public HtmlForm()
    {
    }

    /**
     * Give the action string and a list of FormElement objects to create
     * the new HtmlForm, to be used by the HtmlParser.
     */
    public HtmlForm(String action, Collection formElements)
    {
    }

    public static Collection extractFormElements(String formString)
    {
        return Collections.EMPTY_LIST;
    }
}
