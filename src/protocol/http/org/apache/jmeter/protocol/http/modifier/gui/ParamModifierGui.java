package org.apache.jmeter.protocol.http.modifier.gui;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.jmeter.config.gui.AbstractModifierGui;
import org.apache.jmeter.protocol.http.modifier.ParamMask;
import org.apache.jmeter.protocol.http.modifier.ParamModifier;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;

/****************************************
 * A swing panel to allow UI with the ParamModifier class.
 *
 *@author    David La France
 *@author     <a href="mailto:seade@backstagetech.com.au">Scott Eade</a>
 *@created   Jan 18, 2002
 *@version   $Revision$
 ***************************************/
public class ParamModifierGui extends AbstractModifierGui implements FocusListener
{

	private final String NAME = "name";
	private final String PREFIX = "prefix";
	private final String LOWERBOUND = "lowerBound";
	private final String UPPERBOUND = "upperBound";
	private final String INCREMENT = "increment";
	private final String SUFFIX = "suffix";

	private JTextField _fieldName;
	private JTextField _prefix;
	private JTextField _lowerBound;
	private JTextField _upperBound;
	private JTextField _increment;
	private JTextField _suffix;


	/****************************************
	 * !ToDo (Constructor description)
	 ***************************************/
	public ParamModifierGui()
	{
		init();
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public String getStaticLabel()
	{
		return JMeterUtils.getResString("HTML Parameter Mask");
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param el  !ToDo (Parameter description)
	 ***************************************/
	public void configure(TestElement el)
	{
		super.configure(el);
		ParamModifier model = (ParamModifier)el;
		updateGui(model);
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public TestElement createTestElement()
	{
		ParamModifier modifier = new ParamModifier();
		configureTestElement(modifier);
		ParamMask mask = modifier.getMask();
		mask.setFieldName(_fieldName.getText());
		mask.setPrefix(_prefix.getText());
		mask.setLowerBound(Long.parseLong(_lowerBound.getText()));
		mask.setIncrement(Long.parseLong(_increment.getText()));
		mask.setUpperBound(Long.parseLong(_upperBound.getText()));
		mask.setSuffix(_suffix.getText());
		mask.resetValue();
		return modifier;
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param evt  !ToDo (Parameter description)
	 ***************************************/
	public void focusGained(FocusEvent evt) { }

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param evt  !ToDo (Parameter description)
	 ***************************************/
	public void focusLost(FocusEvent evt)
	{
		String name = ((Component)evt.getSource()).getName();
		if(evt.isTemporary())
		{
			return;
		}
		else if(name.equals(LOWERBOUND))
		{
			checkTextField(evt, "0");
		}
		else if(name.equals(UPPERBOUND))
		{
			checkTextField(evt, "0");
		}
		else if(name.equals(INCREMENT))
		{
			checkTextField(evt, "0");
		}
	}

	/****************************************
	 * !ToDo (Method description)
	 ***************************************/
	protected void init()
	{
		this.setLayout(new VerticalLayout(5, VerticalLayout.LEFT, VerticalLayout.TOP));

		// MAIN PANEL
		JPanel mainPanel = new JPanel();
		Border margin = new EmptyBorder(10, 10, 5, 10);
		mainPanel.setBorder(margin);
		mainPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));

		// TITLE
		//JLabel panelTitleLabel = new JLabel(JMeterUtils.getResString("url_config_title"));
		JLabel panelTitleLabel = new JLabel(JMeterUtils.getResString("HTML Parameter Mask"));
		Font curFont = panelTitleLabel.getFont();
		int curFontSize = curFont.getSize();
		curFontSize += 4;
		panelTitleLabel.setFont(new Font(curFont.getFontName(), curFont.getStyle(), curFontSize));
		mainPanel.add(panelTitleLabel);

		// NAME
		mainPanel.add(getNamePanel());

		// PARAMETER MASK
		mainPanel.add(getParameterMaskPanel());

		this.add(mainPanel);
		this.updateUI();
	}


	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param model  !ToDo (Parameter description)
	 ***************************************/
	private void updateGui(ParamModifier model)
	{
		_fieldName.setText(model.getMask().getFieldName());
		_prefix.setText(model.getMask().getPrefix());
		_lowerBound.setText(Long.toString(model.getMask().getLowerBound()));
		_upperBound.setText(Long.toString(model.getMask().getUpperBound()));
		_increment.setText(Long.toString(model.getMask().getIncrement()));
		_suffix.setText(model.getMask().getSuffix());
	}

	private JPanel getParameterMaskPanel()
	{
		JPanel paramMaskPanel = new JPanel();
		paramMaskPanel.setLayout(new GridBagLayout());
		//paramMaskPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils.getResString("web_request")));
		paramMaskPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), JMeterUtils.getResString("HTML Parameter Mask")));

		GridBagConstraints gridBagConstraints;

		JLabel name = new JLabel(JMeterUtils.getResString("Name"));
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.ipadx = 40;
		gridBagConstraints.insets = new Insets(5, 15, 0, 0);
		paramMaskPanel.add(name, gridBagConstraints);

		JLabel prefix = new JLabel(JMeterUtils.getResString("ID Prefix"));
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.ipadx = 40;
		gridBagConstraints.insets = new Insets(5, 15, 0, 0);
		paramMaskPanel.add(prefix, gridBagConstraints);

		JLabel start = new JLabel(JMeterUtils.getResString("Lower Bound"));
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(5, 15, 0, 0);
		paramMaskPanel.add(start, gridBagConstraints);

		JLabel stop = new JLabel(JMeterUtils.getResString("Upper Bound"));
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(5, 15, 0, 0);
		paramMaskPanel.add(stop, gridBagConstraints);

		JLabel increment = new JLabel(JMeterUtils.getResString("Increment"));
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(5, 15, 0, 0);
		paramMaskPanel.add(increment, gridBagConstraints);

		JLabel suffix = new JLabel(JMeterUtils.getResString("ID Suffix"));
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.ipadx = 40;
		gridBagConstraints.insets = new Insets(5, 15, 0, 0);
		paramMaskPanel.add(suffix, gridBagConstraints);

		_fieldName = new JTextField("", 10);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(10, 15, 0, 0);
		_fieldName.setName(NAME);
		paramMaskPanel.add(_fieldName, gridBagConstraints);

		_prefix = new JTextField("", 5);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(10, 15, 0, 0);
		_prefix.setName(PREFIX);
		paramMaskPanel.add(_prefix, gridBagConstraints);

		_lowerBound = new JTextField("0", 5);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(10, 15, 0, 0);
		_lowerBound.addFocusListener(this);
		_lowerBound.setName(LOWERBOUND);
		paramMaskPanel.add(_lowerBound, gridBagConstraints);

		_upperBound = new JTextField("10", 5);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 3;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(10, 15, 0, 0);
		_upperBound.addFocusListener(this);
		_upperBound.setName(UPPERBOUND);
		paramMaskPanel.add(_upperBound, gridBagConstraints);

		_increment = new JTextField("1", 3);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 4;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(10, 15, 0, 0);
		_increment.addFocusListener(this);
		_increment.setName(INCREMENT);
		paramMaskPanel.add(_increment, gridBagConstraints);

		_suffix = new JTextField("", 5);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 5;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(10, 15, 0, 0);
		_suffix.setName(SUFFIX);
		paramMaskPanel.add(_suffix, gridBagConstraints);

		return paramMaskPanel;
	}

	/****************************************
	 * Used to validate a text field that requires a <code>long</code> input.
	 * Returns the <code>long</code> if valid, else creates a pop-up error message
	 * and throws a NumberFormatException.
	 *
	 *@param evt                        !ToDo (Parameter description)
	 *@param defaultValue               !ToDo (Parameter description)
	 *@return                           The number entered in the text field
	 ***************************************/
	private long checkTextField(FocusEvent evt, String defaultValue)
	{
		JTextField temp = (JTextField)evt.getSource();
		boolean pass = true;
		long longVal = 0;

		try
		{
			longVal = Long.parseLong(temp.getText());
		}
		catch(NumberFormatException err)
		{
			JOptionPane.showMessageDialog(this,
					"This field must have a long value!",
					"Value Required",
					JOptionPane.ERROR_MESSAGE);
			temp.setText(defaultValue);
			temp.requestFocus();
		}
		return longVal;
	}

}
