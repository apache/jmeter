// $Header: 
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.swing.JComponent;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * Class is responsible for taking a component and saving it
 * as a JPEG. The class is very simple. It provides one
 * method saveJComponent(filename,Component). This means any
 * GUI component can be passed to the save service. Logic
 * governing which panels can be saved is completely 
 * external to the save service.
 */
public class SaveGraphicsService implements SaveServiceConstants {

	/**
	 * 
	 */
	public SaveGraphicsService() {
		super();
	}

	/**
	 * Method saves a given component to a file as a
	 * JPeg.
	 * @param filename
	 * @param component
	 */
	public void saveJComponent(String filename, JComponent component)
	{
		Dimension size = component.getSize();
		BufferedImage image = new BufferedImage(size.width, size.height,
			BufferedImage.TYPE_INT_RGB);
		Graphics2D grp = image.createGraphics();
		component.setBackground(Color.white);
		component.paint(grp);
		
		File outfile = new File(filename);
		System.out.println("file: " + outfile.getAbsolutePath());
		FileOutputStream fos = createFile(outfile);
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(fos);
		try {
			encoder.encode(image);
			fos.close();
		} catch (Exception e){
			// for now do nothing with the exception
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
