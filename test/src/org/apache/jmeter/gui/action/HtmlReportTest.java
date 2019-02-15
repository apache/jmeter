package org.apache.jmeter.gui.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.jmeter.gui.action.HtmlReportAction;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

public class HtmlReportTest extends JMeterTestCase {

    /**
     * Check if HtmlReportAction.run() return the good error messages if there is no file or directory at each path
     */
    @Test
    public void checkNoFileOrDirectoryErrors() {
        HtmlReportAction htmlReportAction = new HtmlReportAction("", "", JMeterUtils.getJMeterHome() + "/bin/jmeter");
        List<String> errors = htmlReportAction.run();
        assertTrue(errors
                .contains(JMeterUtils.getResString("csv_file") + JMeterUtils.getResString(HtmlReportAction.NO_FILE)));
        assertTrue(errors.contains(
                JMeterUtils.getResString("user_properties_file") + JMeterUtils.getResString(HtmlReportAction.NO_FILE)));
        assertTrue(errors.contains(JMeterUtils.getResString("output_directory")
                + JMeterUtils.getResString(HtmlReportAction.NO_DIRECTORY)));
    }

    /**
     * Check if HtmlReportAction.run() return the good error messages if the file don't have the right extension or the directory isn't empty
     */
    @Test
    public void checkCorrectFileOrDirectoryErrors() {
        HtmlReportAction htmlReportAction = new HtmlReportAction(JMeterUtils.getJMeterBinDir() + "/jmeter",
                JMeterUtils.getJMeterBinDir() + "/jmeter", JMeterUtils.getJMeterBinDir());
        List<String> errors = htmlReportAction.run();
        assertTrue(errors.contains(
                JMeterUtils.getResString("csv_file") + JMeterUtils.getResString(HtmlReportAction.WRONG_FILE)));
        assertTrue(errors.contains(JMeterUtils.getResString("user_properties_file")
                + JMeterUtils.getResString(HtmlReportAction.WRONG_FILE)));
        assertTrue(errors.contains(JMeterUtils.getResString("output_directory")
                + JMeterUtils.getResString(HtmlReportAction.NOT_EMPTY_DIRECTORY)));
    }

    /**
     * Check if HtmlReportAction.run() return the good error message in case of success
     * @throws IOException
     * @throws ParseException 
     */
    @Test
    public void checkSuccessFile() throws IOException, ParseException {
        FileUtils.forceMkdir(new File(JMeterUtils.getJMeterBinDir() + "/testReport"));
        HtmlReportAction htmlReportAction = new HtmlReportAction(
                JMeterUtils.getJMeterBinDir() + "/testfiles/HTMLReportTestFile.csv",
                JMeterUtils.getJMeterBinDir() + "/user.properties", JMeterUtils.getJMeterBinDir() + "/testReport");
        assertEquals(HtmlReportAction.HTML_REPORT_SUCCESS, htmlReportAction.run().get(0));
        JSONParser parser = new JSONParser();
        FileReader fr = new FileReader(JMeterUtils.getJMeterBinDir()+"/testReport/statistics.json");
        JSONObject statistics = (JSONObject) parser.parse(fr);
        assertEquals(8615,(((JSONObject) statistics.get("Total")).get("sampleCount")));
        fr.close();
    }

    /**
     * Delete the test directory if it was created
     * @throws IOException
     */
    @AfterClass
    public static void deleteTestDirectory() throws IOException {
        File testDirectory = new File(JMeterUtils.getJMeterBinDir() + "/testReport");
        if (testDirectory.exists()) {
            FileUtils.forceDelete(testDirectory);
        }
    }
}
