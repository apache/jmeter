package org.apache.jmeter;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.ComponentUtil;

class Splash {

	public Splash() {
	}
	

	
	JFrame splash;
	
	void showSplash()
	{
		splash = new JFrame();
		JPanel borderedPanel = new JPanel();
		BevelBorder b = new SoftBevelBorder(BevelBorder.RAISED,
				Color.LIGHT_GRAY,Color.DARK_GRAY);
		borderedPanel.setBorder(b);
		SplashCanvas image = new SplashCanvas(borderedPanel.getFont().deriveFont(Font.BOLD));
		borderedPanel.add(image);
		splash.getContentPane().add(borderedPanel);
		splash.setResizable(false);
		splash.setUndecorated(true);
		splash.pack();
		ComponentUtil.centerComponentInWindow(splash);
		splash.setVisible(true);
		splash.setAlwaysOnTop(true);
	}
	
	void removeSplash()
	{
		SwingUtilities.invokeLater(new Runnable(){
			public void run()
			{
				splash.setVisible(false);
				splash.dispose();
				splash = null;
			}
		});
	}
	
	private class SplashCanvas extends Canvas
	{
		Image icon = JMeterUtils.getImage("jmeter.jpg").getImage();
		String versionNotice = new String("Apache JMeter Version " + JMeterUtils.getJMeterVersion());
		String copyRight = JMeterUtils.getJMeterCopyright();
		Font font;
		Rectangle2D versionRect;
		Rectangle2D copyRect;
		/* (non-Javadoc)
		 * @see java.awt.Canvas#paint(java.awt.Graphics)
		 */
		@Override
		public void paint(Graphics g) {
			g.drawImage(icon,0,0,icon.getWidth(null),icon.getHeight(null),null);
			g.setColor(Color.BLACK);
			g.setFont(font);
			g.drawChars(versionNotice.toCharArray(),0,versionNotice.length(),10,115);
			g.drawChars(copyRight.toCharArray(),0,copyRight.length(),10,120 + (int)versionRect.getHeight());
		}
		
		public SplashCanvas(Font f)
		{
			font = f;
			versionRect = font.getStringBounds(versionNotice,new FontRenderContext(null,true,true));
			copyRect = font.getStringBounds(copyRight,new FontRenderContext(null,true,true));
		}
		
		/* (non-Javadoc)
		 * @see java.awt.Component#getBackground()
		 */
		@Override
		public Color getBackground() {
			return Color.WHITE;
		}

		/* (non-Javadoc)
		 * @see java.awt.Component#getMaximumSize()
		 */
		@Override
		public Dimension getMaximumSize() {
			// TODO Auto-generated method stub
			return getPreferredSize();
		}

		/* (non-Javadoc)
		 * @see java.awt.Component#getMinimumSize()
		 */
		@Override
		public Dimension getMinimumSize() {
			// TODO Auto-generated method stub
			return getPreferredSize();
		}

		/* (non-Javadoc)
		 * @see java.awt.Component#getPreferredSize()
		 */
		@Override
		public Dimension getPreferredSize() {
			return new Dimension(Math.max(Math.max(getWidth(),25 + (int)versionRect.getWidth()),
					25 + (int)copyRect.getWidth()),
					getHeight()+(int)versionRect.getHeight() + (int)copyRect.getHeight() + 20);
		}

		public int getWidth()
		{
			return icon.getWidth(null);
		}
		
		public int getHeight()
		{
			return icon.getHeight(null);
		}		
	}

}
