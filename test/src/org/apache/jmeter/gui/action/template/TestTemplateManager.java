package org.apache.jmeter.gui.action.template;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.jmeter.util.JMeterUtils;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * Test the templates.xml parsing by the TemplateManager Class
 */
public class TestTemplateManager {
    
    final String xmlTemplatesDoc = JMeterUtils.getResourceFileAsText("org/apache/jmeter/gui/action/template/testTemplates.xml");
    
    @Test
    public void testParseTemplateFile() {
//        récupérer un xml de test et vérifier que le resultat correspond
//        récupérer un xml de test qui utilise l'ancien dtd pour voir qu'il parse bien
//        vérifier qu'il renvoie IllegalArgumentException si le fichier existe pas
//        vérifier qu'un xml invalide renvoie une IOException ou SAXException ou fait un truc
        
        String xmlTemplatePath = this.getClass().getResource("testTemplates.xml").getFile();
        File templateFile = new File(xmlTemplatePath);
        TemplateManager templateManager = TemplateManager.getInstance();
        try {
            Map<String, Template> templates = templateManager.parseTemplateFile(templateFile);
            Template tmp = templates.get("MyTestTemplates3");
            System.out.println(tmp.equals(tmp));
            Template tmp2 = tmp;
            tmp2.getParameters().clear();
            System.out.println(tmp.equals(tmp2));
        } catch (SAXException e) {
            // NOOP
        } catch (ParserConfigurationException e) {
            // NOOP
        } catch (IOException e) {
            // NOOP
        }
    }
    
}
