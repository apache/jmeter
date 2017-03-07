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
package org.apache.jmeter.save;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JComponent;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.xmlgraphics.image.codec.png.PNGEncodeParam;
import org.apache.xmlgraphics.image.codec.png.PNGImageEncoder;
import org.apache.xmlgraphics.image.codec.tiff.TIFFEncodeParam;
import org.apache.xmlgraphics.image.codec.tiff.TIFFImageEncoder;

/**
 * Class is responsible for taking a component and saving it as a JPEG, PNG or
 * TIFF. The class is very simple. Thanks to Batik and the developers who worked
 * so hard on it.
 */
public class SaveGraphicsService {

    public static final int PNG = 0;

    public static final int TIFF = 1;

    public static final String PNG_EXTENSION = ".png"; //$NON-NLS-1$

    public static final String TIFF_EXTENSION = ".tif"; //$NON-NLS-1$

    public static final String JPEG_EXTENSION = ".jpg"; //$NON-NLS-1$

    /**
     *
     */
    public SaveGraphicsService() {
        super();
    }

    /**
     * Method will save the JComponent as an image. The formats are PNG, and
     * TIFF.
     *
     * @param filename
     *            name of the file to store the image into
     * @param type
     *            of the image to be stored. Can be one of {@value #PNG} for PNG
     *            or {@value #TIFF} for TIFF
     * @param component
     *            to draw the image on
     */
    public void saveJComponent(String filename, int type, JComponent component) {
        Dimension size = component.getSize();
        BufferedImage image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_BYTE_INDEXED);
        Graphics2D grp = image.createGraphics();
        component.paint(grp);

        if (type == PNG) {
            filename += PNG_EXTENSION;
            this.savePNGWithBatik(filename, image);
        } else if (type == TIFF) {
            filename = filename + TIFF_EXTENSION;
            this.saveTIFFWithBatik(filename, image);
        }
    }

    /**
     * Use Batik to save a PNG of the graph
     *
     * @param filename
     *            name of the file to store the image into
     * @param image
     *            to be stored
     */
    public void savePNGWithBatik(String filename, BufferedImage image) {
        File outfile = new File(filename);
        OutputStream fos = createFile(outfile);
        if (fos == null) {
            return;
        }
        PNGEncodeParam param = PNGEncodeParam.getDefaultEncodeParam(image);
        PNGImageEncoder encoder = new PNGImageEncoder(fos, param);
        try {
            encoder.encode(image);
        } catch (IOException e) {
            JMeterUtils.reportErrorToUser("PNGImageEncoder reported: "+e.getMessage(), "Problem creating image file");
        } finally {
            JOrphanUtils.closeQuietly(fos);
        }
    }

    /**
     * Use Batik to save a TIFF file of the graph
     *
     * @param filename
     *            name of the file to store the image into
     * @param image
     *            to be stored
     */
    public void saveTIFFWithBatik(String filename, BufferedImage image) {
        File outfile = new File(filename);
        OutputStream fos = createFile(outfile);
        if (fos == null) {
            return;
        }
        TIFFEncodeParam param = new TIFFEncodeParam();
        TIFFImageEncoder encoder = new TIFFImageEncoder(fos, param);
        try {
            encoder.encode(image);
        } catch (IOException e) {
            JMeterUtils.reportErrorToUser("TIFFImageEncoder reported: "+e.getMessage(), "Problem creating image file");
        } catch (Error e) { // NOSONAR TIFFImageEncoder uses Error to report runtime problems
            JMeterUtils.reportErrorToUser("TIFFImageEncoder reported: "+e.getMessage(), "Problem creating image file");
            if (e.getClass() != Error.class){// NOSONAR rethrow other errors
                throw e;
            }
        } finally {
            JOrphanUtils.closeQuietly(fos);
        }
    }

    /**
     * Create a new file for the graphics. Since the method creates a new file,
     * we shouldn't get a FNFE.
     *
     * @param filename
     * @return output stream created from the filename
     */
    private FileOutputStream createFile(File filename) {
        try {
            return new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            JMeterUtils.reportErrorToUser("Could not create file: "+e.getMessage(), "Problem creating image file");
            return null;
        }
    }

}
