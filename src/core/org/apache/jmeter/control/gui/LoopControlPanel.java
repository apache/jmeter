package org.apache.jmeter.control.gui;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.gui.util.FocusRequester;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

/****************************************
 * Title: JMeter Description: Copyright: Copyright (c) 2000 Company: Apache
 *
 *@author    Michael Stover
 *@created   $Date$
 *@version   1.0
 ***************************************/

public class LoopControlPanel extends AbstractControllerGui implements ActionListener
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
        super.configure(element);
        if (element instanceof LoopController)
        {
            setState(((LoopController) element).getLoopString());
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
        modifyTestElement(lc);
        return lc;
    }

    /**
         * Modifies a given TestElement to mirror the data in the gui components.
         * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
         */
    public void modifyTestElement(TestElement lc)
    {
        configureTestElement(lc);
        if (lc instanceof LoopController)
        {
            if (loops.getText().length() > 0)
            {
                ((LoopController) lc).setLoops(loops.getText());
            }
            else
            {
                ((LoopController) lc).setLoops(-1);
            }
        }
    }

    /****************************************
     * !ToDo (Method description)
     *
     *@param event  !ToDo (Parameter description)
     ***************************************/
    public void actionPerformed(ActionEvent event)
    {
        if (infinite.isSelected())
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
        // The Loop Controller panel can be displayed standalone or inside
        // another panel.  For standalone, we want to display the TITLE, NAME,
        // etc. (everything). However, if we want to display it within another
        // panel, we just display the Loop Count fields (not the TITLE and
        // NAME).

        // Standalone
        if (displayName)
        {
            setLayout(new BorderLayout(0, 5));
            setBorder(makeBorder());
            add(makeTitlePanel(), BorderLayout.NORTH);
            
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.add(createLoopCountPanel(), BorderLayout.NORTH);
            add(mainPanel, BorderLayout.CENTER);
        }

        // Embedded
        else
        {
            setLayout(new BorderLayout());
            add(createLoopCountPanel(), BorderLayout.NORTH);
        }
    }

    private JPanel createLoopCountPanel()
    {
        JPanel loopPanel = new JPanel(new BorderLayout(5, 0));

        // LOOP LABEL
        JLabel loopsLabel = new JLabel(JMeterUtils.getResString("iterator_num"));
        loopPanel.add(loopsLabel, BorderLayout.WEST);

        // TEXT FIELD
        loops = new JTextField("1", 5);
        loops.setName(LOOPS);
        loopsLabel.setLabelFor(loops);
        loopPanel.add(loops, BorderLayout.CENTER);

        // FOREVER CHECKBOX
        infinite = new JCheckBox(JMeterUtils.getResString("infinite"));
        infinite.setActionCommand(INFINITE);
        infinite.addActionListener(this);
        loopPanel.add(infinite, BorderLayout.EAST);

        loopPanel.add(Box.createHorizontalStrut(loopsLabel.getPreferredSize().width + loops.getPreferredSize().width + infinite.getPreferredSize().width), BorderLayout.NORTH);

        return loopPanel;
    }
    
    private void setState(String loopCount)
    {
        if (loopCount.startsWith("-")) {
            setState(-1);
        } else {
            loops.setText(loopCount);
            infinite.setSelected(false);
            loops.setEnabled(true);
        }
    }

    private void setState(int loopCount)
    {
        if (loopCount <= -1)
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
