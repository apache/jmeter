package org.apache.jmeter.save.old.handlers;

import java.io.IOException;
import java.io.Writer;

import org.apache.jmeter.protocol.http.modifier.ParamMask;
import org.apache.jmeter.save.old.Saveable;
import org.apache.jmeter.save.old.xml.TagHandler;
import org.apache.jmeter.testelement.TestElement;
import org.xml.sax.Attributes;

/**
 * Saves and loads a {@link ParamMask} object to/from XML format.
 *
 * @author David La France
 * @version 1.0
 */
public class ParamMaskHandler extends TagHandler {

    /**
     * Default constructor
     */
    public ParamMaskHandler() {
    }

    /**
     * Writes a {@link ParamMask} object in XML format to the given <code>Writer</code>
     */
    public void save(Saveable parm1, Writer out) throws IOException {
        ParamMask mask = (ParamMask) parm1;
        out.write("\n");
        out.write("  <ParamMask>\n");
        out.write("    <mask name=\"");
        out.write(JMeterHandler.convertToXML(mask.getName()));
        out.write("\" prefix=\"");
        out.write(JMeterHandler.convertToXML(mask.getPrefix()));
        out.write("\" upperBound=\"");
        out.write(JMeterHandler.convertToXML(Long.toString(mask.getUpperBound())));
        out.write("\" lowerBound=\"");
        out.write(JMeterHandler.convertToXML(Long.toString(mask.getLowerBound())));
        out.write("\" increment=\"");
        out.write(JMeterHandler.convertToXML(Long.toString(mask.getIncrement())));
        out.write("\">");
        out.write("</mask>\n");
        out.write("  </ParamMask>\n");
    }

    /**
     * This method is automatically called whenever a "ParamMask" tag is reached.  The ParamMask
     * tag does not currently support attributes
     *
     * @param atts The attributes of the XML "ParamMask" tag
     */
    public void setAtts(Attributes atts) throws Exception {
        _mask = new ParamMask();
        _mask.setProperty(TestElement.GUI_CLASS,JMeterHandler.getGuiClass(atts.getValue("type")));
    }


    public String getPrimaryTagName() {
        return "ParamMask";
    }

    /**
     * This method is automatically called whenever a "mask" tag is reached.  Loads the values for
     * the ParamMask
     *
     * @param atts The attributes of the XML "mask" tag
     */
    public void mask(Attributes atts) {
        _mask.setName       (atts.getValue("name"));
        _mask.setPrefix     (atts.getValue("prefix"));
        _mask.setUpperBound (Long.parseLong(atts.getValue("upperBound")));
        _mask.setLowerBound (Long.parseLong(atts.getValue("lowerBound")));
        _mask.setIncrement  (Long.parseLong(atts.getValue("increment")));
    }

    /**
     * This method is called with any String data found between the start and end tags of a "mask"
     * tag.
     *
     * @param data The string data found
     */
    public void mask(String data) {
    }

    public Object getModel() {
        return _mask;
    }

    ParamMask _mask;
}