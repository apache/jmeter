// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import javax.swing.JComponent;

import com.sun.media.jai.codec.BMPEncodeParam;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.PNGEncodeParam;
import com.sun.media.jai.codec.TIFFEncodeParam;

/**
 * Class is responsible for taking a component and saving it
 * as a JPEG. The class is very simple. It provides one
 * method saveJComponent(filename,Component). This means any
 * GUI component can be passed to the save service. Logic
 * governing which panels can be saved is completely 
 * external to the save service.
 */
public class SaveGraphicsService implements SaveServiceConstants {

	public static final int PNG = 0;
	public static final int BMP = 1;
	public static final int TIFF = 2;
	public static final String PNG_EXTENSION = ".png";
	public static final String BMP_EXTENSION = ".bmp";
	public static final String TIFF_EXTENSION = ".tif";
	
	/**
	 * 
	 */
	public SaveGraphicsService() {
		super();
	}

	/**
	 * Method uses JAI to save the graph instead of the
	 * stock com.sun.image.codec.jpeg API. The stock
	 * codec is a bit lame and generates poor quality.
	 * @param filename
	 * @param component
	 */
	public void saveJComponentWithJAI(String filename, JComponent component){
		Dimension size = component.getSize();
		BufferedImage image = new BufferedImage(size.width, size.height,
			BufferedImage.TYPE_INT_RGB);
		Graphics2D grp = image.createGraphics();
		component.paint(grp);
		
		File outfile = new File(filename);
		FileOutputStream fos = createFile(outfile);
		ImageEncoder encoder = ImageCodec.createImageEncoder("PNG",fos,null);
		PNGEncodeParam param = new PNGEncodeParam.RGB();
		encoder.setParam(param);
		try {
			encoder.encode(image);
			fos.close();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Method will save the JComponent as an image. The
	 * formats are PNG, BMP, and TIFF.
	 * @param filename
	 * @param type
	 * @param component
	 */
	public void saveJComponent(String filename, int type, JComponent component){	
		Dimension size = component.getSize();
		BufferedImage image = new BufferedImage(size.width, size.height,
			BufferedImage.TYPE_INT_RGB);
		Graphics2D grp = image.createGraphics();
		component.paint(grp);
		
		if (type == PNG){
			filename += PNG_EXTENSION;
			this.savePNG(filename,image);
		} else if (type == BMP){
			filename += BMP_EXTENSION;
			this.saveBMP(filename,image);
		} else if (type == TIFF){
			filename = filename + TIFF_EXTENSION;
			this.saveTIFF(filename,image);
		}
	}

	/**
	 * Method takes a filename and BufferedImage. It will save
	 * the image as PNG.
	 * @param filename
	 * @param image
	 */	
	public void savePNG(String filename, BufferedImage image){
		File outfile = new File(filename);
		FileOutputStream fos = createFile(outfile);
		ImageEncoder encoder = ImageCodec.createImageEncoder("PNG",fos,null);
		PNGEncodeParam param = new PNGEncodeParam.RGB();
		encoder.setParam(param);
		try {
			encoder.encode(image);
			fos.close();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Method takes filename and BufferedImage. It will save
	 * the image as a BMP. BMP is generally a larger file
	 * than PNG.
	 * @param filename
	 * @param image
	 */	
	public void saveBMP(String filename, BufferedImage image){
		File outfile = new File(filename);
		FileOutputStream fos = createFile(outfile);
		ImageEncoder encoder = ImageCodec.createImageEncoder("BMP",fos,null);
		BMPEncodeParam param = new BMPEncodeParam();
		encoder.setParam(param);
		try {
			encoder.encode(image);
			fos.close();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Method takes a filename and BufferedImage. It will save
	 * the image as a TIFF.
	 * @param filename
	 * @param image
	 */	
	public void saveTIFF(String filename, BufferedImage image){
		File outfile = new File(filename);
		FileOutputStream fos = createFile(outfile);
		ImageEncoder encoder = ImageCodec.createImageEncoder("TIFF",fos,null);
		TIFFEncodeParam param = new TIFFEncodeParam();
		encoder.setParam(param);
		try {
			encoder.encode(image);
			fos.close();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Create a new file for the graphics. Since the method
	 * creates a new file, we shouldn't get a FNFE.
	 * @param filename
	 * @return
	 */
	public FileOutputStream createFile(File filename){
		try {
			return new FileOutputStream(filename);
		} catch (FileNotFoundException e){
			e.printStackTrace();
			return null;
		}
	}
	
}
