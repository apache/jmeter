package org.apache.jmeter.protocol.http.parser;

import java.util.List;

/**
 * Created    June 16, 2001
 * @version    $Revision$ Last updated: $Date$
 */
public class FormElement
{
    private boolean multipleAllowed;
    private String name;
    private java.util.List possibleValues;
    private java.util.List defaultValues;

    /**
     * Constructor for the FormElement object.
     */
    public FormElement()
    {
    }

    /**
     * Sets the MultipleAllowed attribute of the FormElement object.
     *
     * @param  newMultipleAllowed  the new MultipleAllowed value
     */
    public void setMultipleAllowed(boolean newMultipleAllowed)
    {
        multipleAllowed = newMultipleAllowed;
    }

    /**
     * Sets the Name attribute of the FormElement object.
     *
     * @param  newName  the new Name value
     */
    public void setName(String newName)
    {
        name = newName;
    }

    /**
     * Sets the PossibleValues attribute of the FormElement object.
     *
     * @param  newPossibleValues  the new PossibleValues value
     */
    public void setPossibleValues(java.util.List newPossibleValues)
    {
        possibleValues = newPossibleValues;
    }

    /**
     * Sets the DefaultValues attribute of the FormElement object.
     *
     * @param  newDefaultValues  the new DefaultValues value
     */
    public void setDefaultValues(java.util.List newDefaultValues)
    {
        defaultValues = newDefaultValues;
    }

    /**
     * Gets the MultipleAllowed attribute of the FormElement object.
     *
     * @return    the MultipleAllowed value
     */
    public boolean isMultipleAllowed()
    {
        return multipleAllowed;
    }

    /**
     * Gets the Name attribute of the FormElement object.
     *
     * @return    the Name value
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets the PossibleValues attribute of the FormElement object.
     *
     * @return    the PossibleValues value
     */
    public List getPossibleValues()
    {
        return possibleValues;
    }

    /**
     * Gets the DefaultValues attribute of the FormElement object.
     *
     * @return    the DefaultValues value
     */
    public java.util.List getDefaultValues()
    {
        return defaultValues;
    }
}
