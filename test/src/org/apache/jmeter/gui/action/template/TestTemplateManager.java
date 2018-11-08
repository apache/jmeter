package org.apache.jmeter.gui.action.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * Test the templates.xml parsing by the TemplateManager Class
 */
public class TestTemplateManager {

    /*
     * Test a xsd templateFile.
     */
    @Test
    public void testXSDtemplateFile() {
        String xmlTemplatePath = this.getClass().getResource("testTemplates.xml").getFile();
        testParseTemplateFile(xmlTemplatePath, true);
    }

    /*
     * Test a possible older version of template file.
     */
    @Test
    public void testDTDtemplateFile() {
        String xmlTemplatePath = this.getClass().getResource("testTemplatesOld.xml").getFile();
        testParseTemplateFile(xmlTemplatePath, false);
    }
    
    /*
     * Check that a wrong xml file throw a IOException 
     */
    @Test
    public void testNonExistingTemplateFile() {
        String xmlTemplatePath = "IDoNotExist.xml";
        File templateFile = new File(xmlTemplatePath);
        TemplateManager templateManager = TemplateManager.getInstance();
        try {
            templateManager.parseTemplateFile(templateFile);
        }catch(IOException e) {
            String exceptionClass = e.getClass().toString();
            assert(exceptionClass.contains("java.io.FileNotFoundException"));
        } catch (SAXException e) {
            fail("Should never throw SAXException here : "+e.getCause());
        } catch (ParserConfigurationException e) {
            fail("Should never throw ParserConfigurationException here : "+e.getCause());
        }
    }

    private void testParseTemplateFile(String pathFile, boolean isXsdTemplate) {
        File templateFile = new File(pathFile);
        TemplateManager templateManager = TemplateManager.getInstance();
        
        try {
            Map<String, Template> templateList1 = templateManager.parseTemplateFile(templateFile);
            Map<String, Template> templateList2 = templateManager.parseTemplateFile(templateFile);

            assertEquals(templateList1.get("testTemplates3"), templateList2.get("testTemplates3"));
            assertNotEquals(templateList1.get("testTemplates3"), templateList1.get("testTemplates2"));

            // only applicable to xsd templates files as the dtd version did not handle the parameters option
            if(isXsdTemplate) {
                Template testTemplate = templateList1.get("testTemplates3");
                assertEquals("testTemplates3", testTemplate.getName());
                assertEquals("/bin/templates/testTemplate.jmx.fmkr", testTemplate.getFileName());
                assertEquals("\n        Last template\n        ", testTemplate.getDescription());
                Map<String, String> testTemplateParameters = testTemplate.getParameters();
                assertEquals("n 1", testTemplateParameters.get("testKey1"));
                assertEquals("n 2", testTemplateParameters.get("testKey2"));
                assertEquals("n 3", testTemplateParameters.get("testKey3"));
            }

        } catch (SAXException e) {
            fail("Should never throw SAXException here : "+e.getCause());
        } catch (ParserConfigurationException e) {
            fail("Should never throw ParserConfigurationException here : "+e.getCause());
        } catch (IOException e) {
            fail("Should never throw IOException here : "+e.getCause());
        }
    }

}
