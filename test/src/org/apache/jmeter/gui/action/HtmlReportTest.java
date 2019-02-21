package org.apache.jmeter.gui.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.HtmlReportAction;
import org.apache.jmeter.gui.HtmlReportPanel;
import org.apache.jmeter.gui.MainFrame;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HtmlReportTest extends JMeterTestCase {

    @BeforeClass
    public static void setupTest() throws IOException {
        File testDirectory = new File(JMeterUtils.getJMeterBinDir() + "/testReport");
        if (testDirectory.exists()) {
            FileUtils.forceDelete(testDirectory);
        }
    }

    /**
     * Check if HtmlReportAction.run() return the good error messages if there
     * is no file or directory at each path
     */
    @Test
    public void checkNoFileOrDirectoryErrors() {
        HtmlReportGenerator htmlReportGenerator = new HtmlReportGenerator("", "",
                JMeterUtils.getJMeterHome() + "/bin/jmeter");
        List<String> errors = htmlReportGenerator.run();
        assertTrue(errors.contains(
                JMeterUtils.getResString("csv_file") + JMeterUtils.getResString(HtmlReportGenerator.NO_FILE)));
        assertTrue(errors.contains(JMeterUtils.getResString("user_properties_file")
                + JMeterUtils.getResString(HtmlReportGenerator.NO_FILE)));
        assertTrue(errors.contains(JMeterUtils.getResString("output_directory")
                + JMeterUtils.getResString(HtmlReportGenerator.NO_DIRECTORY)));
    }

    /**
     * Check if HtmlReportAction.run() return the good error messages if the
     * file don't have the right extension or the directory isn't empty
     */
    @Test
    public void checkCorrectFileOrDirectoryErrors() {
        HtmlReportGenerator htmlReportGenerator = new HtmlReportGenerator(JMeterUtils.getJMeterBinDir() + "/jmeter",
                JMeterUtils.getJMeterBinDir() + "/jmeter", JMeterUtils.getJMeterBinDir());
        List<String> errors = htmlReportGenerator.run();
        assertTrue(errors.contains(
                JMeterUtils.getResString("csv_file") + JMeterUtils.getResString(HtmlReportGenerator.WRONG_FILE)));
        assertTrue(errors.contains(JMeterUtils.getResString("user_properties_file")
                + JMeterUtils.getResString(HtmlReportGenerator.WRONG_FILE)));
        assertTrue(errors.contains(JMeterUtils.getResString("output_directory")
                + JMeterUtils.getResString(HtmlReportGenerator.NOT_EMPTY_DIRECTORY)));
    }

    /**
     * Check if HtmlReportAction.run() return the good error message in case of
     * success
     * 
     * @throws IOException
     * @throws ParseException
     */
    @Test
    public void checkSuccessFile() throws IOException {
        FileUtils.forceMkdir(new File(JMeterUtils.getJMeterBinDir() + "/testReport"));
        HtmlReportGenerator htmlReportGenerator = new HtmlReportGenerator(
                JMeterUtils.getJMeterBinDir() + "/testfiles/HTMLReportTestFile.csv",
                JMeterUtils.getJMeterBinDir() + "/user.properties", JMeterUtils.getJMeterBinDir() + "/testReport");
        assertEquals(HtmlReportGenerator.HTML_REPORT_SUCCESS, htmlReportGenerator.run().get(0));
        ObjectMapper mapper = new ObjectMapper();
        FileReader jsonFileReader = new FileReader(JMeterUtils.getJMeterBinDir() + "/testReport/statistics.json");
        JsonNode root = mapper.readTree(jsonFileReader);
        jsonFileReader.close();
        assertEquals(8615, root.path("Total").path("sampleCount").intValue());

    }

    /**
     * Delete the test directory if it was created
     * 
     * @throws IOException
     */
    @AfterClass
    public static void deleteTestDirectory() throws IOException {
        File testDirectory = new File(JMeterUtils.getJMeterBinDir() + "/testReport");
        if (testDirectory.exists()) {
            FileUtils.forceDelete(testDirectory);
        }
    }

    /**
     * HtmlReportAction should throw NPE as there is no
     * GuiPackage.getMainFrame()
     * 
     * @throws IllegalUserActionException
     */
    @Test(expected = NullPointerException.class)
    public void testDoActionWithoutCallFrame() throws IllegalUserActionException {
        HtmlReportAction htmlReportAction = new HtmlReportAction();
        htmlReportAction.doAction(null);
    }

    /**
     * Check if htmlReportPanel is initialized like it should be
     * 
     * @throws IllegalUserActionException
     */
    @Test
    public void testDoAction() {
        // Prepare GUIPackage in order for htmlReportPanel to launch
        JMeterTreeModel treeModel = new JMeterTreeModel();
        JMeterTreeListener treeListener = new JMeterTreeListener(treeModel);
        GuiPackage.initInstance(treeListener, treeModel);
        GuiPackage.getInstance().setMainFrame(new MainFrame(treeModel, treeListener));

        HtmlReportPanel htmlReportPanel = new HtmlReportPanel();
        htmlReportPanel.setupInputDialog();
        assertEquals("", htmlReportPanel.getcSVFilePathTextField().getText());
        assertEquals("", htmlReportPanel.getOutputDirectoryPathTextField().getText());
        assertEquals("", htmlReportPanel.getUserPropertiesFilePathTextField().getText());
        assertEquals(JMeterUtils.getResString("html_report_request"),
                htmlReportPanel.getReportLaunchButton().getText());

    }
}
