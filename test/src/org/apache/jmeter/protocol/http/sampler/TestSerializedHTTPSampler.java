package org.apache.jmeter.protocol.http.sampler;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.nio.file.Paths;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.test.JMeterSerialTest;
import org.junit.Test;

public class TestSerializedHTTPSampler extends JMeterTestCase implements JMeterSerialTest {

    @Test
    public void checkThatFilesAreReadRelativeToBaseDir() {
        String baseDirPath = FileServer.getFileServer().getBaseDir();
        File baseDir = new File(baseDirPath);
        try {
            FileServer.getFileServer().setBase(Paths.get(JMeterUtils.getJMeterHome(), "test", "resources").toFile());
            HTTPSamplerBase sampler = new HTTPSampler3();
            sampler.setMethod("POST");
            sampler.setPath("https://httpbin.org/post");
            sampler.setHTTPFiles(new HTTPFileArg[]{new HTTPFileArg("resourcefile.txt", "", "")});
        
            SampleResult sample = sampler.sample();
            assertThat(sample.getResponseDataAsString(), not(containsString("java.io.FileNotFoundException:")));
        } finally {
            FileServer.getFileServer().setBase(baseDir);
        }
    }

}
