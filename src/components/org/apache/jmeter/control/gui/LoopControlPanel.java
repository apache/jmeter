package org.apache.jmeter.control.gui;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.gui.util.FocusRequester;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.jorphan.gui.layout.VerticalLayout;

/****************************************
 * Title: JMeter Description: Copyright: Copyright (c) 2000 Company: Apache
 *
 *@author    Michael Stover
 *@created   $Date$
 *@version   1.0
 ***************************************/

public class LoopControlPanel extends AbstractControllerGui implements KeyListener, ActionListener
{


	JCheckBox infinite;
	JTextField loops;

	private boolean displayName = true;
	private static String INFINITE = "Infinite Field";
	private static String LOOPS = "Loops Field";

	/****************************************
	 * !ToDo (Constructor description)
	 ***************************************/
	public LoopControlPanel()
	{
		this(true);
	}

	/****************************************
	 * !ToDo (Constructor description)
	 *
	 *@param displayName  !ToDo (Parameter description)
	 ***************************************/
	public LoopControlPanel(boolean displayName)
	{
		this.displayName = displayName;
		init();
		setState(1);
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param element  !ToDo (Parameter description)
	 ***************************************/
	public void configure(TestElement element)
	{
		setName((String)element.getProperty(TestElement.NAME));
		if(element instanceof LoopController)
		{
			setState(((LoopController)element).getLoops());
		}
		else
		{
			setState(1);
		}
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public TestElement createTestElement()
	{
		LoopController lc = new LoopController();
		configureTestElement(lc);
		if(loops.getText().length() > 0)
		{
			lc.setLoops(Integer.parseInt(loops.getText()));
		}
		else
		{
			lc.setLoops(-1);
		}
		return lc;
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param event  !ToDo (Parameter description)
	 ***************************************/
	public void actionPerformed(ActionEvent event)
	{
		if(infinite.isSelected())
		{
			loops.setText("");
			loops.setEnabled(false);
		}
		else
		{
			loops.setEnabled(true);
			new FocusRequester(loops);
		}
	}

	/****************************************
	 * Description of the Method
	 *
	 *@param e  Description of Parameter
	 ***************************************/
	public void keyPressed(KeyEvent e) { }

	/****************************************
	 * Description of the Method
	 *
	 *@param e  Description of Parameter
	 ***************************************/
	public void keyTyped(KeyEvent e) { }

	/****************************************
	 * Description of the Method
	 *
	 *@param e  Description of Parameter
	 ***************************************/
	public void keyReleased(KeyEvent e)
	{
		String temp = e.getComponent().getName();
		if(temp.equals(LOOPS))
		{
			try
			{
				Integer.parseInt(loops.getText());
			}
			catch(NumberFormatException ex)
			{
				if(loops.getText().length() > 0)
				{
					// We need a standard warning/error dialog. The problem with
					// having it here is that the dialog is centered over this
					// LoopControlPanel instead of begin centered in the entire
					// JMeter GUI window.
					JOptionPane.showMessageDialog(this, "You must enter a valid number",
							"Invalid data", JOptionPane.WARNING_MESSAGE);
					loops.setText("");
				}
			}
		}
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public String getStaticLabel()
	{
		return JMeterUtils.getResString("loop_controller_title");
	}

	private void init()
	{
		// The Loop Controller panel can be displayed standalone or inside another panel.
		// For standalone, we want to display the TITLE, NAME, etc. (everything). However,
		// if we want to display it within another panel, we just display the Loop Count
		// fields (not the TITLE and NAME).

		// Standalone
		if(displayName)
		{
			this.setLayout(new VerticalLayout(5, VerticalLayout.LEFT, VerticalLayout.TOP));

			// MAIN PANEL
			JPanel mainPanel = new JPanel();
			Border margin = new EmptyBorder(10, 10, 5, 10);
			mainPanel.setBorder(margin);
			mainPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));

			// TITLE
			JLabel panelTitleLabel = new JLabel(JMeterUtils.getResString("loop_controller_title"));
			Font curFont = panelTitleLabel.getFont();
			int curFontSize = curFont.getSize();
			curFontSize += 4;
			panelTitleLabel.setFont(new Font(curFont.getFontName(), curFont.getStyle(), curFontSize));
			mainPanel.add(panelTitleLabel);

			// NAME
			mainPanel.add(getNamePanel());

			// LOOP
			mainPanel.add(createLoopCountPanel());

			this.add(mainPanel);
		}

		// Embedded
		else
		{
			this.add(createLoopCountPanel());
		}
	}


	private JPanel createLoopCountPanel()
	{
		JPanel loopPanel = new JPanel();

		// LOOP LABEL
		JLabel loopsLabel = new JLabel(JMeterUtils.getResString("iterator_num"));
		loopPanel.add(loopsLabel);

		// TEXT FIELD
		loops = new JTextField(5);
		loopPanel.add(loops);
		loops.setName(LOOPS);
		loops.addKeyListener(this);
		loops.setText("1");

		// FOREVER CHECKBOX
		infinite = new JCheckBox(JMeterUtils.getResString("infinite"));
		infinite.setActionCommand(INFINITE);
		infinite.addActionListener(this);
		loopPanel.add(infinite);

		return loopPanel;
	}

	private void setState(int loopCount)
	{
		if(loopCount <= -1)
		{
			infinite.setSelected(true);
			loops.setEnabled(false);
			loops.setText("");
		}
		else
		{
			infinite.setSelected(false);
			loops.setEnabled(true);
			loops.setText("" + loopCount);
		}
	}
}
